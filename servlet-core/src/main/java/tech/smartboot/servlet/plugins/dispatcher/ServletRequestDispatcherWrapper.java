/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.dispatcher;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.MappingMatch;
import tech.smartboot.feat.core.common.FeatUtils;
import tech.smartboot.feat.core.common.HeaderName;
import tech.smartboot.servlet.SmartHttpServletRequest;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.impl.HttpServletMappingImpl;
import tech.smartboot.servlet.plugins.security.LoginAccount;
import tech.smartboot.servlet.provider.SecurityProvider;

import javax.net.ssl.SSLEngine;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/20
 */
public class ServletRequestDispatcherWrapper extends HttpServletRequestWrapper implements SmartHttpServletRequest {
    private final SmartHttpServletRequest request;
    private final boolean named;
    private String requestUri;
    private String queryString;
    private ServletMappingInfo servletMappingInfo;
    private String servletPath;
    private String pathInfo;
    private Map<String, String[]> parameters;
    private final DispatcherType dispatcherType;
    private boolean pathInit = false;
    private String method;

    public ServletRequestDispatcherWrapper(SmartHttpServletRequest request, DispatcherType dispatcherType, boolean named) {
        super(request);
        this.request = request;
        this.dispatcherType = dispatcherType;
        this.named = named;
        this.servletMappingInfo = request.getServletMappingInfo();
        Object m = request.getAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD);
        if (m != null) {
            this.method = m.toString();
        }
    }

    @Override
    public String getParameter(String name) {
        if (parameters == null) {
            return null;
        }
        String[] values = parameters.get(name);
        return values == null || values.length == 0 ? null : values[0];
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return parameters == null ? Collections.emptyMap() : parameters;
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (parameters == null) {
            return null;
        }
        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (parameters == null) {
            return null;
        }
        return parameters.get(name);
    }

    @Override
    public DispatcherType getDispatcherType() {
        return dispatcherType;
    }

    @Override
    public HttpServletRequest getRequest() {
        return request;
    }

    @Override
    public String getRequestURI() {
        if (named || getDispatcherType() == DispatcherType.INCLUDE) {
            return super.getRequestURI();
        }
        return this.requestUri;
    }

    @Override
    public void setRequestUri(String requestURI) {
        this.requestUri = requestURI;
    }

    @Override
    public String getQueryString() {
        return named || dispatcherType == DispatcherType.INCLUDE ? super.getQueryString() : queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    @Override
    public String getPathInfo() {
        initPath();
        return pathInfo;
    }

    private void initPath() {
        if (pathInit) {
            return;
        }
        pathInit = true;
        switch (servletMappingInfo.getMappingMatch()) {
            case EXACT -> {
                servletPath = servletMappingInfo.getUrlPattern();
                pathInfo = null;
            }
            case EXTENSION -> {
                servletPath = getRequestURI().substring(getContextPath().length());
                pathInfo = null;
            }
            case PATH -> {
                servletPath = servletMappingInfo.getUrlPattern().substring(0, servletMappingInfo.getUrlPattern().length() - 2);
                if (getContextPath().length() + servletPath.length() < getRequestURI().length()) {
                    pathInfo = getRequestURI().substring(getContextPath().length() + servletPath.length());
                }

            }
        }
    }

    @Override
    public String getMethod() {
        if (method == null) {
            return super.getMethod();
        }
        return method;
    }

    @Override
    public void setServletInfo(ServletInfo servletInfo) {
        this.request.setServletInfo(servletInfo);
    }

    @Override
    public ServletInfo getServletInfo() {
        return this.request.getServletInfo();
    }

    @Override
    public void setServletMappingInfo(ServletMappingInfo httpServletMapping) {
        this.servletMappingInfo = httpServletMapping;
    }

    @Override
    public ServletMappingInfo getServletMappingInfo() {
        return servletMappingInfo;
    }

    @Override
    public void setAsyncSupported(boolean supported) {
        this.request.setAsyncSupported(supported);
    }

    @Override
    public void setLoginAccount(LoginAccount loginAccount) {
        this.request.setLoginAccount(loginAccount);
    }

    @Override
    public SSLEngine getSslEngine() {
        return request.getSslEngine();
    }


    @Override
    public HttpServletMapping getHttpServletMapping() {
        if (named || getDispatcherType() == DispatcherType.INCLUDE) {
            return request.getHttpServletMapping();
        }
        if (servletMappingInfo == null) {
            return null;
        }
        String matchValue;
        MappingMatch mappingMatch = servletMappingInfo.getMappingMatch();
        switch (servletMappingInfo.getMappingMatch()) {
            case DEFAULT:
                matchValue = "";
                if (FeatUtils.isBlank(getServletContext().getContextPath())) {
                    mappingMatch = MappingMatch.CONTEXT_ROOT;
                }
                break;
            case EXACT:
                matchValue = servletMappingInfo.getUrlPattern();
                if (matchValue.startsWith("/")) {
                    matchValue = matchValue.substring(1);
                }
                break;
            case PATH:
                String servletPath = getServletPath();
                if (servletMappingInfo.getUrlPattern().length() >= servletPath.length() + 2) {
                    matchValue = "";
                } else {
                    matchValue = getServletPath().substring(servletMappingInfo.getUrlPattern().length() - 1);
                }

                if (matchValue.startsWith("/")) {
                    matchValue = matchValue.substring(1);
                }
                break;
            case EXTENSION:
                matchValue = getServletPath().substring(getServletPath().charAt(0) == '/' ? 1 : 0, getServletPath().length() - servletMappingInfo.getUrlPattern().length() + 1);
                break;
            default:
                throw new IllegalStateException();
        }
        return new HttpServletMappingImpl(mappingMatch, servletMappingInfo, matchValue);
    }

    @Override
    public StringBuffer getRequestURL() {
        if (getDispatcherType() == DispatcherType.FORWARD) {
            return new StringBuffer(getScheme() + "://" + getHeader(HeaderName.HOST.getName()) + getRequestURI());
        }
        return super.getRequestURL();
    }

    @Override
    public String getServletPath() {
        initPath();
        return servletPath;
    }


    public void setParameters(Map<String, String[]> parameters) {
        this.parameters = parameters;
    }

}
