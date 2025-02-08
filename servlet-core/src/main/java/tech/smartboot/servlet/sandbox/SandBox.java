/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.sandbox;

import tech.smartboot.servlet.provider.AsyncContextProvider;
import tech.smartboot.servlet.provider.DispatcherProvider;
import tech.smartboot.servlet.provider.FaviconProvider;
import tech.smartboot.servlet.provider.VendorProvider;
import tech.smartboot.servlet.provider.WebsocketProvider;

/**
 * 沙箱环境
 *
 * @author 三刀
 * @version V1.0 , 2020/11/28
 */
public class SandBox {
    public static final String UPGRADE_MESSAGE_ZH = "请升级至 smart-servlet 企业版以启用该功能";
    public static final SandBox INSTANCE = new SandBox();
    private final DispatcherProvider dispatcherProvider = MockProvider.INSTANCE;
    private final WebsocketProvider websocketProvider = MockProvider.INSTANCE;

    private final VendorProvider vendorProvider = MockProvider.INSTANCE;
    private final AsyncContextProvider asyncContextProvider = MockProvider.INSTANCE;
    private final FaviconProvider faviconProvider = MockProvider.INSTANCE;

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
