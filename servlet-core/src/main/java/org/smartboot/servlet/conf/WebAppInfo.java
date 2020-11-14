/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebAppInfo.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * web.xml对象
 *
 * @author 三刀
 * @version V1.0 , 2019/12/12
 */
public class WebAppInfo {

    /**
     * web.xml中的Servlet配置
     */
    private final Map<String, ServletInfo> servlets = new HashMap();

    /**
     * web.xml中的Filter配置
     */
    private Map<String, FilterInfo> filters = new HashMap<>();

    /**
     * web.xml中的Filter映射信息配置
     */
    private List<FilterMappingInfo> filterMappings = new ArrayList<>();

    private List<String> listeners = new ArrayList<>();

    private Map<String, String> contextParams = new HashMap<>();

    private Map<Integer, ErrorPageInfo> errorPages = new HashMap<>();

    private int sessionTimeout = 0;

    public void addServlet(ServletInfo servletInfo) {
        servlets.put(servletInfo.getServletName(), servletInfo);
    }

    public void addFilter(FilterInfo filterInfo) {
        filters.put(filterInfo.getFilterName(), filterInfo);
    }

    public void addFilterMapping(FilterMappingInfo filterMappingInfo) {
        filterMappings.add(filterMappingInfo);
    }

    public void addListener(String listener) {
        listeners.add(listener);
    }

    public void addContextParam(String param, String value) {
        this.contextParams.put(param, value);
    }

    public ServletInfo getServlet(String servletName) {
        return servlets.get(servletName);
    }

    public void addErrorPage(ErrorPageInfo errorPageInfo) {
        errorPages.put(errorPageInfo.getErrorCode(), errorPageInfo);
    }

    public int getSessionTimeout() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    public Map<String, ServletInfo> getServlets() {
        return servlets;
    }

    public Map<String, FilterInfo> getFilters() {
        return filters;
    }

    public List<FilterMappingInfo> getFilterMappings() {
        return filterMappings;
    }

    public List<String> getListeners() {
        return listeners;
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

    public Map<Integer, ErrorPageInfo> getErrorPages() {
        return errorPages;
    }
}
