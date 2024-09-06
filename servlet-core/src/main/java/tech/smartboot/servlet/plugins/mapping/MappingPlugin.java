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
import tech.smartboot.servlet.plugins.Plugin;

public class MappingPlugin extends Plugin {
    @Override
    public void onServletContextStartSuccess(ServletContextRuntime servletContextRuntime) {
        MappingProviderImpl provider = new MappingProviderImpl(servletContextRuntime.getServletContext().getContextPath().length());
        servletContextRuntime.setMappingProvider(provider);
        servletContextRuntime.getDeploymentInfo().getServletMappings().forEach(mapping -> {
            switch (mapping.getMappingMatch()) {
                case DEFAULT:
                    provider.setDefaultMapping(mapping);
                    break;
                case EXACT:
                    provider.getExactMapping().put(mapping.getUrlPattern(), mapping);
                    break;
                case EXTENSION:
                    provider.getExtensionMappings().add(mapping);
                    break;
                case PATH:
                    provider.getPathMappings().add(mapping);
                    provider.getPathMappings().sort((o1, o2) -> o2.getUrlPattern().length() - o1.getUrlPattern().length());
                    break;
            }
        });
    }
}
