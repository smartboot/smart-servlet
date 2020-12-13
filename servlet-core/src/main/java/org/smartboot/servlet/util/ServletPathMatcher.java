/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletPathMatcher.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.util;

import org.smartboot.servlet.conf.ServletMappingInfo;
import org.smartboot.servlet.enums.ServletMappingTypeEnum;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class ServletPathMatcher {

    private final static ServletPathMatcher INSTANCE = new ServletPathMatcher();

    public static ServletPathMatcher getInstance() {
        return INSTANCE;
    }

    public static boolean isAbsoluteUrl(String location) {
        if (location != null && location.length() > 0 && location.contains(":")) {
            try {
                URI uri = new URI(location);
                return uri.getScheme() != null;
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static ServletMappingInfo addMapping(final String mapping) {
        if (!mapping.contains("*")) {
            if (!mapping.startsWith("/")) {
                throw new IllegalArgumentException("invalid mapping: " + mapping);
            }
            return new ServletMappingInfo(mapping, ServletMappingTypeEnum.EXACT_MATCH);
        } else if (mapping.startsWith("*.")) {
            return new ServletMappingInfo(mapping, ServletMappingTypeEnum.EXTENSION_MATCH);
        } else if (mapping.startsWith("/") && mapping.endsWith("/*")) {
            return new ServletMappingInfo(mapping, ServletMappingTypeEnum.PREFIX_MATCH);
        } else {
            throw new UnsupportedOperationException(mapping);
        }
    }

    public int matches(String uri, int startIndex, ServletMappingInfo mappingInfo) {
        String pattern = mappingInfo.getMapping();
        ServletMappingTypeEnum mappingTypeEnum = mappingInfo.getMappingType();
        int matcherIndex = -1;
        switch (mappingTypeEnum) {
            case EXACT_MATCH:
                //《Servlet3.1规范中文版》12.2 映射规范
                //空字符串“”是一个特殊的 URL 模式，其精确映射到应用的上下文根，
                // 即，http://host:port/<context-root>/ 请求形式。
                // 在这种情况下，路径信息是‘/’且 servlet 路径和上下文路径是空字符串(“”)。
                if (uri.length() == startIndex && "/".equals(pattern)) {
                    return 0;
                }
                if (uri.length() - startIndex != pattern.length()) {
                    return -1;
                }
                //第一位肯定是"/",从第二位开始匹配
                for (int i = 1; i < pattern.length(); i++) {
                    if (uri.charAt(startIndex + i) != pattern.charAt(i)) {
                        return -1;
                    }
                }
                matcherIndex = pattern.length();
                break;
            case PREFIX_MATCH:
                //《Servlet3.1规范中文版》12.2 映射规范
                //空字符串“”是一个特殊的 URL 模式，其精确映射到应用的上下文根，
                // 即，http://host:port/<context-root>/ 请求形式。
                // 在这种情况下，路径信息是‘/’且 servlet 路径和上下文路径是空字符串(“”)。
                //pattern.length() == 2 等同于 "/*".equals(pattern)
                if (uri.length() == startIndex && pattern.length() == 2) {
                    return 0;
                }
                //舍去"/ab/*"最后一位"/*"
                int matchLen = pattern.length() - 2;
                if (uri.length() - startIndex < matchLen) {
                    return -1;
                }
                //第一位肯定是"/",从第二位开始匹配
                for (int i = 1; i < matchLen; i++) {
                    if (uri.charAt(startIndex + i) != pattern.charAt(i)) {
                        return -1;
                    }
                }
                matcherIndex = pattern.length() - 2;
                break;
            case EXTENSION_MATCH:
                // 不比较"*.xx" 中的 *
                int uriStartIndex = uri.length() - pattern.length() - 1;
                for (int i = 1; i < pattern.length(); i++) {
                    if (uri.charAt(uriStartIndex + i) != pattern.charAt(i)) {
                        return -1;
                    }
                }
                matcherIndex = uri.length();
                break;
            default:
                throw new UnsupportedOperationException("unSupport mappingType " + mappingTypeEnum);
        }
        return matcherIndex;
    }
}