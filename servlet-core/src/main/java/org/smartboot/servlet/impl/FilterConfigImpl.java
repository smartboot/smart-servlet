/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.conf.FilterInfo;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import java.util.Collections;
import java.util.Enumeration;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
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
