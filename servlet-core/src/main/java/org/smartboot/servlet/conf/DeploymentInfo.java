/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.conf;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.annotation.HandlesTypes;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionListener;
import org.smartboot.servlet.AnnotationsLoader;

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

    private final Map<Integer, ErrorPageInfo> errorStatusPages = new HashMap<>();
    private final Map<String, ErrorPageInfo> errorPages = new HashMap<>();
    private final Map<String, FilterInfo> filters = new HashMap<>();
    private final List<FilterMappingInfo> filterMappings = new ArrayList<>();
    private final Map<String, String> initParameters = new HashMap<>();
    private List<String> eventListeners = new ArrayList<>();
    private List<ServletContainerInitializerInfo> servletContainerInitializers = new ArrayList<>();
    private List<ServletContextAttributeListener> servletContextAttributeListeners = new ArrayList<>();
    private List<ServletContextListener> servletContextListeners = new ArrayList<>();
    private List<HttpSessionListener> httpSessionListeners = new ArrayList<>();
    private List<ServletRequestListener> servletRequestListeners = new ArrayList<>();

    private List<HttpSessionAttributeListener> sessionAttributeListeners = new ArrayList<>();
    private List<ServletRequestAttributeListener> requestAttributeListeners = new ArrayList<>();
    private List<String> welcomeFiles = Collections.emptyList();
    private ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
    private String displayName;
    private URL contextUrl;

    private AnnotationsLoader annotationsLoader;
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
        HandlesTypes handlesTypesAnnotation = servletContainerInitializer.getClass().getDeclaredAnnotation(HandlesTypes.class);
        if (handlesTypesAnnotation != null) {
            for (Class<?> c : handlesTypesAnnotation.value()) {
                annotationsLoader.add(servletContainerInitializer, c);
            }
        } else {
            servletContainerInitializers.add(new ServletContainerInitializerInfo(servletContainerInitializer, null));
        }
    }

    public AnnotationsLoader getHandlesTypesLoader() {
        return annotationsLoader;
    }

    public void setHandlesTypesLoader(AnnotationsLoader annotationsLoader) {
        this.annotationsLoader = annotationsLoader;
    }

    public List<ServletContainerInitializerInfo> getServletContainerInitializers() {
        return servletContainerInitializers;
    }

    public void addServlet(final ServletInfo servlet) {
        servlets.put(servlet.getServletName(), servlet);
    }

    public Map<String, ServletInfo> getServlets() {
        return servlets;
    }

    public void addErrorPage(final ErrorPageInfo servlet) {
        if (servlet.getErrorCode() != null) {
            errorStatusPages.put(servlet.getErrorCode(), servlet);
        }
        if (servlet.getExceptionType() != null) {
            errorPages.put(servlet.getExceptionType(), servlet);
        }

    }

    public String getErrorPageLocation(int errorCode) {
        ErrorPageInfo errorPage = errorStatusPages.get(errorCode);
        return errorPage == null ? null : errorPage.getLocation();
    }

    public String getErrorPageLocation(Exception exception) {
        ErrorPageInfo errorPage = errorPages.get(exception.getClass().getName());
        return errorPage == null ? null : errorPage.getLocation();
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

    public void amazing() {
        eventListeners.clear();
        eventListeners = null;
        if (servletContainerInitializers.isEmpty()) {
            servletContainerInitializers = Collections.emptyList();
        }
        if (servletContextAttributeListeners.isEmpty()) {
            servletContextAttributeListeners = Collections.emptyList();
        }
        if (servletContextListeners.isEmpty()) {
            servletContextListeners = Collections.emptyList();
        }
        if (httpSessionListeners.isEmpty()) {
            httpSessionListeners = Collections.emptyList();
        }
        if (servletRequestListeners.isEmpty()) {
            servletRequestListeners = Collections.emptyList();
        }
        if (sessionAttributeListeners.isEmpty()) {
            sessionAttributeListeners = Collections.emptyList();
        }
        if (requestAttributeListeners.isEmpty()) {
            requestAttributeListeners = Collections.emptyList();
        }
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

    public void addSessionAttributeListener(HttpSessionAttributeListener requestListener) {
        sessionAttributeListeners.add(requestListener);
    }

    public void addRequestAttributeListener(ServletRequestAttributeListener requestListener) {
        requestAttributeListeners.add(requestListener);
    }

    public void addServletContextAttributeListener(ServletContextAttributeListener attributeListener) {
        servletContextAttributeListeners.add(attributeListener);
    }

    public void addHttpSessionListener(HttpSessionListener httpSessionListener) {
        httpSessionListeners.add(httpSessionListener);
    }

    public List<HttpSessionListener> getHttpSessionListeners() {
        return httpSessionListeners;
    }

    public List<ServletContextAttributeListener> getServletContextAttributeListeners() {
        return servletContextAttributeListeners;
    }

    public List<ServletRequestListener> getServletRequestListeners() {
        return servletRequestListeners;
    }

    public List<HttpSessionAttributeListener> getSessionAttributeListeners() {
        return sessionAttributeListeners;
    }

    public List<ServletRequestAttributeListener> getRequestAttributeListeners() {
        return requestAttributeListeners;
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


    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }
}
