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

import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
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

    private final SmartServerEndpointConfig smartServerEndpointConfig;
    private final Map<String, String> uriData;

    public AnnotatedEndpoint(SmartServerEndpointConfig config, Map<String, String> uriData) {
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
