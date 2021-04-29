/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebsocketContainerProvider.java
 * Date: 2021-04-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket;

import org.smartboot.servlet.plugins.websocket.impl.WebsocketServerContainer;

import javax.websocket.ContainerProvider;
import javax.websocket.WebSocketContainer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/27
 */
public class WebsocketContainerProvider extends ContainerProvider {
    @Override
    protected WebSocketContainer getContainer() {
        return new WebsocketServerContainer();
    }
}
