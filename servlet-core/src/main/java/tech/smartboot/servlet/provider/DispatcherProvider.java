/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.provider;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.impl.ServletContextImpl;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public interface DispatcherProvider {
    RequestDispatcher getRequestDispatcher(ServletContextImpl servletContext, String path);

    RequestDispatcher getNamedDispatcher(ServletContextImpl servletContext, String name);

    RequestDispatcher getRequestDispatcher(HttpServletRequestImpl request, String path);

    default void error(ServletContextImpl servletContext, String path, HttpServletRequest req, HttpServletResponse resp) throws IOException {
        error(servletContext, path, req, resp, null, null, null);
    }

    default void error(ServletContextImpl servletContext, String path, HttpServletRequest req, HttpServletResponse resp, Throwable throwable, String errorServletName, String errorMessage) throws IOException {
    }
}
