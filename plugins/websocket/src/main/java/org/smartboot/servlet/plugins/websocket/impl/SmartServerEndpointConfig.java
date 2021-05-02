/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServerEndpointConfigImpl.java
 * Date: 2021-04-24
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket.impl;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.PongMessage;
import javax.websocket.server.ServerEndpointConfig;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public class SmartServerEndpointConfig {
    private final ServerEndpointConfig serverEndpointConfig;
    private final List<OnMessageConfig> onMessageConfigs = new ArrayList<>();
    private final List<PathNode> pathNodes;
    private Object instance;
    private Method onCloseMethod;
    private Method onOpenMethod;
    private Method onErrorMethod;

    public SmartServerEndpointConfig(ServerEndpointConfig serverEndpointConfig) {
        this.serverEndpointConfig = serverEndpointConfig;
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
        pathNodes = PathNode.convertToPathNodes(serverEndpointConfig.getPath());
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

    public ServerEndpointConfig getServerEndpointConfig() {
        return serverEndpointConfig;
    }

    public List<OnMessageConfig> getOnMessageConfigs() {
        return onMessageConfigs;
    }

    public List<PathNode> getPathNodes() {
        return pathNodes;
    }

    public Object getInstance() {
        return instance;
    }

    public Method getOnCloseMethod() {
        return onCloseMethod;
    }

    public Method getOnOpenMethod() {
        return onOpenMethod;
    }

    public Method getOnErrorMethod() {
        return onErrorMethod;
    }
}
