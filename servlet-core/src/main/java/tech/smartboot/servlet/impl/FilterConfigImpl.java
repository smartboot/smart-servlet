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

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import tech.smartboot.servlet.conf.FilterInfo;

import java.util.Collections;
import java.util.Enumeration;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class FilterConfigImpl implements FilterConfig {

    private final FilterInfo filterInfo;
    private final ServletContext servletContext;

    public FilterConfigImpl(final FilterInfo filterInfo, final ServletContext servletContext) {
        this.filterInfo = filterInfo;
        this.servletContext = servletContext;
    }

    @Override
    public String getFilterName() {
        return filterInfo.getFilterName();
    }

    @Override
    public ServletContext getServletContext() {
        return servletContext;
    }

    @Override
    public String getInitParameter(final String name) {
        return filterInfo.getInitParams().get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(filterInfo.getInitParams().keySet());
    }
}
