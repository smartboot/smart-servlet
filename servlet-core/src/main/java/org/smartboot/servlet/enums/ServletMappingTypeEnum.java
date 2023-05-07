/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
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
    EXTENSION_MATCH,

}
