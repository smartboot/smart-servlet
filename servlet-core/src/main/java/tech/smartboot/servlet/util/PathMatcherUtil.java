/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.MappingMatch;
import tech.smartboot.servlet.conf.UrlPattern;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class PathMatcherUtil {

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

    public static String getUrlPattern(String urlPattern) {
        if (!urlPattern.startsWith("/") && !urlPattern.startsWith("*") && !urlPattern.isEmpty()) {
            urlPattern = "/" + urlPattern;
        }
        return urlPattern;
    }

    public static MappingMatch getMappingType(String urlPattern) {
        if ("/".equals(urlPattern)) {
            return MappingMatch.DEFAULT;
        } else if (!urlPattern.contains("*")) {
            if (!urlPattern.startsWith("/")) {
                throw new IllegalArgumentException("invalid mapping: " + urlPattern);
            }
            return MappingMatch.EXACT;
        } else if (urlPattern.startsWith("*.")) {
            return MappingMatch.EXTENSION;
        } else if (urlPattern.startsWith("/") && urlPattern.endsWith("/*")) {
            return MappingMatch.PATH;
        } else {
            throw new IllegalArgumentException("illegal mapping : " + urlPattern);
        }
    }

    public static boolean matches(HttpServletRequest request, UrlPattern mappingInfo) {
        String uri = request.getRequestURI();
        return matches(uri, request.getContextPath().length(), mappingInfo);
    }

    /**
     * @param uri
     * @param startIndex
     * @param mappingInfo
     * @return 0:根目录匹配
     */
    public static boolean matches(String uri, int startIndex, UrlPattern mappingInfo) {
        String pattern = mappingInfo.getUrlPattern();
        MappingMatch mappingTypeEnum = mappingInfo.getMappingMatch();
        int servletPathEndIndex = -1;
        switch (mappingTypeEnum) {
            case DEFAULT:
                if ("/".equals(pattern)) {
                    return true;
                }
                break;
            //精准匹配
            case EXACT:
                //《Servlet3.1规范中文版》12.2 映射规范
                //空字符串“”是一个特殊的 URL 模式，其精确映射到应用的上下文根，
                // 即，http://host:port/<context-root>/ 请求形式。
                // 在这种情况下，路径信息是‘/’且 servlet 路径和上下文路径是空字符串(“”)。
                if (uri.length() == startIndex || "/".equals(pattern)) {
                    return true;
                }
                if (uri.length() - startIndex != pattern.length()) {
                    return false;
                }
                //第一位肯定是"/",从第二位开始匹配
                for (int i = 1; i < pattern.length(); i++) {
                    if (uri.charAt(startIndex + i) != pattern.charAt(i)) {
                        return false;
                    }
                }
                servletPathEndIndex = startIndex + pattern.length();
                break;
            //路径匹配（前缀匹配）
            case PATH:
                //《Servlet3.1规范中文版》12.2 映射规范
                //空字符串“”是一个特殊的 URL 模式，其精确映射到应用的上下文根，
                // 即，http://host:port/<context-root>/ 请求形式。
                // 在这种情况下，路径信息是‘/’且 servlet 路径和上下文路径是空字符串(“”)。
                //pattern.length() == 2 等同于 "/*".equals(pattern)
                if (uri.length() == startIndex && pattern.length() == 2) {
                    return true;
                }
                //舍去"/ab/*"最后一位"/*"
                int matchLen = pattern.length() - 2;
                int remainingLen = uri.length() - startIndex;
                if (remainingLen < matchLen) {
                    return false;
                }
                if (remainingLen >= matchLen + 1 && uri.charAt(startIndex + matchLen) != '/') {
                    return false;
                }

                //第一位肯定是"/",从第二位开始匹配
                for (int i = 1; i < matchLen; i++) {
                    if (uri.charAt(startIndex + i) != pattern.charAt(i)) {
                        return false;
                    }
                }

                servletPathEndIndex = startIndex + pattern.length() - 2;
                break;
            //后缀匹配
            case EXTENSION:
                // 不比较"*.xx" 中的 *
                int uriStartIndex = uri.length() - pattern.length();
                if (uriStartIndex <= 0) {
                    return false;
                }
                for (int i = 1; i < pattern.length(); i++) {
                    if (uri.charAt(uriStartIndex + i) != pattern.charAt(i)) {
                        return false;
                    }
                }
                servletPathEndIndex = uri.length();
                break;
            default:
                throw new IllegalStateException("unSupport mappingType " + mappingTypeEnum);
        }
        return true;
    }
}