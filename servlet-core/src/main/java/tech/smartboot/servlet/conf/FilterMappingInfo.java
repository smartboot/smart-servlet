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
    private static final byte MAPPING_URL = 0x00;
    private static final byte MAPPING_SERVLET = 0x01;
    private static final byte DISPATCHER_FORWARD = 0x02;
    private static final byte DISPATCHER_INCLUDE = DISPATCHER_FORWARD << 1;
    private static final byte DISPATCHER_REQUEST = DISPATCHER_FORWARD << 2;
    private static final byte DISPATCHER_ERROR = DISPATCHER_FORWARD << 3;
    private static final byte DISPATCHER_ASYNC = DISPATCHER_FORWARD << 4;
    private final String filterName;
    private final byte mask;
    private final String servletNameMapping;

    public FilterMappingInfo(final String filterName, final FilterMappingType mappingType, final String servletNameMapping, String urlPattern, final Set<DispatcherType> dispatchers) {
        super(urlPattern);
        if (mappingType == null) {
            throw new IllegalArgumentException("mappingType can not be null");
        }
        this.filterName = filterName;
        this.servletNameMapping = servletNameMapping;
        byte mask;
        if (mappingType == FilterMappingType.SERVLET) {
            mask = MAPPING_SERVLET;
        } else {
            mask = MAPPING_URL;
        }

        for (DispatcherType dispatcherType : dispatchers) {
            switch (dispatcherType) {
                case FORWARD:
                    mask |= DISPATCHER_FORWARD;
                    break;
                case INCLUDE:
                    mask |= DISPATCHER_INCLUDE;
                    break;
                case REQUEST:
                    mask |= DISPATCHER_REQUEST;
                    break;
                case ERROR:
                    mask |= DISPATCHER_ERROR;
                    break;
                case ASYNC:
                    mask |= DISPATCHER_ASYNC;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported dispatcher type: " + dispatcherType);
            }
        }
        this.mask = mask;
    }

    public boolean isServletMappingType() {
        return (mask & MAPPING_SERVLET) == MAPPING_SERVLET;
    }

    public String getServletNameMapping() {
        return servletNameMapping;
    }

    public boolean contains(DispatcherType dispatcherType) {
        return switch (dispatcherType) {
            case FORWARD -> (mask & DISPATCHER_FORWARD) != 0;
            case INCLUDE -> (mask & DISPATCHER_INCLUDE) != 0;
            case REQUEST -> (mask & DISPATCHER_REQUEST) != 0;
            case ERROR -> (mask & DISPATCHER_ERROR) != 0;
            case ASYNC -> (mask & DISPATCHER_ASYNC) != 0;
        };
    }

    public String getFilterName() {
        return filterName;
    }

}
