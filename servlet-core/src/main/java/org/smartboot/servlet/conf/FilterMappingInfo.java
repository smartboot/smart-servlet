/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: FilterMappingInfo.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import org.smartboot.servlet.enums.FilterMappingType;

import javax.servlet.DispatcherType;
import java.util.Set;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class FilterMappingInfo {

    private final String filterName;
    private final FilterMappingType mappingType;
    private final String mapping;
    private final Set<DispatcherType> dispatcher;

    public FilterMappingInfo(final String filterName, final FilterMappingType mappingType, final String mapping, final Set<DispatcherType> dispatcher) {
        this.filterName = filterName;
        this.mappingType = mappingType;
        this.mapping = mapping;
        this.dispatcher = dispatcher;
    }

    public FilterMappingType getMappingType() {
        return mappingType;
    }

    public String getMapping() {
        return mapping;
    }

    public Set<DispatcherType> getDispatcher() {
        return dispatcher;
    }

    public String getFilterName() {
        return filterName;
    }


}
