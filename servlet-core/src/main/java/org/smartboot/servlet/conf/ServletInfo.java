/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletInfo.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import org.smartboot.servlet.enums.ServletMappingTypeEnum;

import javax.servlet.Servlet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletInfo {
    private final List<ServletMappingInfo> mappings = new ArrayList<>();
    private final Map<String, String> initParams = new HashMap<>();
    private String servletClass;
    private String servletName;
    private int loadOnStartup;
    private Servlet servlet;

    private boolean dynamic;

    public ServletInfo() {
    }

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    public List<ServletMappingInfo> getMappings() {
        return Collections.unmodifiableList(mappings);
    }

    public ServletInfo addInitParam(final String name, final String value) {
        initParams.put(name, value);
        return this;
    }

    public ServletInfo addMapping(final String mapping) {
        if (!mapping.contains("*")) {
            if (!mapping.startsWith("/")) {
                throw new IllegalArgumentException("invalid mapping: " + mapping);
            }
            mappings.add(new ServletMappingInfo(mapping, ServletMappingTypeEnum.EXACT_MATCH));
        } else if (mapping.startsWith("*.")) {
            mappings.add(new ServletMappingInfo(mapping, ServletMappingTypeEnum.EXTENSION_MATCH));
        } else if (mapping.startsWith("/") && mapping.endsWith("*")) {
            mappings.add(new ServletMappingInfo(mapping, ServletMappingTypeEnum.PREFIX_MATCH));
        } else {
            throw new UnsupportedOperationException(mapping);
        }
        return this;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }
}
