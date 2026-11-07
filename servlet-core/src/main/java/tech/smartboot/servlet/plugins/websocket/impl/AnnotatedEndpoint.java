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

import jakarta.websocket.CloseReason;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import jakarta.websocket.server.PathParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Objects;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/4/26
 */
public class AnnotatedEndpoint extends Endpoint {

    private final AnnotatedEndpointConfig smartServerEndpointConfig;
    private final Map<String, String> uriData;

    public AnnotatedEndpoint(AnnotatedEndpointConfig config, Map<String, String> uriData) {
        Objects.requireNonNull(config, "SmartServerEndpointConfig cannot be null");
        this.smartServerEndpointConfig = config;
        this.uriData = uriData;
    }


    @Override
    public void onOpen(Session session, EndpointConfig config) {
        if (!Objects.equals(config, smartServerEndpointConfig.getServerEndpointConfig())) {
            throw new IllegalArgumentException();
        }
        //注册 OnMessage 回调
        smartServerEndpointConfig.getOnMessageConfigs().forEach(messageConfig -> session.addMessageHandler(messageConfig.getMessageType(), new WholeMessageHandler<>(messageConfig, session, uriData)));
        if (smartServerEndpointConfig.getOnOpenMethod() == null) {
            return;
        }
        Object[] args = getParamValues(smartServerEndpointConfig.getOnOpenMethod(), smartServerEndpointConfig.getOnOpenAnnotations(), session, null, null);
        invoke(smartServerEndpointConfig.getOnOpenMethod(), args);
    }


    @Override
    public void onClose(Session session, CloseReason closeReason) {
        if (smartServerEndpointConfig.getOnErrorMethod() != null) {
            Object[] args = getParamValues(smartServerEndpointConfig.getOnCloseMethod(), smartServerEndpointConfig.getOnCloseAnnotations(), session, closeReason, null);
            invoke(smartServerEndpointConfig.getOnCloseMethod(), args);
        }
    }


    @Override
    public void onError(Session session, Throwable thr) {
        Object[] args = getParamValues(smartServerEndpointConfig.getOnErrorMethod(), smartServerEndpointConfig.getOnErrorAnnotations(), session, null, thr);
        invoke(smartServerEndpointConfig.getOnErrorMethod(), args);
    }

    private void invoke(Method method, Object[] args) {
        try {
            method.invoke(smartServerEndpointConfig.getInstance(), args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private Object[] getParamValues(Method method, Annotation[][] annotations, Session session, CloseReason closeReason, Throwable thr) {
        Object[] args = new Object[method.getParameterTypes().length];
        int i = 0;
        for (Class<?> paramType : method.getParameterTypes()) {
            Object value = null;
            if (Session.class == paramType) {
                value = session;
            }
            if (CloseReason.class == paramType) {
                value = closeReason;
            }
            if (Throwable.class == paramType) {
                value = thr;
            }
            if (EndpointConfig.class == paramType) {
                value = smartServerEndpointConfig.getServerEndpointConfig().getEndpointClass();
            }
            for (Annotation annotation : annotations[i]) {
                if (annotation.annotationType() == PathParam.class) {
                    value = uriData.get(((PathParam) annotation).value());
                }
            }
            args[i++] = value;
        }

        return args;
    }
}
