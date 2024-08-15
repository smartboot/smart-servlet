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

/**
 * @author 三刀
 * @version V1.0 , 2020/10/11
 */
public class ServletMappingInfo extends UrlPattern {
    private final String servletName;

    public ServletMappingInfo(String servletName, String mapping) {
        super(mapping);
        this.servletName = servletName;
    }

    public String getServletName() {
        return servletName;
    }
}
