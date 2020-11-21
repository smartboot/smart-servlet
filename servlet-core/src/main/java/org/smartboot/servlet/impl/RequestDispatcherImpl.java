/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: RequestDispatcherImpl.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.HandlerContext;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import java.io.IOException;

/**
 * 《Servlet3.1规范中文版》第9章 分派请求
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class RequestDispatcherImpl implements RequestDispatcher {
    private final ServletContextImpl servletContext;
    private boolean named;

    public RequestDispatcherImpl(ServletContextImpl servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (response.isCommitted()) {
            throw new IllegalStateException();
        }
        response.resetBuffer();
        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, false);
        ServletResponseDispatcherWrapper responseWrapper = wrapperResponse(response, false);
        servletContext.getPipeline().handleRequest(null);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, true);
        ServletResponseDispatcherWrapper responseWrapper = wrapperResponse(response, true);
        Object requestUri = null;
        Object contextPath = null;
        Object servletPath = null;
        Object pathInfo = null;
        Object queryString = null;
        if (named) {
            requestUri = request.getAttribute(INCLUDE_REQUEST_URI);
            contextPath = request.getAttribute(INCLUDE_CONTEXT_PATH);
            servletPath = request.getAttribute(INCLUDE_SERVLET_PATH);
            pathInfo = request.getAttribute(INCLUDE_PATH_INFO);
            queryString = request.getAttribute(INCLUDE_QUERY_STRING);
        } else {

        }
        HandlerContext handlerContext = new HandlerContext(null, null, servletContext);
        servletContext.getPipeline().handleRequest(handlerContext);
        try {

        } finally {
            if (!named) {
                request.setAttribute(INCLUDE_REQUEST_URI, requestUri);
                request.setAttribute(INCLUDE_CONTEXT_PATH, contextPath);
                request.setAttribute(INCLUDE_SERVLET_PATH, servletPath);
                request.setAttribute(INCLUDE_PATH_INFO, pathInfo);
                request.setAttribute(INCLUDE_QUERY_STRING, queryString);
            }
        }
    }

    private ServletRequestDispatcherWrapper wrapperRequest(final ServletRequest request, boolean included) {
        ServletRequest current = request;
        while (current instanceof ServletRequestWrapper) {
            current = ((ServletRequestWrapper) current).getRequest();
        }
        if (!(current instanceof HttpServletRequestImpl)) {
            throw new IllegalArgumentException("invalid request object: " + current);
        }
        return new ServletRequestDispatcherWrapper((HttpServletRequestImpl) current);
    }

    private ServletResponseDispatcherWrapper wrapperResponse(final ServletResponse response, boolean included) {
        ServletResponse current = response;
        while (current instanceof ServletResponseWrapper) {
            current = ((ServletResponseWrapper) current).getResponse();
        }
        if (!(current instanceof HttpServletResponseImpl)) {
            throw new IllegalArgumentException("invalid response object: " + current);
        }
        return new ServletResponseDispatcherWrapper((HttpServletResponseImpl) current, included);
    }
}
