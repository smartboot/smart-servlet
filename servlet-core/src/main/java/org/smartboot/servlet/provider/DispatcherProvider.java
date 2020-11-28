/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DispatcherProvider.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.provider;

import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.RequestDispatcher;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public interface DispatcherProvider {
    RequestDispatcher getRequestDispatcher(ServletContextImpl servletContext, String path);

    public RequestDispatcher getNamedDispatcher(ServletContextImpl servletContext, String name);
}
