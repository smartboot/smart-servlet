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

public class UrlPattern {
    private final String urlPattern;
    private final MappingMatch mappingMatch;

    public UrlPattern(String urlPattern) {
        if (urlPattern == null) {
            this.urlPattern = null;
            this.mappingMatch = null;
        } else {
            this.urlPattern = PathMatcherUtil.getUrlPattern(urlPattern);
            this.mappingMatch = PathMatcherUtil.getMappingType(this.urlPattern);
        }

    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public MappingMatch getMappingMatch() {
        return mappingMatch;
    }
}
