/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DeploymentInfo.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import org.smartboot.http.utils.StringUtils;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 运行环境部署配置
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DeploymentInfo {
    private final Map<String, ServletInfo> servlets = new HashMap<>();
    private final Map<String, FilterInfo> filters = new HashMap<>();
    private final List<FilterMappingInfo> filterMappings = new ArrayList<>();
    private final Map<String, String> initParameters = new HashMap<>();
    private final List<String> eventListeners = new ArrayList<>();
    private final List<ServletContextListener> servletContextListeners = new ArrayList<>();
    private final List<ServletRequestListener> servletRequestListeners = new ArrayList<>();
    private final List<ServletContainerInitializer> servletContainerInitializers = new ArrayList<>();
    private ClassLoader classLoader;
    private String contextPath;
    private String displayName;
    private URL contextUrl;


    public URL getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(URL contextUrl) {
        this.contextUrl = contextUrl;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public DeploymentInfo addServletContainerInitializer(final ServletContainerInitializer servletContainerInitializer) {
        if (servletContainerInitializer != null) {
            servletContainerInitializers.add(servletContainerInitializer);
        }
        return this;
    }

    public List<ServletContainerInitializer> getServletContainerInitializers() {
        return servletContainerInitializers;
    }

    public DeploymentInfo addServlet(final ServletInfo servlet) {
        servlets.put(servlet.getServletName(), servlet);
        return this;
    }

    public Map<String, ServletInfo> getServlets() {
        return servlets;
    }

    public DeploymentInfo addFilter(final FilterInfo filter) {
        filters.put(filter.getFilterName(), filter);
        return this;
    }

    public DeploymentInfo addEventListener(final String listenerInfo) {
        eventListeners.add(listenerInfo);
        return this;
    }

    public List<String> getEventListeners() {
        return eventListeners;
    }

    public DeploymentInfo addServletContextListener(ServletContextListener contextListener) {
        servletContextListeners.add(contextListener);
        return this;
    }

    public DeploymentInfo addServletRequestListener(ServletRequestListener requestListener) {
        servletRequestListeners.add(requestListener);
        return this;
    }

    public List<ServletRequestListener> getServletRequestListeners() {
        return servletRequestListeners;
    }

    public Map<String, FilterInfo> getFilters() {
        return filters;
    }

    public void addFilterMapping(FilterMappingInfo filterMappingInfo) {
        filterMappings.add(filterMappingInfo);
    }

    public List<FilterMappingInfo> getFilterMappings() {
        return filterMappings;
    }

    public String getContextPath() {
        return contextPath;
    }

    public DeploymentInfo setContextPath(final String contextPath) {
        if (StringUtils.isBlank(contextPath)) {
            this.contextPath = "/";
        } else {
            this.contextPath = contextPath;
        }
        return this;
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }

    public DeploymentInfo addInitParameter(final String name, final String value) {
        initParameters.put(name, value);
        return this;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }
}
