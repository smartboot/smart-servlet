/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebsocketProvider.java
 * Date: 2021-03-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.provider;

import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.ServletContextRuntime;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public interface WebsocketProvider {
    void doHandle(ServletContextRuntime runtime, WebSocketRequest request, WebSocketResponse response);
}
