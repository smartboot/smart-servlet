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
