/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: AnnotatedEndpoint.java
 * Date: 2021-04-26
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket.impl;

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/26
 */
public class AnnotatedEndpoint extends Endpoint {
    private final List<OnMessageConfig> onMessageConfigs = new ArrayList<>();
    private Object instance;
    private Method onCloseMethod;
    private Method onOpenMethod;
    private Method onErrorMethod;

    public AnnotatedEndpoint(ServerEndpointConfig serverEndpointConfig) {
        try {
            instance = serverEndpointConfig.getEndpointClass().newInstance();
            Class<?> c = serverEndpointConfig.getEndpointClass();
            do {
                for (Method method : serverEndpointConfig.getEndpointClass().getDeclaredMethods()) {
                    if (method.isAnnotationPresent(OnOpen.class)) {
                        if (onOpenMethod == null) {
                            onOpenMethod = method;
                        } else if (overrides(onOpenMethod, method)) {
                            throw new IllegalAccessException("more than one OnOpen annotation");
                        }
                    }
                    if (method.isAnnotationPresent(OnClose.class)) {
                        if (onCloseMethod == null) {
                            onCloseMethod = method;
                        } else if (overrides(onCloseMethod, method)) {
                            throw new IllegalAccessException("more than one OnClose annotation");
                        }
                    }
                    if (method.isAnnotationPresent(OnError.class)) {
                        if (onErrorMethod == null) {
                            onErrorMethod = method;
                        } else if (overrides(onErrorMethod, method)) {
                            throw new IllegalAccessException("more than one OnError annotation");
                        }
                    }
                    if (method.isAnnotationPresent(OnMessage.class)) {
                        OnMessageConfig messageHandler = new OnMessageConfig(method, instance);
                        for (Class<?> paramType : onOpenMethod.getParameterTypes()) {
                            if (paramType == String.class) {
                                messageHandler.setMessageType(String.class);
                                break;
                            }
                            if (paramType == byte[].class) {
                                messageHandler.setMessageType(byte[].class);
                                break;
                            }
                            if (paramType == PongMessage.class) {
                                messageHandler.setMessageType(PongMessage.class);
                                break;
                            }
                        }
                        onMessageConfigs.add(messageHandler);
                    }
                }
                c = c.getSuperclass();
            } while (c != Object.class && c != null);

        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean overrides(Method preMethod, Method method) {
        if (!method.getName().equals(preMethod.getName())) {
            return true;
        }
        if (!method.getReturnType().isAssignableFrom(preMethod.getReturnType())) {
            return true;
        }
        if (method.getParameterTypes().length != preMethod.getParameterTypes().length) {
            return true;
        }
        for (int i = 0; i < method.getParameterTypes().length; ++i) {
            if (method.getParameterTypes()[i] != preMethod.getParameterTypes()[i]) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
        try {
            Object[] args = new Object[onOpenMethod.getParameterTypes().length];
            int i = 0;
            for (Class<?> paramType : onOpenMethod.getParameterTypes()) {
                Object value = null;
                if (Session.class == paramType) {
                    value = session;
                }
                if (EndpointConfig.class == paramType) {
                    value = config;
                }
                args[i++] = value;
            }
            onOpenMethod.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClose(Session session, CloseReason closeReason) {
        try {
            Object[] args = new Object[onCloseMethod.getParameterTypes().length];
            int i = 0;
            for (Class<?> paramType : onCloseMethod.getParameterTypes()) {
                Object value = null;
                if (Session.class == paramType) {
                    value = session;
                }
                if (CloseReason.class == paramType) {
                    value = closeReason;
                }
                args[i++] = value;
            }
            onCloseMethod.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onError(Session session, Throwable thr) {
        try {
            Object[] args = new Object[onErrorMethod.getParameterTypes().length];
            int i = 0;
            for (Class<?> paramType : onErrorMethod.getParameterTypes()) {
                Object value = null;
                if (Session.class == paramType) {
                    value = session;
                }
                if (Throwable.class == paramType) {
                    value = thr;
                }
                args[i++] = value;
            }
            onErrorMethod.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public List<OnMessageConfig> getOnMessageConfigs() {
        return onMessageConfigs;
    }
}
