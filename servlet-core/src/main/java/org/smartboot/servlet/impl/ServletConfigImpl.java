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