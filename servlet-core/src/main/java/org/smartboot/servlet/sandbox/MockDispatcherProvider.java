/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MockDispatcherProvider.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.provider.DispatcherProvider;

import javax.servlet.RequestDispatcher;

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
}
