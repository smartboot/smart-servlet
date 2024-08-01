/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.http.HttpServletMapping;
import jakarta.servlet.http.MappingMatch;

public class HttpServletMappingImpl implements HttpServletMapping {
    private final MappingMatch mappingMatch;
    private final String matchValue;
    private final String servletName;
    private final String pattern;

    public HttpServletMappingImpl(MappingMatch mappingMatch, String matchValue, String servletName, String pattern) {
        this.mappingMatch = mappingMatch;
        this.matchValue = matchValue;
        this.servletName = servletName;
        this.pattern = pattern;
    }

    @Override
    public String getMatchValue() {
        return matchValue;
    }

    @Override
    public String getPattern() {
        return pattern;
    }

    @Override
    public String getServletName() {
        return servletName;
    }

    @Override
    public MappingMatch getMappingMatch() {
        return mappingMatch;
    }
}
