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

package tech.smartboot.servlet.plugins.websocket.impl;

import jakarta.websocket.MessageHandler;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/26
 */
public class HandlerWrapper {
    private final MessageHandler.Whole<?> wholeHandler;
    private final MessageHandler.Partial<?> partialHandler;
    private final Class<?> messageType;
    private final boolean partial;

    public HandlerWrapper(MessageHandler.Whole<?> wholeHandler, final Class<?> messageType) {
        this(wholeHandler, null, messageType, false);
    }

    public HandlerWrapper(MessageHandler.Partial<?> partialHandler, final Class<?> messageType) {
        this(null, partialHandler, messageType, true);
    }

    HandlerWrapper(MessageHandler.Whole<?> wholeHandler, MessageHandler.Partial<?> partialHandler, Class<?> messageType, boolean partial) {
        this.wholeHandler = wholeHandler;
        this.partialHandler = partialHandler;
        this.messageType = messageType;
        this.partial = partial;
    }


    public Class getMessageType() {
        return messageType;
    }

    public MessageHandler.Whole getWholeHandler() {
        return wholeHandler;
    }

    public MessageHandler.Partial getPartialHandler() {
        return partialHandler;
    }

    public boolean isPartial() {
        return partial;
    }
}