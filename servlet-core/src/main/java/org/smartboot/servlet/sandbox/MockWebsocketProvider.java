/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
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
