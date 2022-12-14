/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: HandlePipeline.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;

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
    public void handleRequest(HandlerContext handlerContext) {
        nextHandler.handleRequest(handlerContext);
    }
}
