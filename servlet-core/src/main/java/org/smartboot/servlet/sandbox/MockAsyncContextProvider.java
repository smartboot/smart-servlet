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

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.provider.AsyncContextProvider;

import javax.servlet.AsyncContext;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class MockAsyncContextProvider implements AsyncContextProvider {
    @Override
    public AsyncContext startAsync(HttpServletRequestImpl request, ServletRequest servletRequest, ServletResponse servletResponse, AsyncContext asyncContext) throws IllegalStateException {
        throw new PluginException(SandBox.UPGRADE_MESSAGE_ZH);
    }
}
