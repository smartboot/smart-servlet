/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: HandlerWrapper.java
 * Date: 2021-04-24
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket.impl;

import javax.websocket.MessageHandler;

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