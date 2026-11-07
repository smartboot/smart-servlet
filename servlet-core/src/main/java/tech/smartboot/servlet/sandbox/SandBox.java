/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
