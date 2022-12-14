/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletRequestDispatcherWrapper.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.dispatcher;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.servlet.SmartHttpServletRequest;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/20
 */
class ServletRequestDispatcherWrapper extends HttpServletRequestWrapper implements SmartHttpServletRequest {
    private final HttpServletRequestImpl request;
    private final DispatcherType dispatcherType;
    private final boolean named;
    private HttpServletResponseImpl response;
    private String servletPath;
    private int servletPathStart;
    private int servletPathEnd;
    private String pathInfo;
    private int pathInfoStart;
    private int pathInfoEnd;
    private String requestUri;
    private Map<String, String[]> paramaters;

    public ServletRequestDispatcherWrapper(HttpServletRequestImpl request, DispatcherType dispatcherType, boolean named) {
        super(request);
        this.request = request;
        this.dispatcherType = dispatcherType;
        this.named = named;
    }

    @Override
    public String getParameter(String name) {
        if (paramaters == null) {
            return null;
        }
        String[] values = paramaters.get(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return paramaters == null ? Collections.emptyMap() : paramaters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (paramaters == null) {
            return null;
        }
        return Collections.enumeration(paramaters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (paramaters == null) {
            return null;
        }
        return paramaters.get(name);
    }

    @Override
    public DispatcherType getDispatcherType() {
        return dispatcherType;
    }

    @Override
    public HttpServletRequestImpl getRequest() {
        return request;
    }

    @Override
    public String getRequestURI() {
        return named ? super.getRequestURI() : this.requestUri;
    }

    @Override
    public void setRequestUri(String requestURI) {
        this.requestUri = requestURI;
    }

    @Override
    public String getPathInfo() {
        if (named) {
            return super.getPathInfo();
        }
        if (pathInfoStart < 0) {
            return null;
        }
        if (pathInfo != null) {
            return pathInfo;
        }
        pathInfo = getRequestURI().substring(pathInfoStart, pathInfoEnd);
        return pathInfo;
    }

    @Override
    public void setPathInfo(int start, int end) {
        this.pathInfoStart = start;
        this.pathInfoEnd = end;
    }

    @Override
    public void setServletInfo(ServletInfo servletInfo) {
        this.request.setServletInfo(servletInfo);
    }

    @Override
    public String getServletPath() {
        if (named) {
            return super.getServletPath();
        }
        if (servletPathStart < 0) {
            return null;
        }
        if (servletPath != null) {
            return servletPath;
        }
        servletPath = getRequestURI().substring(servletPathStart, servletPathEnd);
        return servletPath;
    }

    @Override
    public void setServletPath(int start, int end) {
        this.servletPathStart = start;
        this.servletPathEnd = end;
    }

    public void setParamaters(Map<String, String[]> paramaters) {
        this.paramaters = paramaters;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (dispatcherType == DispatcherType.INCLUDE && response.containsHeader(HeaderNameEnum.COOKIE.getName())) {
            throw new IllegalStateException();
        }
        return super.getSession(create);
    }

    @Override
    public HttpSession getSession() {
        if (dispatcherType == DispatcherType.INCLUDE && response.containsHeader(HeaderNameEnum.COOKIE.getName())) {
            throw new IllegalStateException();
        }
        return super.getSession();
    }

    public void setResponse(HttpServletResponseImpl response) {
        this.response = response;
    }
}
