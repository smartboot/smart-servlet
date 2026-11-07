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

package tech.smartboot.servlet.handler;

import jakarta.servlet.ServletException;

import java.io.IOException;

/**
 * 请求处理器
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public abstract class Handler {
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
