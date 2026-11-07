/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
