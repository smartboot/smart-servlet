/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartServletRequestWrapper.java
 * Date: 2020-11-20
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.SmartHttpServletRequest;

import javax.servlet.DispatcherType;
import javax.servlet.http.HttpServletRequestWrapper;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/20
 */
public class ServletRequestDispatcherWrapper extends HttpServletRequestWrapper implements SmartHttpServletRequest {
    private final HttpServletRequestImpl request;

    private final DispatcherType dispatcherType;
    private final boolean named;
    private String servletPath;
    private String pathInfo;
    private String requestUri;

    public ServletRequestDispatcherWrapper(HttpServletRequestImpl request, DispatcherType dispatcherType, boolean named) {
        super(request);
        this.request = request;
        this.dispatcherType = dispatcherType;
        this.named = named;
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
    public String getPathInfo() {
        return named ? super.getPathInfo() : this.pathInfo;
    }

    @Override
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getServletPath() {
        return named ? super.getServletPath() : this.servletPath;
    }

    @Override
    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public void setRequestURI(String requestURI) {
        this.requestUri = requestURI;
    }
}
