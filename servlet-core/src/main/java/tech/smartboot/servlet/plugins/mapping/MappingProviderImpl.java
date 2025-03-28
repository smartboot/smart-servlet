/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.mapping;

import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.provider.MappingProvider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MappingProviderImpl implements MappingProvider {
    private ServletMappingInfo defaultMapping;
    private final Map<String, ServletMappingInfo> exactMapping = new HashMap<>();
    private final List<ServletMappingInfo> extensionMappings = new ArrayList<>();
    private final List<ServletMappingInfo> pathMappings = new ArrayList<>();
    private final int offset;

    public MappingProviderImpl(int offset) {
        this.offset = offset;
    }

    @Override
    public ServletMappingInfo matchWithContextPath(String requestUri) {
        return match(requestUri, offset);
    }

    public ServletMappingInfo match(String requestUri, int offset) {
        String mappingUrl = requestUri.substring(offset);
        //精准匹配
        ServletMappingInfo servletMappingInfo = exactMapping.get(mappingUrl);
        if (servletMappingInfo != null) {
            return servletMappingInfo;
        }
        //路径匹配
        int remaining = requestUri.length() - offset;
        for (ServletMappingInfo mappingInfo : pathMappings) {
            int matchLength = mappingInfo.getUrlPattern().length() - 2;
            if (remaining < matchLength) {
                //后续一定更短
                continue;
            }

            boolean match = true;
            for (int i = 0; i < matchLength; i++) {
                if (requestUri.charAt(i + offset) != mappingInfo.getUrlPattern().charAt(i)) {
                    match = false;
                    break;
                }
            }
            if (match && (matchLength == mappingUrl.length() || (mappingUrl.charAt(matchLength) == '/'))) {
                return mappingInfo;
            }
        }

        //后缀匹配
        for (ServletMappingInfo mappingInfo : extensionMappings) {
            boolean match = true;
            for (int i = 1; i < mappingInfo.getUrlPattern().length() - 1; i++) {
                if (requestUri.charAt(requestUri.length() - i) != mappingInfo.getUrlPattern().charAt(mappingInfo
                        .getUrlPattern().length() - i)) {
                    match = false;
                    break;
                }
            }
            if (match) {
                return mappingInfo;
            }
        }

        //默认
        return defaultMapping;
    }

    @Override
    public ServletMappingInfo matchWithoutContextPath(String url) {
        return match(url, 0);
    }

    public void setDefaultMapping(ServletMappingInfo defaultMapping) {
        if (this.defaultMapping != null) {
            throw new IllegalStateException();
        }
        this.defaultMapping = defaultMapping;
    }

    public Map<String, ServletMappingInfo> getExactMapping() {
        return exactMapping;
    }

    public List<ServletMappingInfo> getExtensionMappings() {
        return extensionMappings;
    }

    public List<ServletMappingInfo> getPathMappings() {
        return pathMappings;
    }

    @Override
    public void addMapping(ServletMappingInfo mapping) {
        switch (mapping.getMappingMatch()) {
            case DEFAULT:
                setDefaultMapping(mapping);
                break;
            case EXACT:
                getExactMapping().put(mapping.getUrlPattern(), mapping);
                break;
            case EXTENSION:
                getExtensionMappings().add(mapping);
                break;
            case PATH:
                getPathMappings().add(mapping);
                getPathMappings().sort((o1, o2) -> o2.getUrlPattern().length() - o1.getUrlPattern().length());
                break;
        }
    }
}
