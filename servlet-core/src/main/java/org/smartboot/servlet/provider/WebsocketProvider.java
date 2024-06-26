/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.provider;

import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.WebSocketServerContainer;
import org.smartboot.socket.util.AttachKey;

import javax.websocket.Session;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public interface WebsocketProvider {
    AttachKey<? super Session> WEBSOCKET_SESSION_ATTACH_KEY = AttachKey.valueOf("websocketSession");

    WebSocketServerContainer getWebSocketServerContainer();

    void doHandle(WebSocketRequest request, WebSocketResponse response);
}
