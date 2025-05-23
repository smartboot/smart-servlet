/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * web.xml对象
 *
 * @author 三刀
 * @version V1.0 , 2019/12/12
 */
public class WebAppInfo {
    private String version;
    private String displayName;
    private String description;
    private boolean metadataComplete;
    /**
     * web.xml中的Servlet配置
     */
    private final Map<String, ServletInfo> servlets = new HashMap<>();
    private final Map<String, List<String>> servletMappings = new HashMap<>();

    /**
     * web.xml中的Filter配置
     */
    private final List<FilterInfo> filters = new ArrayList<>();
    private final List<FilterMappingInfo> filterMappingInfos = new ArrayList<>();


    private final List<String> listeners = new ArrayList<>();

    private final Map<String, String> contextParams = new HashMap<>();

    private final List<ErrorPageInfo> errorPages = new ArrayList<>();

    private final List<String> welcomeFileList = new ArrayList<>();

    private final Map<String, String> localeEncodingMappings = new HashMap<>();
    private final Map<String, String> mimeMappings = new HashMap<>();

    private final Map<String, String> securityRoles = new HashMap<>();

    private final List<SecurityConstraint> securityConstraints = new ArrayList<>();
    private final Map<String, Set<String>> securityRoleMapping = new HashMap<>();
    private List<String> absoluteOrdering;
    private boolean absoluteOrderingOther = false;
    private LoginConfig loginConfig;

    private int sessionTimeout = 0;

    public void addServlet(ServletInfo servletInfo) {
        servlets.put(servletInfo.getServletName(), servletInfo);
    }

    public void addFilter(FilterInfo filterInfo) {
        filters.add(filterInfo);
    }


    public void addListener(String listener) {
        listeners.add(listener);
    }

    public void addContextParam(String param, String value) {
        this.contextParams.put(param, value);
    }

    public void addErrorPage(ErrorPageInfo errorPageInfo) {
        errorPages.add(errorPageInfo);
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

    public List<FilterInfo> getFilters() {
        return filters;
    }

    public List<String> getListeners() {
        return listeners;
    }

    public Map<String, String> getContextParams() {
        return contextParams;
    }

    public List<ErrorPageInfo> getErrorPages() {
        return errorPages;
    }

    public void addWelcomeFile(String file) {
        welcomeFileList.add(file);
    }

    public List<String> getWelcomeFileList() {
        return welcomeFileList;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, String> getLocaleEncodingMappings() {
        return localeEncodingMappings;
    }

    public Map<String, String> getMimeMappings() {
        return mimeMappings;
    }

    public Map<String, String> getSecurityRoles() {
        return securityRoles;
    }

    public List<SecurityConstraint> getSecurityConstraints() {
        return securityConstraints;
    }

    public Map<String, Set<String>> getSecurityRoleMapping() {
        return securityRoleMapping;
    }

    public List<String> getAbsoluteOrdering() {
        return absoluteOrdering;
    }

    public void setAbsoluteOrdering(List<String> absoluteOrdering) {
        this.absoluteOrdering = absoluteOrdering;
    }

    public boolean isMetadataComplete() {
        return metadataComplete;
    }

    public void setMetadataComplete(boolean metadataComplete) {
        this.metadataComplete = metadataComplete;
    }

    public boolean isAbsoluteOrderingOther() {
        return absoluteOrderingOther;
    }

    public void setAbsoluteOrderingOther(boolean absoluteOrderingOther) {
        this.absoluteOrderingOther = absoluteOrderingOther;
    }

    public List<FilterMappingInfo> getFilterMappingInfos() {
        return filterMappingInfos;
    }

    public Map<String, List<String>> getServletMappings() {
        return servletMappings;
    }

    public LoginConfig getLoginConfig() {
        return loginConfig;
    }

    public void setLoginConfig(LoginConfig loginConfig) {
        this.loginConfig = loginConfig;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
