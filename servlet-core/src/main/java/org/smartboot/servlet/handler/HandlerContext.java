/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.handler;

import org.smartboot.servlet.SmartHttpServletRequest;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * 请求处理上下文对象
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HandlerContext {
    /**
     * 请求
     */
    private final SmartHttpServletRequest originalRequest;
    /**
     * 响应
     */
    private ServletResponse response;
    /**
     * 匹配的Servlet上下文
     */
    private final ServletContextImpl servletContext;
    private final boolean namedDispatcher;

    /**
     * 匹配的Servlet处理器
     */
    private ServletInfo servletInfo;

    private ServletRequest request;

    public HandlerContext(SmartHttpServletRequest originalRequest, ServletResponse response, ServletContextImpl servletContext, boolean namedDispatcher) {
        this.originalRequest = originalRequest;
        this.response = response;
        this.servletContext = servletContext;
        this.namedDispatcher = namedDispatcher;
        this.request = originalRequest;
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }


    public SmartHttpServletRequest getOriginalRequest() {
        return originalRequest;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public void setResponse(ServletResponse response) {
        this.response = response;
    }

    public ServletRequest getRequest() {
        return request;
    }

    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    public ServletInfo getServletInfo() {
        return servletInfo;
    }

    public void setServletInfo(ServletInfo servletInfo) {
        this.servletInfo = servletInfo;
        this.originalRequest.setServletInfo(servletInfo);
    }

    public boolean isNamedDispatcher() {
        return namedDispatcher;
    }
}
