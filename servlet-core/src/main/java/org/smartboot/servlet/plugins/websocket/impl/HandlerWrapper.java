/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
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