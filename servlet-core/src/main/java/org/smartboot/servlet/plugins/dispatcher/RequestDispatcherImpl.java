/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.plugins.dispatcher;

import org.smartboot.http.common.utils.HttpUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.handler.HandlerContext;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestWrapper;
import javax.servlet.ServletResponse;
import javax.servlet.ServletResponseWrapper;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 《Servlet3.1规范中文版》第9章 分派请求
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
class RequestDispatcherImpl implements RequestDispatcher {
    private final ServletContextImpl servletContext;
    private final boolean named;
    private final ServletInfo dispatcherServlet;
    private final String dispatcherURL;

    public RequestDispatcherImpl(ServletContextImpl servletContext, ServletInfo dispatcherServlet, String dispatcherURL) {
        if (dispatcherServlet == null && dispatcherURL == null) {
            throw new IllegalArgumentException();
        }
        if (dispatcherServlet != null && dispatcherURL != null) {
            throw new IllegalArgumentException();
        }
        this.servletContext = servletContext;
        this.dispatcherServlet = dispatcherServlet;
        this.dispatcherURL = dispatcherURL;
        this.named = dispatcherServlet != null;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        forward(request, response, named, DispatcherType.FORWARD);
    }

    public void forward(ServletRequest request, ServletResponse response, boolean named, DispatcherType dispatcherType) throws ServletException, IOException {
        if (response.isCommitted()) {
            throw new IllegalStateException();
        }
        response.resetBuffer();
        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, dispatcherType);
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

        //《Servlet3.1规范中文版》9.4 forward 方法
        //request 对象暴露给目标 servlet 的路径元素(path elements)必须反映获得 RequestDispatcher 使用的路径。
        // 唯一例外的是，如果 RequestDispatcher 是通过 getNamedDispatcher 方法获得。这种情况下，request 对象的路径元素必须反映这些原始请求。
        if (named) {
            requestWrapper.setRequestUri(requestWrapper.getRequest().getRequestURI());
            Map<String, String[]> parameters = new HashMap<>();
            HttpUtils.decodeParamString(requestWrapper.getQueryString(), parameters);
            requestWrapper.setParameters(parameters);
        } else {
            String[] array = StringUtils.split(dispatcherURL, "?");
            requestWrapper.setRequestUri(array[0]);
            Map<String, String[]> parameters = new HashMap<>();
            if (array.length > 1) {
                HttpUtils.decodeParamString(array[1], parameters);
                requestWrapper.setParameters(parameters);
                requestWrapper.setQueryString(array[1]);
            }
        }


        HandlerContext handlerContext = new HandlerContext(requestWrapper, responseWrapper, servletContext, named);
        if (dispatcherServlet != null) {
            handlerContext.setServletInfo(dispatcherServlet);
        }
        servletContext.getPipeline().handleRequest(handlerContext);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, DispatcherType.INCLUDE);
        ServletResponseDispatcherWrapper responseWrapper = wrapperResponse(response, true);
        HttpServletRequestImpl requestImpl = requestWrapper.getRequest();

        //《Servlet3.1规范中文版》9.3.1 包含(include)的请求参数
        //这些属性可以通过包含的 servlet 的 request 对象的 getAttribute 方法访问，
        // 它们的值必须分别与被包含 servlet 的请求 RUI、上下文路径、servlet 路径、路径信息、查询字符串相等。
        // 如果包含后续请求，那么这些属性 会被后面包含请求的相应属性值替换。
        //如果通过 getNamedDispatcher 方法获得包含的 servlet，那么不能设置这些属性。

        String requestUri = requestImpl.getRequestURI();
        Object contextPath = requestImpl.getContextPath();
        Object servletPath = requestImpl.getServletPath();
        Object pathInfo = requestImpl.getPathInfo();
        Object queryString = requestImpl.getQueryString();
        if (!named) {
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
            String[] array = StringUtils.split(dispatcherURL, "?");
            requestWrapper.setRequestUri(array[0]);
        }

        HandlerContext handlerContext = new HandlerContext(requestWrapper, responseWrapper, servletContext, named);
        if (dispatcherServlet != null) {
            handlerContext.setServletInfo(dispatcherServlet);
        }
        servletContext.getPipeline().handleRequest(handlerContext);
    }

    private ServletRequestDispatcherWrapper wrapperRequest(final ServletRequest request, DispatcherType dispatcherType) {
        ServletRequest current = request;
        while (current instanceof ServletRequestWrapper) {
            current = ((ServletRequestWrapper) current).getRequest();
        }
        if (!(current instanceof HttpServletRequestImpl)) {
            throw new IllegalArgumentException("invalid request object: " + current);
        }
        return new ServletRequestDispatcherWrapper((HttpServletRequestImpl) current, dispatcherType, named);
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
