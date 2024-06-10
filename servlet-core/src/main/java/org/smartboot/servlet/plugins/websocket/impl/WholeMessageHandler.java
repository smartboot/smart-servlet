package org.smartboot.servlet.plugins.websocket.impl;

import javax.websocket.MessageHandler;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
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
