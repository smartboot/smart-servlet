/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.plugins.async;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.HttpUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.handler.HandlerContext;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.dispatcher.ServletRequestDispatcherWrapper;
import org.smartboot.socket.timer.HashedWheelTimer;
import org.smartboot.socket.timer.TimerTask;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
    private final List<ListenerUnit> listeners = new LinkedList<>();
    private final HttpServletRequestImpl originalRequest;
    private final ServletRequest request;
    private final ServletResponse response;
    private long timeout = 3000;
    private boolean dispatched;
    private boolean finishDispatch;
    private boolean complete;
    private final ServletContextRuntime servletContextRuntime;
    private final CompletableFuture<Object> future;
    private final AsyncContextImpl preAsyncContext;
    private boolean subAsyncContext;

    private TimerTask timerTask;

    private final Runnable timeoutTask = new Runnable() {
        @Override
        public void run() {
            if (complete) {
                return;
            }
            listeners.forEach(unit -> {
                try {
                    unit.listener.onTimeout(new AsyncEvent(AsyncContextImpl.this, unit.request, unit.response));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            if (dispatched) {
                return;
            }
            if (!response.isCommitted()) {
                ServletResponse r = response;
                while (r instanceof ServletResponseWrapper) {
                    r = ((ServletResponseWrapper) r).getResponse();
                }
                if (r instanceof HttpServletResponse) {
                    try {
                        ((HttpServletResponse) r).sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            dispatched = true;
            finishDispatch();
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
        if (preAsyncContext != null) {
            preAsyncContext.subAsyncContext = true;
        }
        timerTask = HashedWheelTimer.DEFAULT_TIMER.schedule(timeoutTask, timeout, TimeUnit.MILLISECONDS);
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

    ServletRequestDispatcherWrapper wrapper;
    Runnable runnable;

    @Override
    public void dispatch(ServletContext context, String path) {
        if (dispatched) {
            throw new IllegalStateException();
        }
        if (!(context instanceof ServletContextImpl)) {
            throw new IllegalStateException();
        }
        dispatched = true;
        ServletContextImpl servletContext = (ServletContextImpl) context;
        wrapper = new ServletRequestDispatcherWrapper(originalRequest, DispatcherType.ASYNC, false);
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


        runnable = () -> {
            try {
                originalRequest.resetAsyncStarted();
                HandlerContext handlerContext = new HandlerContext(wrapper, response, servletContext, false);
                servletContext.getPipeline().handleRequest(handlerContext);
            } catch (Throwable e) {
                e.printStackTrace();
                throw new WrappedRuntimeException(e);
            } finally {
                finishDispatch();
                originalRequest.getInternalAsyncContext().complete();
            }
        };
    }

    @Override
    public synchronized void complete() {
        if (complete) {
            if (preAsyncContext != null) {
                if (preAsyncContext.subAsyncContext) {
                    throw new IllegalStateException();
                }
                preAsyncContext.complete();
            }
            return;
        }
        if (!dispatched) {
            finishDispatch();
            dispatched = true;
            return;
        }
        //dispatched 为true,触发complete场景：
        // 1. 在service方法中，调用了AsyncContext的complete方法
        // 2. 退出service，异步任务还未执行完毕。
        if (finishDispatch) {
            doComplete();
        } else if (runnable != null) {
            servletContextRuntime.getDeploymentInfo().getExecutor().execute(runnable);
        }
    }


    public synchronized void doComplete() {
        if (subAsyncContext) {
            //最末尾的异步任务优先complete
            originalRequest.getInternalAsyncContext().complete();
            return;
        }
        listeners.forEach(unit -> {
            try {
                unit.listener.onComplete(new AsyncEvent(this, unit.request, unit.response));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        if (timerTask != null) {
            timerTask.cancel();
        }
        complete = true;
        if (preAsyncContext == null) {
            try {
                response.flushBuffer();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            future.complete(null);
        } else {
            preAsyncContext.subAsyncContext = false;
            preAsyncContext.complete();
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
        if (finishDispatch) {
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
        timerTask.cancel();
        timerTask = HashedWheelTimer.DEFAULT_TIMER.schedule(timeoutTask, timeout, TimeUnit.MILLISECONDS);
    }

    @Override
    public long getTimeout() {
        return timeout;
    }

    public void finishDispatch() {
        finishDispatch = true;
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
