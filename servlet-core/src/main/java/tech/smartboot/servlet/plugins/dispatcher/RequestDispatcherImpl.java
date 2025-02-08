/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.dispatcher;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletRequestWrapper;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletResponseWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.smartboot.feat.core.common.utils.HttpUtils;
import tech.smartboot.feat.core.common.utils.StringUtils;
import tech.smartboot.servlet.SmartHttpServletRequest;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.handler.HandlerContext;
import tech.smartboot.servlet.impl.HttpServletResponseImpl;
import tech.smartboot.servlet.impl.ServletContextImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
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

    public RequestDispatcherImpl(ServletContextImpl servletContext, ServletInfo dispatcherServlet) {
        if (dispatcherServlet == null) {
            throw new IllegalArgumentException();
        }
        this.servletContext = servletContext;
        this.dispatcherServlet = dispatcherServlet;
        this.dispatcherURL = null;
        this.named = true;
    }

    public RequestDispatcherImpl(ServletContextImpl servletContext, String dispatcherURL) {
        if (dispatcherURL == null) {
            throw new IllegalArgumentException();
        }
        this.servletContext = servletContext;
        this.dispatcherServlet = null;
        this.dispatcherURL = dispatcherURL;
        this.named = false;
    }

    @Override
    public void forward(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        forward(request, response, named, DispatcherType.FORWARD, null, null, null);
    }

    public void forward(ServletRequest request, ServletResponse response, boolean named, DispatcherType dispatcherType, Throwable throwable, String errorServletName, String errorMessage) throws ServletException, IOException {
        //只有在没有输出提交到向客户端时，才能通过正在被调用的 servlet调用。
        // 如果 response 缓冲区中存在尚未提交的输出数据，这些数据内容必须在目标 servlet 的 service 方法调用前清除。
        // 如果 response 已经提交，必须抛出一个 IllegalStateException 异常。
        if (response.isCommitted()) {
            throw new IllegalStateException();
        }
        response.resetBuffer();

        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, dispatcherType);
        ServletResponseDispatcherWrapper responseWrapper = wrapperResponse(response, false);
        HttpServletRequest requestImpl = requestWrapper.getRequest();
        HandlerContext handlerContext = new HandlerContext(requestWrapper, responseWrapper, servletContext, named);


        if (dispatcherType == DispatcherType.ERROR) {
            if (throwable != null) {
                requestWrapper.setAttribute(ERROR_EXCEPTION, throwable);
                requestWrapper.setAttribute(ERROR_EXCEPTION_TYPE, throwable.getClass());
            }
            requestWrapper.setAttribute(ERROR_MESSAGE, errorMessage);
            requestWrapper.setAttribute(ERROR_STATUS_CODE, ((HttpServletResponse) response).getStatus());
            requestWrapper.setAttribute(ERROR_REQUEST_URI, requestImpl.getRequestURI());
            requestWrapper.setAttribute(ERROR_SERVLET_NAME, errorServletName);
            responseWrapper.setStatus(((HttpServletResponse) response).getStatus());
        }

        //《Servlet3.1规范中文版》9.4 forward 方法
        //request 对象暴露给目标 servlet 的路径元素(path elements)必须反映获得 RequestDispatcher 使用的路径。
        // 唯一例外的是，如果 RequestDispatcher 是通过 getNamedDispatcher 方法获得。这种情况下，request 对象的路径元素必须反映这些原始请求。
        if (named) {
            requestWrapper.setRequestUri(requestWrapper.getRequest().getRequestURI());
            Map<String, String[]> parameters = new HashMap<>();
            HttpUtils.decodeParamString(requestWrapper.getQueryString(), parameters);
            requestWrapper.setParameters(parameters);
            handlerContext.setServletInfo(dispatcherServlet);
        } else {
            requestWrapper.setAttribute(FORWARD_REQUEST_URI, requestImpl.getRequestURI());
            requestWrapper.setAttribute(FORWARD_CONTEXT_PATH, requestImpl.getContextPath());
            requestWrapper.setAttribute(FORWARD_SERVLET_PATH, requestImpl.getServletPath());
            requestWrapper.setAttribute(FORWARD_PATH_INFO, requestImpl.getPathInfo());
            requestWrapper.setAttribute(FORWARD_QUERY_STRING, requestImpl.getQueryString());
            String[] array = StringUtils.split(dispatcherURL, "?");
            requestWrapper.setRequestUri(array[0]);
            ServletMappingInfo servletMappingInfo = servletContext.getRuntime().getMappingProvider().matchWithContextPath(array[0]);
            requestWrapper.setServletMappingInfo(servletMappingInfo);
            handlerContext.setServletInfo(servletMappingInfo.getServletInfo());
            Map<String, String[]> parameters = new HashMap<>();
            if (array.length > 1) {
                HttpUtils.decodeParamString(array[1], parameters);
                requestWrapper.setParameters(parameters);
                requestWrapper.setQueryString(array[1]);
            }
        }
        servletContext.getPipeline().handleRequest(handlerContext);
    }

    @Override
    public void include(ServletRequest request, ServletResponse response) throws ServletException, IOException {
        ServletRequestDispatcherWrapper requestWrapper = wrapperRequest(request, DispatcherType.INCLUDE);
        ServletResponseDispatcherWrapper responseWrapper = wrapperResponse(response, true);
        HandlerContext handlerContext = new HandlerContext(requestWrapper, responseWrapper, servletContext, named);
        HttpServletRequest requestImpl = requestWrapper.getRequest();


        //《Servlet3.1规范中文版》9.3.1 包含(include)的请求参数
        //这些属性可以通过包含的 servlet 的 request 对象的 getAttribute 方法访问，
        // 它们的值必须分别与被包含 servlet 的请求 RUI、上下文路径、servlet 路径、路径信息、查询字符串相等。
        // 如果包含后续请求，那么这些属性 会被后面包含请求的相应属性值替换。
        //如果通过 getNamedDispatcher 方法获得包含的 servlet，那么不能设置这些属性。
        Object requestUri = null;
        Object contextPath = null;
        Object servletPath = null;
        Object pathInfo = null;
        Object queryString = null;

//        String requestUri = requestImpl.getRequestURI();
//        Object contextPath = requestImpl.getContextPath();
//        Object servletPath = requestImpl.getServletPath();
//        Object pathInfo = requestImpl.getPathInfo();
//        Object queryString = requestImpl.getQueryString();
        if (!named) {
            requestUri = request.getAttribute(INCLUDE_REQUEST_URI);
            contextPath = request.getAttribute(INCLUDE_CONTEXT_PATH);
            servletPath = request.getAttribute(INCLUDE_SERVLET_PATH);
            pathInfo = request.getAttribute(INCLUDE_PATH_INFO);
            queryString = request.getAttribute(INCLUDE_QUERY_STRING);

            requestWrapper.setAttribute(INCLUDE_CONTEXT_PATH, requestImpl.getContextPath());
            requestWrapper.setAttribute(INCLUDE_PATH_INFO, requestImpl.getPathInfo());
            requestWrapper.setAttribute(INCLUDE_MAPPING, requestImpl.getHttpServletMapping());

            String[] array = StringUtils.split(dispatcherURL, "?");
            ServletMappingInfo servletMappingInfo = servletContext.getRuntime().getMappingProvider().matchWithContextPath(array[0]);
            handlerContext.setServletInfo(servletMappingInfo.getServletInfo());
            requestWrapper.setAttribute(INCLUDE_REQUEST_URI, array[0]);
            requestWrapper.setAttribute(INCLUDE_SERVLET_PATH, getServerPath(servletMappingInfo, array[0]));
            if (array.length > 1) {
                Map<String, String[]> parameters = new HashMap<>();
                HttpUtils.decodeParamString(array[1], parameters);
                mergeParameter(requestImpl.getParameterMap(), parameters);
                requestWrapper.setParameters(parameters);
                requestWrapper.setAttribute(INCLUDE_QUERY_STRING, array[1]);
            }
        } else {
            handlerContext.setServletInfo(dispatcherServlet);
            requestWrapper.setParameters(request.getParameterMap());
        }
        try {
            servletContext.getPipeline().handleRequest(handlerContext);
        } finally {
            if (!named) {
                requestImpl.setAttribute(INCLUDE_REQUEST_URI, requestUri);
                requestImpl.setAttribute(INCLUDE_CONTEXT_PATH, contextPath);
                requestImpl.setAttribute(INCLUDE_SERVLET_PATH, servletPath);
                requestImpl.setAttribute(INCLUDE_PATH_INFO, pathInfo);
                requestImpl.setAttribute(INCLUDE_QUERY_STRING, queryString);
            }
        }
    }

    private String getServerPath(ServletMappingInfo servletMappingInfo, String requestUri) {
        switch (servletMappingInfo.getMappingMatch()) {
            case EXACT -> {
                return servletMappingInfo.getUrlPattern();
            }
            case EXTENSION -> {
                return requestUri.substring(servletContext.getContextPath().length());
            }
            case PATH -> {
                return servletMappingInfo.getUrlPattern().substring(0, servletMappingInfo.getUrlPattern().length() - 2);
            }
        }
        return null;
    }

    private ServletRequestDispatcherWrapper wrapperRequest(final ServletRequest request, DispatcherType dispatcherType) {
        ServletRequest current = request;
        if (named) {
            while (current instanceof ServletRequestWrapper && !(current instanceof SmartHttpServletRequest)) {
                current = ((ServletRequestWrapper) current).getRequest();
            }
        } else {
            while (current instanceof ServletRequestWrapper) {
                current = ((ServletRequestWrapper) current).getRequest();
            }
        }

        if (!(current instanceof SmartHttpServletRequest)) {
            throw new IllegalArgumentException("invalid request object: " + current);
        }
        return new ServletRequestDispatcherWrapper((SmartHttpServletRequest) current, dispatcherType, named);
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

    private void mergeParameter(Map<String, String[]> oldParams, Map<String, String[]> newParams) {
        for (Map.Entry<String, String[]> entry : oldParams.entrySet()) {
            String[] values = newParams.get(entry.getKey());
            if (values == null) {
                newParams.put(entry.getKey(), entry.getValue());
            } else {
                List<String> list = new ArrayList<>(Arrays.asList(values));
                // merge values new params first
                for (String v : entry.getValue()) {
                    if (!list.contains(v)) {
                        list.add(v);
                    }
                }
                newParams.put(entry.getKey(), list.toArray(entry.getValue()));
            }
        }
    }
}
