/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DeploymentInfo.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import org.smartboot.http.utils.StringUtils;

import javax.servlet.Servlet;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
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
    private List<String> welcomeFiles = Collections.emptyList();
    private ClassLoader classLoader;
    private String contextPath;
    private String displayName;
    private URL contextUrl;
    private Servlet defaultServlet;
    /**
     * 会话超时时间
     */
    private int sessionTimeout;

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

    public void addServletContainerInitializer(final ServletContainerInitializer servletContainerInitializer) {
        if (servletContainerInitializer != null) {
            servletContainerInitializers.add(servletContainerInitializer);
        }
    }

    public List<ServletContainerInitializer> getServletContainerInitializers() {
        return servletContainerInitializers;
    }

    public void addServlet(final ServletInfo servlet) {
        servlets.put(servlet.getServletName(), servlet);
    }

    public Map<String, ServletInfo> getServlets() {
        return servlets;
    }

    public void addFilter(final FilterInfo filter) {
        filters.put(filter.getFilterName(), filter);
    }

    public void addEventListener(final String listenerInfo) {
        eventListeners.add(listenerInfo);
    }

    public List<String> getEventListeners() {
        return eventListeners;
    }

    public void addServletContextListener(ServletContextListener contextListener) {
        servletContextListeners.add(contextListener);
    }

    public List<ServletContextListener> getServletContextListeners() {
        return servletContextListeners;
    }

    public void addServletRequestListener(ServletRequestListener requestListener) {
        servletRequestListeners.add(requestListener);
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

    public void setContextPath(final String contextPath) {
        if (StringUtils.isBlank(contextPath)) {
            this.contextPath = "/";
        } else {
            this.contextPath = contextPath;
        }
    }

    public Map<String, String> getInitParameters() {
        return initParameters;
    }

    public void addInitParameter(final String name, final String value) {
        initParameters.put(name, value);
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public List<String> getWelcomeFiles() {
        return welcomeFiles;
    }

    public void setWelcomeFiles(List<String> welcomeFiles) {
        this.welcomeFiles = welcomeFiles;
    }

    public Servlet getDefaultServlet() {
        return defaultServlet;
    }

    public void setDefaultServlet(Servlet defaultServlet) {
        this.defaultServlet = defaultServlet;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
