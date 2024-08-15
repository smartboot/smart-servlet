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

import jakarta.servlet.DispatcherType;
import tech.smartboot.servlet.enums.FilterMappingType;

import java.util.Set;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class FilterMappingInfo extends UrlPattern {

    private final String filterName;
    private final FilterMappingType mappingType;
    private final Set<DispatcherType> dispatcher;
    private final String servletNameMapping;

    public FilterMappingInfo(final String filterName, final FilterMappingType mappingType, final String servletNameMapping, String urlPattern, final Set<DispatcherType> dispatcher) {
        super(urlPattern);
        this.filterName = filterName;
        this.mappingType = mappingType;
        this.servletNameMapping = servletNameMapping;
        this.dispatcher = dispatcher;
    }

    public FilterMappingType getMappingType() {
        return mappingType;
    }

    public String getServletNameMapping() {
        return servletNameMapping;
    }

    public Set<DispatcherType> getDispatcher() {
        return dispatcher;
    }

    public String getFilterName() {
        return filterName;
    }

}
