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

import javax.servlet.http.HttpServletResponse;

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
    private final SmartHttpServletRequest request;
    /**
     * 响应
     */
    private final HttpServletResponse response;
    /**
     * 匹配的Servlet上下文
     */
    private final ServletContextImpl servletContext;
    private final boolean namedDispatcher;

    /**
     * 匹配的Servlet处理器
     */
    private ServletInfo servletInfo;

    public HandlerContext(SmartHttpServletRequest request, HttpServletResponse response, ServletContextImpl servletContext, boolean namedDispatcher) {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
        this.namedDispatcher = namedDispatcher;
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }


    public SmartHttpServletRequest getRequest() {
        return request;
    }


    public HttpServletResponse getResponse() {
        return response;
    }


    public ServletInfo getServletInfo() {
        return servletInfo;
    }

    public void setServletInfo(ServletInfo servletInfo) {
        this.servletInfo = servletInfo;
        this.request.setServletInfo(servletInfo);
    }

    public boolean isNamedDispatcher() {
        return namedDispatcher;
    }
}
