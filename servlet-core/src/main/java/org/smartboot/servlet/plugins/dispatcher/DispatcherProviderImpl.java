/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DispatcherProviderImpl.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.dispatcher;

import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.provider.DispatcherProvider;

import javax.servlet.RequestDispatcher;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
class DispatcherProviderImpl implements DispatcherProvider {

    @Override
    public RequestDispatcher getRequestDispatcher(ServletContextImpl servletContext, String path) {
        if (path == null) {
            return null;
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("");
        }
        return new RequestDispatcherImpl(servletContext, null, path);
    }

    @Override
    public RequestDispatcher getNamedDispatcher(ServletContextImpl servletContext, String name) {
        System.out.println("getNamedDispatcher:" + name);
        ServletInfo servletInfo = servletContext.getDeploymentInfo().getServlets().get(name);
        if (servletInfo == null) {
            return null;
        }
        return new RequestDispatcherImpl(servletContext, servletInfo, null);
    }

    @Override
    public RequestDispatcher getRequestDispatcher(HttpServletRequestImpl request, String path) {
        if (path.startsWith("/")) {
            return getRequestDispatcher(request.getServletContext(), path);
        }
        if ("/".equals(request.getRequestURI())) {
            return getRequestDispatcher(request.getServletContext(), request.getRequestURI() + path);
        }
        int lastIndex = request.getRequestURI().lastIndexOf("/");
        //  "/doc"
        if (lastIndex == 0) {
            return getRequestDispatcher(request.getServletContext(), request.getRequestURI() + "/" + path);
        } else {
            // "/doc/"
            return getRequestDispatcher(request.getServletContext(), request.getRequestURI().substring(0, lastIndex) + "/" + path);
        }
    }
}
