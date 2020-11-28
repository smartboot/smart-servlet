/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletMappingTypeEnum.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.enums;

/**
 * 参考《Servlet3.1规范中文版》第12章 映射请求到Servlet
 *
 * @author 三刀
 * @version V1.0 , 2020/10/11
 */
public enum ServletMappingTypeEnum {
    /**
     * 精准匹配
     */
    EXACT_MATCH,
    /**
     * 路径匹配（前缀匹配）
     */
    PREFIX_MATCH,
    /**
     * 后缀匹配
     */
    EXTENSION_MATCH
}
