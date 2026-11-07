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

import jakarta.websocket.server.ServerContainer;
import tech.smartboot.servlet.plugins.websocket.impl.WebSocketServerContainerImpl;
import tech.smartboot.servlet.provider.WebsocketProvider;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/31
 */
public class WebsocketProviderImpl implements WebsocketProvider {

    private final WebSocketServerContainerImpl serverContainer = new WebSocketServerContainerImpl();

    @Override
    public ServerContainer getWebSocketServerContainer() {
        return serverContainer;
    }

}
