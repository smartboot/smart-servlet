/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.async;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.provider.AsyncContextProvider;

public class AsyncContextProviderImpl implements AsyncContextProvider {
    @Override
    public AsyncContext startAsync(HttpServletRequestImpl request, ServletRequest servletRequest, ServletResponse servletResponse, AsyncContext asyncContext) throws IllegalStateException {
        return new AsyncContextImpl(request.getServletContext().getRuntime(), request, servletRequest, servletResponse, request.getCompletableFuture(), (AsyncContextImpl) asyncContext);
    }
}
