/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.provider;

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public interface DispatcherProvider {
    RequestDispatcher getRequestDispatcher(ServletContextImpl servletContext, String path);

    RequestDispatcher getNamedDispatcher(ServletContextImpl servletContext, String name);

    RequestDispatcher getRequestDispatcher(HttpServletRequestImpl request, String path);

    void error(ServletContextImpl servletContext, String path, HttpServletRequest req, HttpServletResponse resp) throws IOException;
}
