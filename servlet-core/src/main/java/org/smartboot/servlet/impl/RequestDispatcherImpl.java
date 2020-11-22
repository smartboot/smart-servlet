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

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
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
    private Servlet dispatcherServlet;
    private String dispatcherURL;

    public RequestDispatcherImpl(ServletContextImpl servletContext, Servlet dispatcherServlet, String dispatcherURL) {
        if (dispatcherServlet == null && dispatcherURL == null) {
            throw new IllegalArgumentException();
        }
        if (dispatcherServlet != null && dispatcherURL != null) {
            throw new IllegalArgumentException();
        }
        this.servletContext = servletContext;
        this.dispatcherServlet = dispatcherServlet;
        this.dispatcherURL = dispatcherURL;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        if (response.isCommitted()) {
            throw new IllegalStateException();
        }
        response.resetBuffer();
        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, false);
        ServletResponseDispatcherWrapper responseWrapper = wrapperResponse(response, false);
        HttpServletRequestImpl requestImpl = requestWrapper.getRequest();
        Object requestUri = requestImpl.getRequestURI();
        Object contextPath = requestImpl.getContextPath();
        Object servletPath = requestImpl.getServletPath();
        Object pathInfo = requestImpl.getPathInfo();
        Object queryString = requestImpl.getQueryString();
        if (requestUri != null) {
            requestWrapper.setAttribute(FORWARD_REQUEST_URI, requestUri);
        }
        if (contextPath != null) {
            requestWrapper.setAttribute(FORWARD_CONTEXT_PATH, contextPath);
        }
        if (servletPath != null) {
            requestWrapper.setAttribute(FORWARD_SERVLET_PATH, servletPath);
        }
        if (pathInfo != null) {
            requestWrapper.setAttribute(FORWARD_PATH_INFO, pathInfo);
        }
        if (queryString != null) {
            requestWrapper.setAttribute(FORWARD_QUERY_STRING, queryString);
        }
        HandlerContext handlerContext = new HandlerContext(requestWrapper, responseWrapper, servletContext);
        if (dispatcherServlet != null) {
            handlerContext.setServlet(dispatcherServlet);
        }
        servletContext.getPipeline().handleRequest(handlerContext);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, true);
        ServletResponseDispatcherWrapper responseWrapper = wrapperResponse(response, true);
        HttpServletRequestImpl requestImpl = requestWrapper.getRequest();
        Object requestUri = requestImpl.getRequestURI();
        Object contextPath = requestImpl.getContextPath();
        Object servletPath = requestImpl.getServletPath();
        Object pathInfo = requestImpl.getPathInfo();
        Object queryString = requestImpl.getQueryString();
        if (requestUri != null) {
            requestWrapper.setAttribute(INCLUDE_REQUEST_URI, requestUri);
        }
        if (contextPath != null) {
            requestWrapper.setAttribute(INCLUDE_CONTEXT_PATH, contextPath);
        }
        if (servletPath != null) {
            requestWrapper.setAttribute(INCLUDE_PATH_INFO, servletPath);
        }
        if (pathInfo != null) {
            requestWrapper.setAttribute(INCLUDE_PATH_INFO, pathInfo);
        }
        if (queryString != null) {
            requestWrapper.setAttribute(INCLUDE_QUERY_STRING, queryString);
        }
        HandlerContext handlerContext = new HandlerContext(requestWrapper, responseWrapper, servletContext);
        if (dispatcherServlet != null) {
            handlerContext.setServlet(dispatcherServlet);
        }
        servletContext.getPipeline().handleRequest(handlerContext);
    }

    private ServletRequestDispatcherWrapper wrapperRequest(final ServletRequest request, boolean included) {
        ServletRequest current = request;
        while (current instanceof ServletRequestWrapper) {
            current = ((ServletRequestWrapper) current).getRequest();
        }
        if (!(current instanceof HttpServletRequestImpl)) {
            throw new IllegalArgumentException("invalid request object: " + current);
        }
        return new ServletRequestDispatcherWrapper((HttpServletRequestImpl) current, included ? DispatcherType.INCLUDE : DispatcherType.FORWARD);
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
