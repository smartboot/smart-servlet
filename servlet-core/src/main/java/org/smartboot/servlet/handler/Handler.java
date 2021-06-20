/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: Handler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;

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
    public abstract void handleRequest(HandlerContext handlerContext);

    /**
     * 执行下一层处理器
     *
     * @param handlerContext
     * @throws Exception
     */
    protected final void doNext(HandlerContext handlerContext) {
        if (nextHandler != null) {
            nextHandler.handleRequest(handlerContext);
        }
    }
}
