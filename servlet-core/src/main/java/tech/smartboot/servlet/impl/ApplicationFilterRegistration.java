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

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterRegistration;
import tech.smartboot.servlet.conf.FilterInfo;
import tech.smartboot.servlet.conf.FilterMappingInfo;
import tech.smartboot.servlet.enums.FilterMappingType;

import java.util.Collection;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class ApplicationFilterRegistration implements FilterRegistration.Dynamic {


    private final FilterInfo filterDef;

    public ApplicationFilterRegistration(FilterInfo filterDef) {
        this.filterDef = filterDef;
    }

    @Override
    public void addMappingForServletNames(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... servletNames) {
        for (String servletName : servletNames) {
            filterDef.getMappings().add(new FilterMappingInfo(filterDef.getFilterName(), FilterMappingType.SERVLET, servletName, null, dispatcherTypes));
        }
    }

    @Override
    public void addMappingForUrlPatterns(EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter, String... urlPatterns) {
        for (String urlPattern : urlPatterns) {
            filterDef.getMappings().add(new FilterMappingInfo(filterDef.getFilterName(), FilterMappingType.URL, null, urlPattern, dispatcherTypes));
        }
    }

    @Override
    public Collection<String> getServletNameMappings() {
        return filterDef.getMappings().stream().filter(filterMappingInfo -> filterMappingInfo.getFilterName().equals(filterDef.getFilterName()) && filterMappingInfo.isServletMappingType()).map(FilterMappingInfo::getServletNameMapping).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return filterDef.getMappings().stream().filter(filterMappingInfo -> filterMappingInfo.getFilterName().equals(filterDef.getFilterName()) && !filterMappingInfo.isServletMappingType()).map(FilterMappingInfo::getUrlPattern).collect(Collectors.toList());
    }

    @Override
    public String getClassName() {
        return filterDef.getFilterClass();
    }

    @Override
    public String getInitParameter(String name) {
        return filterDef.getInitParams().get(name);
    }

    @Override
    public Map<String, String> getInitParameters() {
        return new HashMap<>(filterDef.getInitParams());
    }

    @Override
    public String getName() {
        return filterDef.getFilterName();
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (name == null || value == null) {
            throw new IllegalArgumentException();
        }
        if (getInitParameter(name) != null) {
            return false;
        }
        filterDef.addInitParam(name, value);
        return true;
    }

    @Override
    public Set<String> setInitParameters(Map<String, String> initParameters) {

        Set<String> conflicts = new HashSet<>();

        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null) {
                throw new IllegalArgumentException();
            }
            if (getInitParameter(entry.getKey()) != null) {
                conflicts.add(entry.getKey());
            }
        }

        // Have to add in a separate loop since spec requires no updates at all
        // if there is an issue
        for (Map.Entry<String, String> entry : initParameters.entrySet()) {
            setInitParameter(entry.getKey(), entry.getValue());
        }

        return conflicts;
    }

    @Override
    public void setAsyncSupported(boolean asyncSupported) {
        filterDef.setAsyncSupported(asyncSupported);
    }

}