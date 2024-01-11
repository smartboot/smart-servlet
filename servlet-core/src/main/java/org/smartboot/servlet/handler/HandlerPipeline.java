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
 * @author 三刀
 * @version V1.0 , 2019/11/3
 */
public final class HandlerPipeline extends Handler {
    /**
     * 管道尾
     */
    private Handler tailHandler;

    /**
     * 添加HttpHandle至末尾
     *
     * @param handle 尾部handle
     * @return 当前管道对象
     */
    public HandlerPipeline next(Handler handle) {
        if (nextHandler == null) {
            nextHandler = tailHandler = handle;
            return this;
        }
        Handler httpHandle = tailHandler;
        while (httpHandle.nextHandler != null) {
            httpHandle = httpHandle.nextHandler;
        }
        httpHandle.nextHandler = handle;
        return this;
    }

    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        nextHandler.handleRequest(handlerContext);
    }
}
