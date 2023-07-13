/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.plugins.dispatcher.ServletRequestDispatcherWrapper;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/11/23
 */
public class AsyncContextImpl implements AsyncContext {
    private List<ListenerUnit> listeners = new LinkedList<>();
    private HttpServletRequestImpl originalRequest;
    private final ServletRequest request;
    private final ServletResponse response;
    private long timeout = 5000;
    private boolean dispatched;
    private boolean complete;
    private final ServletContextRuntime servletContextRuntime;

    public AsyncContextImpl(ServletContextRuntime deployment, HttpServletRequestImpl originalRequest, ServletRequest request, ServletResponse response) {
        this.originalRequest = originalRequest;
        this.request = request;
        this.response = response;
        this.servletContextRuntime = deployment;
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
        if (hasOriginalRequestAndResponse()) {
            String toDispatch = originalRequest.getRequestURI().substring(request.getServletContext().getContextPath().length());
            String qs = originalRequest.getQueryString();
            if (qs != null && !qs.isEmpty()) {
                toDispatch = toDispatch + "?" + qs;
            }
            dispatch(request.getServletContext(), toDispatch);
        } else {
            throw new UnsupportedOperationException();
//            dispatch(request.getServletContext(), "");
        }

    }

    @Override
    public void dispatch(String path) {
        dispatch(request.getServletContext(), path);
    }

    @Override
    public void dispatch(ServletContext context, String path) {
        if (dispatched) {
            throw new IllegalStateException();
        }
        dispatched = true;
        servletContextRuntime.getDeploymentInfo().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ServletRequestDispatcherWrapper wrapper = new ServletRequestDispatcherWrapper(originalRequest, DispatcherType.ASYNC, false);
                HandlerContext handlerContext = new HandlerContext(wrapper, response, originalRequest.getServletContext(), false);
                servletContextRuntime.getServletContext().getPipeline().handleRequest(handlerContext);
            }
        });

    }

    @Override
    public void complete() {
        listeners.forEach(unit -> {
            try {
                unit.listener.onComplete(new AsyncEvent(this, unit.request, unit.response));
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
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
        if (dispatched) {
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
