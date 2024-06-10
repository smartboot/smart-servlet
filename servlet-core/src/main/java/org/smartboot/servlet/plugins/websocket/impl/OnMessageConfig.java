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

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/27
 */
public class OnMessageConfig {
    private final Method method;
    private final Object instance;
    private final Annotation[][] annotations;
    private Class<?> messageType;

    public OnMessageConfig(Method method, Object instance) {
        this.method = method;
        this.instance = instance;
        this.annotations = method.getParameterAnnotations();
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

    public Annotation[][] getAnnotations() {
        return annotations;
    }
}
