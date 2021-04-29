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
import java.lang.reflect.Method;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/26
 */
public class HandlerWrapper {
    private final MessageHandler.Whole<?> wholeHandler;
    private final MessageHandler.Partial<?> partialHandler;
    private final Class<?> messageType;
    private final boolean partial;
    private Method method;
    private Object instance;

    public HandlerWrapper(MessageHandler.Whole<?> wholeHandler, final Class<?> messageType) {
        this(wholeHandler, null, messageType, false);
    }

    public HandlerWrapper(MessageHandler.Partial<?> partialHandler, final Class<?> messageType) {
        this(null, partialHandler, messageType, true);
    }

    public HandlerWrapper(Method method, Object instance) {
        this(null, null, null, false);
        this.method = method;
        this.instance = instance;
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

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
    }

    public Object getInstance() {
        return instance;
    }

    public void setInstance(Object instance) {
        this.instance = instance;
    }
}