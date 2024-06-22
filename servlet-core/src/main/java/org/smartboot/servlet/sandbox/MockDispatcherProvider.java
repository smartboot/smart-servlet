/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.provider.DispatcherProvider;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
class MockDispatcherProvider implements DispatcherProvider {
    @Override
    public RequestDispatcher getRequestDispatcher(ServletContextImpl servletContext, String path) {
        throw new PluginException("Please install the [dispatcher] plugin to enable the [getRequestDispatcher] function");
    }

    @Override
    public RequestDispatcher getNamedDispatcher(ServletContextImpl servletContext, String name) {
        throw new PluginException("Please install the [dispatcher] plugin to enable the [getNamedDispatcher] function");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(HttpServletRequestImpl servletContext, String path) {
        throw new PluginException("Please install the [dispatcher] plugin to enable the [getRequestDispatcher] function");
    }

    @Override
    public void error(ServletContextImpl servletContext, String path,HttpServletRequest req, HttpServletResponse resp) {
    }
}
