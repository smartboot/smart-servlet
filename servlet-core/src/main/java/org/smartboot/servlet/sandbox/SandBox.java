/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SandBox.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.servlet.provider.DispatcherProvider;
import org.smartboot.servlet.provider.MemoryPoolProvider;
import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.servlet.provider.WebsocketProvider;

/**
 * 沙箱环境
 *
 * @author 三刀
 * @version V1.0 , 2020/11/28
 */
public class SandBox {
    public static final SandBox INSTANCE = new SandBox();
    private final DispatcherProvider dispatcherProvider = new MockDispatcherProvider();
    private final SessionProvider sessionProvider = new MockSessionProvider();
    private final WebsocketProvider websocketProvider = new MockWebsocketProvider();

    private final MemoryPoolProvider memoryPoolProvider = new MockMemoryPoolProvider();

    public MemoryPoolProvider getMemoryPoolProvider() {
        return memoryPoolProvider;
    }

    public DispatcherProvider getDispatcherProvider() {
        return dispatcherProvider;
    }

    public SessionProvider getSessionProvider() {
        return sessionProvider;
    }

    public WebsocketProvider getWebsocketProvider() {
        return websocketProvider;
    }
}
