/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
    public void willStartServletContext(ServletContextRuntime containerRuntime) {
        containerRuntime.setWebsocketProvider(new WebsocketProviderImpl());
        containerRuntime.getServletContext().setAttribute(ServerContainer.class.getName(), containerRuntime.getWebsocketProvider().getWebSocketServerContainer());

        //通过Filter触发WebSocket的Upgrade
        FilterInfo filterInfo = new FilterInfo();
        filterInfo.setFilterName("wsFilter");
        filterInfo.setFilterClass(WebSocketFilter.class.getName());
        filterInfo.setAsyncSupported(true);
        containerRuntime.getDeploymentInfo().addFilter(filterInfo);
        filterInfo.getMappings().add(new FilterMappingInfo(filterInfo.getFilterName(), FilterMappingType.URL, null, "/*", Collections.singleton(DispatcherType.REQUEST)));
    }

    @Override
    public String pluginName() {
        return "websocket";
    }
}
