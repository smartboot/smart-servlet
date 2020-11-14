/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: FilterConfigImpl.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.conf.FilterInfo;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;


public class FilterConfigImpl implements FilterConfig {

    private final FilterInfo filterInfo;
    private final ServletContext servletContext;

    public FilterConfigImpl(final FilterInfo filterInfo, final ServletContext servletContext) {
        this.filterInfo = filterInfo;
        this.servletContext = servletContext;
    }

    @Override
    public String getFilterName() {
        return filterInfo.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(final String name) {
        return filterInfo.getInitParams().get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(filterInfo.getInitParams().keySet());
    }
}
