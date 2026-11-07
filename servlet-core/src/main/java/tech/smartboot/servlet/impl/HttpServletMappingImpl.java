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

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.MappingMatch;
import tech.smartboot.servlet.conf.ServletMappingInfo;

public class HttpServletMappingImpl implements HttpServletMapping {
    private final ServletMappingInfo servletMappingInfo;
    private final MappingMatch mappingMatch;
    private final String matchValue;

    public HttpServletMappingImpl(MappingMatch mappingMatch, ServletMappingInfo servletMappingInfo, String matchValue) {
        this.mappingMatch = mappingMatch;
        this.servletMappingInfo = servletMappingInfo;
        this.matchValue = matchValue;
    }

    @Override
    public String getMatchValue() {
        return matchValue;
    }

    @Override
    public String getPattern() {
        return servletMappingInfo.getUrlPattern();
    }

    @Override
    public String getServletName() {
        return servletMappingInfo.getServletInfo().getServletName();
    }

    @Override
    public MappingMatch getMappingMatch() {
        return mappingMatch;
    }
}
