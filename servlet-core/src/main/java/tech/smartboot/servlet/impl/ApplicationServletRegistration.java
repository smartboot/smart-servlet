/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletSecurityElement;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private final ServletInfo servletInfo;
    private final ServletContextRuntime runtime;

    public ApplicationServletRegistration(ServletContextRuntime runtime, ServletInfo servletInfo) {
        this.runtime = runtime;
        this.servletInfo = servletInfo;
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
        servletInfo.setAsyncSupported(isAsyncSupported);
    }

    @Override
    public Set<String> addMapping(String... urlPatterns) {
        //If any of the specified URL patterns are already mapped to a different Servlet, no updates will be performed.
        Set<String> mappingSet = new HashSet<>(Arrays.asList(urlPatterns));
        if (runtime.getDeploymentInfo().getServlets().values().stream().anyMatch(servlet -> servlet.getServletMappings().stream().anyMatch(mapping -> mappingSet.contains(mapping.getUrlPattern())))) {
            //the (possibly empty) Set of URL patterns that are already mapped to a different Servlet
            return Collections.emptySet();
        }
        for (String urlPattern : urlPatterns) {
            servletInfo.addServletMapping(urlPattern,runtime);
        }
        return Collections.emptySet();
    }

    @Override
    public Collection<String> getMappings() {
        return servletInfo.getServletMappings().stream().map(ServletMappingInfo::getUrlPattern).collect(Collectors.toList());
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
        initParameters.forEach(servletInfo::addInitParam);
        return servletInfo.getInitParams().keySet();
    }

    @Override
    public Map<String, String> getInitParameters() {
        return new HashMap<>(servletInfo.getInitParams());
    }
}
