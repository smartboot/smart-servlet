/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebsocketPlugin.java
 * Date: 2021-03-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket;

import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.plugins.Plugin;
import org.smartboot.servlet.plugins.websocket.impl.WebsocketServerContainer;

import javax.websocket.server.ServerContainer;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public class WebsocketPlugin extends Plugin {
    private final WebsocketServerContainer container = new WebsocketServerContainer();

    @Override
    public void willStartContainer(ServletContextRuntime containerRuntime) {
        containerRuntime.setWebsocketProvider(new WebsocketProviderImpl(container));
        containerRuntime.getServletContext().setAttribute(ServerContainer.class.getName(), container);
    }
}
