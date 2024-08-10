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
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.MappingMatch;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.socket.util.Attachment;
import tech.smartboot.servlet.SmartHttpServletRequest;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.impl.HttpServletMappingImpl;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/20
 */
public class ServletRequestDispatcherWrapper extends HttpServletRequestWrapper implements SmartHttpServletRequest {
    private final HttpServletRequestImpl request;
    private final boolean named;
    private String requestUri;
    private String queryString;
    private ServletMappingInfo servletMappingInfo;
    private String servletPath;
    private String pathInfo;
    private Map<String, String[]> parameters;
    private final DispatcherType dispatcherType;
    private boolean pathInit = false;

    public ServletRequestDispatcherWrapper(HttpServletRequestImpl request, DispatcherType dispatcherType, boolean named) {
        super(request);
        this.request = request;
        this.dispatcherType = dispatcherType;
        this.named = named;
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
    public HttpServletRequestImpl getRequest() {
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
        switch (servletMappingInfo.getMappingType()) {
            case EXACT -> {
                servletPath = servletMappingInfo.getMapping();
                pathInfo = null;
            }
            case EXTENSION -> {
                servletPath = getRequestURI().substring(getContextPath().length());
                pathInfo = null;
            }
            case PATH -> {
                servletPath = servletMappingInfo.getMapping().substring(0, servletMappingInfo.getMapping().length() - 2);
                if (getContextPath().length() + servletPath.length() < getRequestURI().length()) {
                    pathInfo = getRequestURI().substring(getContextPath().length() + servletPath.length());
                }

            }
        }
    }

    @Override
    public void setServletInfo(ServletInfo servletInfo) {
        this.request.setServletInfo(servletInfo);
    }

    @Override
    public Attachment getAttachment() {
        return request.getAttachment();
    }

    @Override
    public void setAttachment(Attachment attachment) {
        request.setAttachment(attachment);
    }

    @Override
    public void setServletMappingInfo(ServletMappingInfo httpServletMapping) {
        this.servletMappingInfo = httpServletMapping;
    }

    @Override
    public void setAsyncSupported(boolean supported) {
        this.request.setAsyncSupported(supported);
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
        MappingMatch mappingMatch = servletMappingInfo.getMappingType();
        switch (servletMappingInfo.getMappingType()) {
            case EXACT:
                matchValue = servletMappingInfo.getMapping();
                if (matchValue.startsWith("/")) {
                    matchValue = matchValue.substring(1);
                }
                if (matchValue.isEmpty()) {
                    mappingMatch = StringUtils.isBlank(getServletContext().getContextPath()) ? MappingMatch.CONTEXT_ROOT : MappingMatch.DEFAULT;
                }
                break;
            case PATH:
                String servletPath = getServletPath();
                if (servletMappingInfo.getMapping().length() >= servletPath.length() + 2) {
                    matchValue = "";
                } else {
                    matchValue = getServletPath().substring(servletMappingInfo.getMapping().length() - 1);
                }

                if (matchValue.startsWith("/")) {
                    matchValue = matchValue.substring(1);
                }
                break;
            case EXTENSION:
                matchValue = getServletPath().substring(getServletPath().charAt(0) == '/' ? 1 : 0, getServletPath().length() - servletMappingInfo.getMapping().length() + 1);
                break;
            default:
                throw new IllegalStateException();
        }
        return new HttpServletMappingImpl(mappingMatch, servletMappingInfo, matchValue);
    }

    @Override
    public StringBuffer getRequestURL() {
        if (getDispatcherType() == DispatcherType.FORWARD) {
            return new StringBuffer(getScheme() + "://" + getHeader(HeaderNameEnum.HOST.getName()) + getRequestURI());
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
