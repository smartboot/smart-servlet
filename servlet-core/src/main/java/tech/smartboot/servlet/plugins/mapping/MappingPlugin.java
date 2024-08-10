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

import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.plugins.Plugin;

import java.util.Comparator;

public class MappingPlugin extends Plugin {
    @Override
    public void onContainerStartSuccess(ServletContextRuntime servletContextRuntime) {
        MappingProviderImpl provider = new MappingProviderImpl(servletContextRuntime.getServletContext().getContextPath().length());
        servletContextRuntime.setMappingProvider(provider);
        servletContextRuntime.getDeploymentInfo().getServlets().values().stream().map(ServletInfo::getMappings).forEach(mappings -> {
            mappings.forEach(mapping -> {
                switch (mapping.getMappingType()) {
                    case DEFAULT:
                        provider.setDefaultMapping(mapping);
                        break;
                    case EXACT:
                        provider.getExactMapping().put(mapping.getMapping(), mapping);
                        break;
                    case EXTENSION:
                        provider.getExtensionMappings().add(mapping);
                        break;
                    case PATH:
                        provider.getPathMappings().add(mapping);
                        provider.getPathMappings().sort((o1, o2) -> o2.getMapping().length() - o1.getMapping().length());
                        break;
                }
            });
        });
    }
}
