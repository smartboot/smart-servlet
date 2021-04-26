/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletMappingInfo.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import org.smartboot.servlet.enums.UriMappingTypeEnum;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/11
 */
public class UriMappingInfo {
    private final String mapping;
    private final UriMappingTypeEnum mappingType;

    public UriMappingInfo(String mapping, UriMappingTypeEnum mappingType) {
        this.mapping = mapping;
        this.mappingType = mappingType;
    }

    public String getMapping() {
        return mapping;
    }

    public UriMappingTypeEnum getMappingType() {
        return mappingType;
    }
}
