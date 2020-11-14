/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletConfigImpl.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.conf.ServletInfo;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;
/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class ServletConfigImpl implements ServletConfig {

    private final ServletInfo servletInfo;
    private final ServletContext servletContext;

    public ServletConfigImpl(final ServletInfo servletInfo, final ServletContext servletContext) {
        this.servletInfo = servletInfo;
        this.servletContext = servletContext;
    }

    @Override
    public String getServletName() {
        return servletInfo.getServletName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(final String name) {
        return servletInfo.getInitParams().get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(servletInfo.getInitParams().keySet());
    }
}