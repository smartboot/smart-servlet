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

import org.smartboot.servlet.provider.DispatcherProvider;
import org.smartboot.servlet.provider.MemoryPoolProvider;
import org.smartboot.servlet.provider.SecurityProvider;
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

    private final SecurityProvider securityProvider = new MockSecurityProvider();

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

    public SecurityProvider getSecurityProvider() {
        return securityProvider;
    }
}
