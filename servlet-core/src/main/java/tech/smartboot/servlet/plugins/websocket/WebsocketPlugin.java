/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.websocket;

import jakarta.servlet.DispatcherType;
import jakarta.websocket.server.ServerContainer;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.FilterInfo;
import tech.smartboot.servlet.conf.FilterMappingInfo;
import tech.smartboot.servlet.enums.FilterMappingType;
import tech.smartboot.servlet.plugins.Plugin;

import java.util.Collections;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public class WebsocketPlugin extends Plugin {


    @Override
    public void willStartContainer(ServletContextRuntime containerRuntime) {
        containerRuntime.setWebsocketProvider(new WebsocketProviderImpl());
        containerRuntime.getServletContext().setAttribute(ServerContainer.class.getName(), containerRuntime.getWebsocketProvider().getWebSocketServerContainer());

        //通过Filter触发WebSocket的Upgrade
        FilterInfo filterInfo = new FilterInfo();
        filterInfo.setFilterName("wsFilter");
        filterInfo.setFilterClass(WebSocketFilter.class.getName());
        filterInfo.setAsyncSupported(true);
        containerRuntime.getDeploymentInfo().addFilter(filterInfo);
        containerRuntime.getDeploymentInfo().addFilterMapping(new FilterMappingInfo(filterInfo.getFilterName(), FilterMappingType.URL, null, "/*", Collections.singleton(DispatcherType.REQUEST)));
    }

    @Override
    public String pluginName() {
        return "websocket";
    }
}
