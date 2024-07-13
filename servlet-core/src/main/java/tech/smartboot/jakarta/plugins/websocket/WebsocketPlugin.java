/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.jakarta.plugins.websocket;

import tech.smartboot.jakarta.ServletContextRuntime;
import tech.smartboot.jakarta.conf.FilterInfo;
import tech.smartboot.jakarta.conf.FilterMappingInfo;
import tech.smartboot.jakarta.enums.FilterMappingType;
import tech.smartboot.jakarta.plugins.Plugin;
import tech.smartboot.jakarta.util.PathMatcherUtil;

import jakarta.servlet.DispatcherType;
import jakarta.websocket.server.ServerContainer;
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
        containerRuntime.getDeploymentInfo().addFilter(filterInfo);
        containerRuntime.getDeploymentInfo().addFilterMapping(new FilterMappingInfo(filterInfo.getFilterName(), FilterMappingType.URL, null, PathMatcherUtil.addMapping("/*"), Collections.singleton(DispatcherType.REQUEST)));
    }

    @Override
    public String pluginName() {
        return "websocket";
    }
}
