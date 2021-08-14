/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MockWeboscketProvider.java
 * Date: 2021-03-30
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.provider.WebsocketProvider;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/30
 */
public class MockWebsocketProvider implements WebsocketProvider {
    @Override
    public void onHandShark(ServletContextRuntime runtime, WebSocketRequest request, WebSocketResponse response) {

    }

    @Override
    public void doHandle(ServletContextRuntime runtime, WebSocketRequest request, WebSocketResponse response) {
        throw new PluginException("Please install the [websocket] plugin to enable the [doHandle] function");
    }
}
