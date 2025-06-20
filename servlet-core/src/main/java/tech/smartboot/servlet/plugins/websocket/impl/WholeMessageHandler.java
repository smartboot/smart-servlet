/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.websocket.impl;

import jakarta.websocket.MessageHandler;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

class WholeMessageHandler<T> implements MessageHandler.Whole<T> {
    private final OnMessageConfig messageConfig;
    private final Session session;
    private final Map<String, String> data;

    public WholeMessageHandler(OnMessageConfig messageConfig, Session session, Map<String, String> data) {
        this.messageConfig = messageConfig;
        this.session = session;
        this.data = data;
    }

    @Override
    public void onMessage(T message) {
        try {
            Method method = messageConfig.getMethod();
            Object[] args = new Object[method.getParameterTypes().length];
            int i = 0;
            for (Class<?> paramType : method.getParameterTypes()) {
                Object value = null;
                PathParam pathParam = null;
                for (Annotation annotation : messageConfig.getAnnotations()[i]) {
                    if (annotation.annotationType() == PathParam.class) {
                        pathParam = (PathParam) annotation;
                    }
                }
                if (pathParam != null) {
                    value = data.get(pathParam.value());
                } else if (Session.class == paramType) {
                    value = session;
                } else if (messageConfig.getMessageType() == paramType) {
                    value = message;
                }
                args[i++] = value;
            }
            method.invoke(messageConfig.getInstance(), args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
