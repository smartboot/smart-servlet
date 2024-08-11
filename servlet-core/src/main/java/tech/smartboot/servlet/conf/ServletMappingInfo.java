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

import jakarta.servlet.http.MappingMatch;
import tech.smartboot.servlet.util.PathMatcherUtil;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/11
 */
public class ServletMappingInfo {
    private final String servletName;
    private final String mapping;
    private final MappingMatch mappingType;

    public ServletMappingInfo(String servletName, String mapping) {
        this.mapping = PathMatcherUtil.getUrlPattern(mapping);
        this.servletName = servletName;
        this.mappingType = PathMatcherUtil.getMappingType(this.mapping);
    }

    public String getMapping() {
        return mapping;
    }

    public MappingMatch getMappingType() {
        return mappingType;
    }


    public String getServletName() {
        return servletName;
    }
}
