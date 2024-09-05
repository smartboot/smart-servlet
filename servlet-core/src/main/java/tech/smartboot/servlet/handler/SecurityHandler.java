/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.handler;

import jakarta.servlet.ServletException;

import java.io.IOException;

public class SecurityHandler extends Handler {
    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        if (handlerContext.getServletContext().getRuntime().getSecurityProvider().login(handlerContext.getOriginalRequest(), handlerContext.getResponse(), handlerContext.getServletInfo())) {
            doNext(handlerContext);
        }
    }
}
