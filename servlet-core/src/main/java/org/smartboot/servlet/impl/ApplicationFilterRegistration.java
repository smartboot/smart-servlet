/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ApplicationFilterRegistration.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.conf.UriMappingInfo;
import org.smartboot.servlet.enums.FilterMappingType;
import org.smartboot.servlet.util.PathMatcherUtil;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
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
public class ApplicationFilterRegistration
        implements FilterRegistration.Dynamic {


    private final FilterInfo filterDef;
    private final DeploymentInfo context;

    public ApplicationFilterRegistration(FilterInfo filterDef, DeploymentInfo context) {
        this.filterDef = filterDef;
        this.context = context;
    }

    @Override
    public void addMappingForServletNames(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... servletNames) {
        for (String servletName : servletNames) {
            FilterMappingInfo mappingInfo = new FilterMappingInfo(filterDef.getFilterName(), FilterMappingType.SERVLET, servletName, null, dispatcherTypes);
            context.addFilterMapping(mappingInfo);
        }
    }

    @Override
    public void addMappingForUrlPatterns(
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String... urlPatterns) {
        for (String urlPattern : urlPatterns) {
            FilterMappingInfo mappingInfo = new FilterMappingInfo(filterDef.getFilterName(), FilterMappingType.URL, null, PathMatcherUtil.addMapping(urlPattern), dispatcherTypes);
            context.addFilterMapping(mappingInfo);
        }
    }

    @Override
    public Collection<String> getServletNameMappings() {
        return context.getFilterMappings().stream()
                .filter(filterMappingInfo -> filterMappingInfo.getMappingType() == FilterMappingType.URL)
                .map(FilterMappingInfo::getServletUrlMapping).map(UriMappingInfo::getMapping).collect(Collectors.toList());
    }

    @Override
    public Collection<String> getUrlPatternMappings() {
        return context.getFilterMappings().stream()
                .filter(filterMappingInfo -> filterMappingInfo.getMappingType() == FilterMappingType.SERVLET)
                .map(FilterMappingInfo::getServletNameMapping).collect(Collectors.toList());
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
        System.out.println("setAsyncSupported:" + asyncSupported);
//        throw new UnsupportedOperationException();
//        filterDef.setAsyncSupported(Boolean.valueOf(asyncSupported).toString());
    }

}