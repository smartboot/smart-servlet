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

    public HandlerPipeline head(Handler handle) {
        if (handle == null || handle.nextHandler != null) {
            throw new IllegalArgumentException("head handle must be null or head handle nextHandler must be null");
        }
        handle.nextHandler = nextHandler;
        nextHandler = handle;
        return this;
    }

    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        nextHandler.handleRequest(handlerContext);
    }
}
