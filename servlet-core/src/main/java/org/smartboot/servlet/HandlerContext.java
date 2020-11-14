/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: HandlerContext.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.Servlet;

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
    private final HttpServletRequestImpl request;
    /**
     * 响应
     */
    private final HttpServletResponseImpl response;
    /**
     * 匹配的Servlet上下文
     */
    private final ServletContextImpl servletContext;
    /**
     * 匹配的Servlet处理器
     */
    private Servlet servlet;

    public HandlerContext(HttpServletRequestImpl request, HttpServletResponseImpl response, ServletContextImpl servletContext) {
        this.request = request;
        this.response = response;
        this.servletContext = servletContext;
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }


    public HttpServletRequestImpl getRequest() {
        return request;
    }


    public HttpServletResponseImpl getResponse() {
        return response;
    }


    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }
}
