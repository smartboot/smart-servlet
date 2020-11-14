/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DefaultFilter.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;


import org.smartboot.http.logging.RunLogger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DefaultFilter implements Filter {

    @Override
    public void init(FilterConfig config) throws ServletException {
        Enumeration<String> enumeration = config.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            RunLogger.getLogger().log(Level.INFO, "filter parameter name:" + name + " ,value:" + config.getInitParameter(name));
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

    }

    @Override
    public void destroy() {

    }
}
