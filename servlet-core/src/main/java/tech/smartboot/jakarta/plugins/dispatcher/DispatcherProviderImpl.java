/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.jakarta.plugins.dispatcher;

import org.smartboot.http.common.utils.StringUtils;
import tech.smartboot.jakarta.conf.ServletInfo;
import tech.smartboot.jakarta.impl.HttpServletRequestImpl;
import tech.smartboot.jakarta.impl.ServletContextImpl;
import tech.smartboot.jakarta.provider.DispatcherProvider;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
class DispatcherProviderImpl implements DispatcherProvider {

    @Override
    public RequestDispatcherImpl getRequestDispatcher(ServletContextImpl servletContext, String path) {
        if (path == null) {
            return null;
        }
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("");
        }
        return new RequestDispatcherImpl(servletContext, null, StringUtils.isBlank(servletContext.getContextPath()) ? path : servletContext.getContextPath() + path);
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
        //If the path begins with a "/" it is interpreted as relative to the current context root
        if (path.startsWith("/")) {
            return getRequestDispatcher(request.getServletContext(), path);
        }
        int lastIndex = request.getRequestURI().lastIndexOf("/");
        if (lastIndex != -1) {
            return getRequestDispatcher(request.getServletContext(), request.getRequestURI().substring(request.getContextPath().length(), lastIndex + 1) + path);
        } else {
            return null;
        }
    }

    @Override
    public void error(ServletContextImpl servletContext, String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        RequestDispatcherImpl requestDispatcher = getRequestDispatcher(servletContext, path);
        try {
            requestDispatcher.forward(req, resp, false, DispatcherType.ERROR);
        } catch (ServletException e) {
            throw new IOException(e);
        }
    }
}
