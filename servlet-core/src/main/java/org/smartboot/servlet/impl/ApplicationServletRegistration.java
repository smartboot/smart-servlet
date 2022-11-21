/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ApplicationServletRegistration.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.conf.ServletMappingInfo;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration;
import javax.servlet.ServletSecurityElement;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/18
 */
public class ApplicationServletRegistration implements ServletRegistration.Dynamic {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationServletRegistration.class);
    private ServletInfo servletInfo;
    private DeploymentInfo deploymentInfo;

    public ApplicationServletRegistration(ServletInfo servletInfo, DeploymentInfo deploymentInfo) {
        this.servletInfo = servletInfo;
        this.deploymentInfo = deploymentInfo;
    }

    @Override
    public void setLoadOnStartup(int loadOnStartup) {
        servletInfo.setLoadOnStartup(loadOnStartup);
    }

    @Override
    public Set<String> setServletSecurity(ServletSecurityElement constraint) {
        LOGGER.info("unSupport");
        return null;
    }

    @Override
    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        servletInfo.setMultipartConfig(multipartConfig);
    }

    @Override
    public void setAsyncSupported(boolean isAsyncSupported) {
        LOGGER.info("unSupport");
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        Set<String> mappingSet = new HashSet<>();
        for (String urlPattern : urlPatterns) {
            servletInfo.addMapping(urlPattern);
            mappingSet.add(urlPattern);
        }
        return mappingSet;
    }

    @Override
    public Collection<String> getMappings() {
        return servletInfo.getMappings().stream()
                .map(ServletMappingInfo::getMapping).collect(Collectors.toList());
    }

    @Override
    public String getRunAsRole() {
        LOGGER.info("unSupport");
        return null;
    }

    @Override
    public void setRunAsRole(String roleName) {
        LOGGER.info("unSupport");
    }

    @Override
    public String getName() {
        return servletInfo.getServletName();
    }

    @Override
    public String getClassName() {
        return servletInfo.getServlet().getClass().getName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        return !Objects.equals(servletInfo.getInitParams().put(name, value), value);
    }

    @Override
    public String getInitParameter(String name) {
        return servletInfo.getInitParams().get(name);
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {
        initParameters.forEach((key, value) -> servletInfo.addInitParam(key, value));
        return servletInfo.getInitParams().keySet();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return new HashMap<>(servletInfo.getInitParams());
    }
}
