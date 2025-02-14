/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.async;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.AsyncEvent;
import jakarta.servlet.AsyncListener;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smartboot.socket.timer.HashedWheelTimer;
import org.smartboot.socket.timer.TimerTask;
import tech.smartboot.feat.core.common.enums.HttpStatus;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;
import tech.smartboot.feat.core.common.utils.HttpUtils;
import tech.smartboot.feat.core.common.utils.StringUtils;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.exception.WrappedRuntimeException;
import tech.smartboot.servlet.handler.HandlerContext;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.impl.HttpServletResponseImpl;
import tech.smartboot.servlet.impl.ServletContextImpl;
import tech.smartboot.servlet.plugins.dispatcher.ServletRequestDispatcherWrapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/11/23
 */
public class AsyncContextImpl implements AsyncContext {
    private static final int DISPATCHER_STATE_INIT = 0;
    private static final int DISPATCHER_STATE_CALL = 1;
    private static final int DISPATCHER_STATE_CALL_NONE = 2;
    private static final int DISPATCHER_STATE_EXECUTING = 3;
    private static final int DISPATCHER_STATE_COMPLETE = 4;
    private static final int DEFAULT_DISPATCHER_TIMEOUT_MILLIS = 30000;
    private static final Logger logger = LoggerFactory.getLogger(AsyncContextImpl.class);
    private final List<ListenerUnit> listeners = new LinkedList<>();
    private final HttpServletRequestImpl originalRequest;
    private final ServletRequest request;
    private final ServletResponse response;
    private long timeout;
    private int dispatchState = DISPATCHER_STATE_INIT;
    private final ServletContextRuntime servletContextRuntime;
    private final CompletableFuture<Object> future;
    /**
     * 前一个异步上下文
     */
    private final AsyncContextImpl preAsyncContext;

    private TimerTask timerTask;

    private final Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            if (dispatchState == DISPATCHER_STATE_COMPLETE) {
                return;
            }
            //已经开始执行响应
            if (response.isCommitted()) {
                return;
            }
            listeners.forEach(unit -> {
                try {
                    unit.listener.onTimeout(new AsyncEvent(AsyncContextImpl.this, unit.request, unit.response));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
//            dispatchInited = true;
            if (dispatchState == DISPATCHER_STATE_INIT && !response.isCommitted()) {
                ServletResponse r = response;
                while (r instanceof ServletResponseWrapper) {
                    r = ((ServletResponseWrapper) r).getResponse();
                }
                if (r instanceof HttpServletResponse) {
                    try {
                        ((HttpServletResponse) r).sendError(HttpStatus.GATEWAY_TIMEOUT.value());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            complete();
        }
    };

    public AsyncContextImpl(ServletContextRuntime runtime, HttpServletRequestImpl originalRequest, ServletRequest request, ServletResponse response, CompletableFuture<Object> future, AsyncContextImpl preAsyncContext) {
        this.originalRequest = originalRequest;
        this.request = request;
        this.response = response;
        this.servletContextRuntime = runtime;
        this.future = future;
        this.preAsyncContext = preAsyncContext;

        if (preAsyncContext != null && preAsyncContext.getTimeout() != runtime.getContainerRuntime().getConfiguration().getDefaultAsyncContextTimeout()) {
            setTimeout(preAsyncContext.getTimeout());
        } else {
            setTimeout(runtime.getContainerRuntime().getConfiguration().getDefaultAsyncContextTimeout());
        }
    }


    @Override
    public ServletRequest getRequest() {
        return request;
    }

    @Override
    public ServletResponse getResponse() {
        return response;
    }

    @Override
    public boolean hasOriginalRequestAndResponse() {
        return request instanceof HttpServletRequestImpl && response instanceof HttpServletResponseImpl;
    }

    @Override
    public void dispatch() {
        String toDispatch;
        String queryString;
        if (request instanceof HttpServletRequest) {
            toDispatch = ((HttpServletRequest) request).getRequestURI().substring(request.getServletContext().getContextPath().length());
            queryString = getQueryString((HttpServletRequest) request);
        } else {
            toDispatch = originalRequest.getRequestURI().substring(request.getServletContext().getContextPath().length());
            queryString = getQueryString(originalRequest);
        }
        if (queryString != null && !queryString.isEmpty()) {
            toDispatch = toDispatch + "?" + queryString;
        }
        dispatch(request.getServletContext(), toDispatch);
    }

    @Override
    public void dispatch(String path) {
        dispatch(request.getServletContext(), path);
    }

    private String getQueryString(HttpServletRequest request) {
//        String queryString = (String) request.getAttribute(RequestDispatcher.FORWARD_QUERY_STRING);
//        if (queryString != null) {
//            return queryString;
//        }
//        queryString = (String) request.getAttribute(AsyncContext.ASYNC_QUERY_STRING);
//        if (queryString != null) {
//            return queryString;
//        }
        return request.getQueryString();
    }

    private Runnable dispatchRunnable;

    @Override
    public void dispatch(ServletContext context, String path) {
        if (dispatchState != DISPATCHER_STATE_INIT && dispatchState != DISPATCHER_STATE_CALL_NONE) {
            throw new IllegalStateException();
        }
        if (!(context instanceof ServletContextImpl)) {
            throw new IllegalStateException();
        }
        dispatchState = DISPATCHER_STATE_CALL;
        ServletContextImpl servletContext = (ServletContextImpl) context;
        ServletRequestDispatcherWrapper wrapper = new ServletRequestDispatcherWrapper(originalRequest, DispatcherType.ASYNC, false);
        path = context.getContextPath() + path;
        String[] array = StringUtils.split(path, "?");
        wrapper.setRequestUri(array[0]);
        Map<String, String[]> parameters = new HashMap<>();
        if (array.length > 1) {
            HttpUtils.decodeParamString(array[1], parameters);
            wrapper.setParameters(parameters);
            wrapper.setQueryString(array[1]);
        }
        //9.7.2 分派的请求参数
        //使用 AsyncContext 的 dispatch 方法调用过的 servlet 能够访问原始请求的路径。必须设置下面的 request 属性：
        //javax.servlet.async.request_uri
        //javax.servlet.async.context_path
        //javax.servlet.async.servlet_path
        //javax.servlet.async.path_info
        //javax.servlet.async.query_string
        //这些属性的值必须分别与 HttpServletRequest 的 getRequestURI,、getContextPath、getServletPath、getPathInfo、
        //getQueryString 方法的返回值相等，调用请求对象的这些方法把值传递给从客户端接收请求的调用链中的第一个 servlet 对象。
//        originalRequest.setRequestUri(originalRequest.getRequestURI());
        wrapper.setAttribute(ASYNC_REQUEST_URI, originalRequest.getRequestURI());
        wrapper.setAttribute(ASYNC_CONTEXT_PATH, originalRequest.getContextPath());
        wrapper.setAttribute(ASYNC_SERVLET_PATH, originalRequest.getServletPath());
        wrapper.setAttribute(ASYNC_PATH_INFO, originalRequest.getPathInfo());
        wrapper.setAttribute(ASYNC_QUERY_STRING, originalRequest.getQueryString());
        wrapper.setAttribute(ASYNC_MAPPING, originalRequest.getHttpServletMapping());
        originalRequest.setServletContext(servletContext);


        dispatchRunnable = () -> {
            try {
                originalRequest.resetAsyncStarted();
                HandlerContext handlerContext = new HandlerContext(wrapper, response, servletContext, false);
                ServletMappingInfo servletMappingInfo = servletContext.getRuntime().getMappingProvider().matchWithContextPath(wrapper.getRequestURI());
                handlerContext.setServletInfo(servletMappingInfo.getServletInfo());
                wrapper.setServletMappingInfo(servletMappingInfo);
                servletContext.getPipeline().handleRequest(handlerContext);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new WrappedRuntimeException(e);
            } finally {
                complete();
//                originalRequest.getInternalAsyncContext().complete();
            }
        };
    }

    @Override
    public synchronized void complete() {
        //启动异步后未调用dispatch,
        if (dispatchState == DISPATCHER_STATE_INIT) {
            dispatchState = DISPATCHER_STATE_CALL_NONE;
            return;
        }
        if (dispatchState == DISPATCHER_STATE_CALL_NONE) {
            onListenerComplete();
            return;
        }
        if (dispatchState == DISPATCHER_STATE_CALL) {
            if (dispatchRunnable == null) {
                onListenerComplete();
            } else {
                dispatchState = DISPATCHER_STATE_EXECUTING;
                servletContextRuntime.getDeploymentInfo().getExecutor().execute(new Runnable() {
                    @Override
                    public void run() {
                        dispatchRunnable.run();
                        onListenerComplete();
                    }
                });
            }
        }
    }

    private void onListenerComplete() {
        if (dispatchState == DISPATCHER_STATE_COMPLETE) {
            logger.warn("Async context is already complete");
            return;
        }
        dispatchState = DISPATCHER_STATE_COMPLETE;
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        listeners.forEach(unit -> {
            try {
                unit.listener.onComplete(new AsyncEvent(this, unit.request, unit.response));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (preAsyncContext != null) {
            preAsyncContext.complete();
        } else {
            try {
                response.flushBuffer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            future.complete(null);
        }
    }

    @Override
    public void start(Runnable run) {
        servletContextRuntime.getDeploymentInfo().getExecutor().execute(run);
    }

    @Override
    public void addListener(AsyncListener listener) {
        addListener(listener, request, response);
    }

    @Override
    public void addListener(AsyncListener listener, ServletRequest servletRequest, ServletResponse servletResponse) {
        if (dispatchState != DISPATCHER_STATE_INIT) {
            throw new IllegalStateException();
        }
        listeners.add(new ListenerUnit(listener, request, response));
    }

    @Override
    public <T extends AsyncListener> T createListener(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    @Override
    public void setTimeout(long timeout) {
        this.timeout = timeout;
        if (timerTask != null) {
            timerTask.cancel();
        }
        timerTask = HashedWheelTimer.DEFAULT_TIMER.schedule(timeoutTask, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    static class ListenerUnit {
        AsyncListener listener;
        ServletRequest request;
        ServletResponse response;

        public ListenerUnit(AsyncListener listener, ServletRequest request, ServletResponse response) {
            this.listener = listener;
            this.request = request;
            this.response = response;
        }
    }
}
