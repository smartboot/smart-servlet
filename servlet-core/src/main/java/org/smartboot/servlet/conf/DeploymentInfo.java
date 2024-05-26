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

import org.smartboot.servlet.AnnotationsLoader;
import org.smartboot.servlet.impl.ServletContextWrapperListener;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRequestAttributeListener;
import javax.servlet.ServletRequestListener;
import javax.servlet.annotation.HandlesTypes;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionIdListener;
import javax.servlet.http.HttpSessionListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private List<ServletContainerInitializerInfo> servletContainerInitializers = new ArrayList<>();
    private List<ServletContextAttributeListener> servletContextAttributeListeners = new ArrayList<>();
    private List<ServletContextWrapperListener> servletContextListeners = new ArrayList<>();
    //是否处于动态listener阶段
    private boolean dynamicListenerState;
    private List<HttpSessionListener> httpSessionListeners = new ArrayList<>();
    private List<HttpSessionIdListener> httpSessionIdListeners = new ArrayList<>();
    private List<ServletRequestListener> servletRequestListeners = new ArrayList<>();

    private List<HttpSessionAttributeListener> sessionAttributeListeners = new ArrayList<>();
    private List<ServletRequestAttributeListener> requestAttributeListeners = new ArrayList<>();
    private List<String> welcomeFiles = Collections.emptyList();
    private final Set<String> securityRoles = new HashSet<>();
    private final Map<String, String> localeEncodingMappings = new HashMap<>();
    private final ClassLoader classLoader;
    private String displayName;
    private URL contextUrl;

    private AnnotationsLoader annotationsLoader;
    /**
     * 会话超时时间
     */
    private int sessionTimeout;

    private ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

    public URL getContextUrl() {
        return contextUrl;
    }

    public void setContextUrl(URL contextUrl) {
        this.contextUrl = contextUrl;
    }

    public DeploymentInfo(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
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

    public void amazing() {
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
        if (httpSessionIdListeners.isEmpty()) {
            httpSessionIdListeners = Collections.emptyList();
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
        servletContextListeners.add(new ServletContextWrapperListener(contextListener, dynamicListenerState));
    }

    public List<ServletContextWrapperListener> getServletContextListeners() {
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

    public void addHttpSessionIdListener(HttpSessionIdListener httpSessionIdListener) {
        httpSessionIdListeners.add(httpSessionIdListener);
    }

    public List<HttpSessionListener> getHttpSessionListeners() {
        return httpSessionListeners;
    }

    public List<HttpSessionIdListener> getHttpSessionIdListeners() {
        return httpSessionIdListeners;
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

    public void addLocaleEncodingMapping(String locale, String encoding) {
        localeEncodingMappings.put(locale, encoding);
    }

    public Map<String, String> getLocaleEncodingMappings() {
        return localeEncodingMappings;
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public Set<String> getSecurityRoles() {
        return securityRoles;
    }

    public void setDynamicListenerState(boolean dynamicListenerState) {
        this.dynamicListenerState = dynamicListenerState;
    }

    public boolean isDynamicListenerState() {
        return dynamicListenerState;
    }
}
