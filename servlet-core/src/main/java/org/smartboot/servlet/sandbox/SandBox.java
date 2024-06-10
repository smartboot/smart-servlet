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

import org.smartboot.servlet.provider.AsyncContextProvider;
import org.smartboot.servlet.provider.DispatcherProvider;
import org.smartboot.servlet.provider.FaviconProvider;
import org.smartboot.servlet.provider.VendorProvider;
import org.smartboot.servlet.provider.WebsocketProvider;

/**
 * 沙箱环境
 *
 * @author 三刀
 * @version V1.0 , 2020/11/28
 */
public class SandBox {
    public static final String UPGRADE_MESSAGE_ZH = "请升级至 smart-servlet 企业版以启用该功能";
    public static final SandBox INSTANCE = new SandBox();
    private final DispatcherProvider dispatcherProvider = new MockDispatcherProvider();
    private final WebsocketProvider websocketProvider = new MockWebsocketProvider();

    private final VendorProvider vendorProvider = new MockVendorProvider();
    private final AsyncContextProvider asyncContextProvider = new MockAsyncContextProvider();
    private final FaviconProvider faviconProvider = new MockFaviconProvider();

    public VendorProvider getVendorProvider() {
        return vendorProvider;
    }

    public DispatcherProvider getDispatcherProvider() {
        return dispatcherProvider;
    }

    public WebsocketProvider getWebsocketProvider() {
        return websocketProvider;
    }


    public AsyncContextProvider getAsyncContextProvider() {
        return asyncContextProvider;
    }

    public FaviconProvider getFaviconProvider() {
        return faviconProvider;
    }
}
