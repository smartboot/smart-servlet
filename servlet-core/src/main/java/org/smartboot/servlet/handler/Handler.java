/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.handler;

import javax.servlet.ServletException;

import java.io.IOException;

/**
 * 请求处理器
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
abstract class Handler {
    /**
     * 持有下一个处理器的句柄
     */
    protected Handler nextHandler;

    /**
     * 处理handlerContext中的请求
     *
     * @param handlerContext
     * @throws Exception
     */
    public abstract void handleRequest(HandlerContext handlerContext) throws ServletException, IOException;

    /**
     * 执行下一层处理器
     *
     * @param handlerContext
     * @throws Exception
     */
    protected final void doNext(HandlerContext handlerContext) throws ServletException, IOException {
        if (nextHandler != null) {
            nextHandler.handleRequest(handlerContext);
        }
    }
}
