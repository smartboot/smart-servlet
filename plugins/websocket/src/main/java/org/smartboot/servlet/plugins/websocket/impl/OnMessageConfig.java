/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: AnnotatedMessageHandler.java
 * Date: 2021-04-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket.impl;

import java.lang.reflect.Method;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/27
 */
public class OnMessageConfig {
    private final Method method;
    private final Object instance;
    private Class<?> messageType;

    public OnMessageConfig(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
    }

    public Method getMethod() {
        return method;
    }

    public Object getInstance() {
        return instance;
    }

    public Class getMessageType() {
        return messageType;
    }

    public void setMessageType(Class<?> messageType) {
        this.messageType = messageType;
    }
}
