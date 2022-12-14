/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebsocketProviderImpl.java
 * Date: 2021-04-25
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket;

import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.plugins.websocket.impl.AnnotatedEndpoint;
import org.smartboot.servlet.plugins.websocket.impl.HandlerWrapper;
import org.smartboot.servlet.plugins.websocket.impl.PathNode;
import org.smartboot.servlet.plugins.websocket.impl.SmartServerEndpointConfig;
import org.smartboot.servlet.plugins.websocket.impl.WebsocketServerContainer;
import org.smartboot.servlet.plugins.websocket.impl.WebsocketSession;
import org.smartboot.servlet.provider.WebsocketProvider;

import javax.websocket.PongMessage;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/31
 */
public class WebsocketProviderImpl implements WebsocketProvider {
    private final WebsocketServerContainer container;

    public WebsocketProviderImpl(WebsocketServerContainer container) {
        this.container = container;
    }

    @Override
    public void doHandle(ServletContextRuntime runtime, WebSocketRequest request, WebSocketResponse response) {
        try {
            WebsocketSession session = request.getAttachment();
            switch (request.getFrameOpcode()) {
                case WebSocketRequestImpl.OPCODE_TEXT:
                    handleTextMessage(session.getTextMessageHandler(), new String(request.getPayload(), StandardCharsets.UTF_8));
                    break;
                case WebSocketRequestImpl.OPCODE_BINARY:
                    handleBinaryMessage(session.getBinaryMessageHandler(), request.getPayload());
                    break;
                case WebSocketRequestImpl.OPCODE_CLOSE:
                    try {
                        onClose(request, response);
                    } finally {
                        response.close();
                    }
                    break;
                case WebSocketRequestImpl.OPCODE_PING:
//                            onPing(request, response);
                    throw new UnsupportedOperationException();
//                            break;
                case WebSocketRequestImpl.OPCODE_PONG:
                    onPong(request, session.getPongMessageHandler());
                    break;
                default:
                    throw new UnsupportedOperationException();
            }

        } catch (Throwable throwable) {
            onError(throwable);
            throw throwable;
        }
    }

    public void onHandShark(ServletContextRuntime runtime, WebSocketRequest request, WebSocketResponse response) {
        try {
            SmartServerEndpointConfig matchedServerEndpointConfig = null;
            Map<String, String> data = new HashMap<>();
            List<PathNode> requestPathNodes = PathNode.convertToPathNodes(request.getRequestURI());
            for (SmartServerEndpointConfig serverEndpointConfig : container.getEndpointConfigs()) {
                List<PathNode> pathNodes = serverEndpointConfig.getPathNodes();
                if (requestPathNodes.size() != pathNodes.size()) {
                    continue;
                }
                //是否匹配成功
                boolean matched = true;
                Map<String, String> matchData = new HashMap<>();
                for (int i = 0; i < pathNodes.size(); i++) {
                    PathNode node = pathNodes.get(i);
                    PathNode requestNode = requestPathNodes.get(i);
                    if (node.isPatternMatching()) {
                        matchData.put(node.getNodeName(), requestNode.getNodeName());
                    } else if (!StringUtils.equals(requestNode.getNodeName(), node.getNodeName())) {
                        matched = false;
                        break;
                    }
                }
                //匹配成功
                if (matched) {
                    data = matchData;
                    matchedServerEndpointConfig = serverEndpointConfig;
                    break;
                }
            }
            //匹配失败
            if (matchedServerEndpointConfig == null) {
                response.close();
                return;
            }
            AnnotatedEndpoint endpoint = new AnnotatedEndpoint(matchedServerEndpointConfig, data);

            WebsocketSession websocketSession = new WebsocketSession(container, endpoint, URI.create(request.getRequestURI()));
            request.setAttachment(websocketSession);

            //注册 OnMessage 回调
            Map<String, String> finalData = data;
            matchedServerEndpointConfig.getOnMessageConfigs().forEach(messageConfig -> {
                websocketSession.addMessageHandler(messageConfig.getMessageType(), message -> {
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
                                value = finalData.get(pathParam.value());
                            } else if (Session.class == paramType) {
                                value = websocketSession;
                            } else if (messageConfig.getMessageType() == paramType) {
                                value = message;
                            }
                            args[i++] = value;
                        }
                        method.invoke(messageConfig.getInstance(), args);
                    } catch (IllegalAccessException | InvocationTargetException e) {
                        e.printStackTrace();
                    }
                });
            });
            endpoint.onOpen(websocketSession, matchedServerEndpointConfig.getServerEndpointConfig());

        } catch (Throwable e) {
            onError(e);
            response.close();
        }
    }

    private void handleTextMessage(HandlerWrapper handler, String message) {
        if (handler.isPartial()) {
            if (handler.getMessageType() == String.class) {
                handler.getPartialHandler().onMessage(message, true);
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            if (handler.getMessageType() == String.class) {
                handler.getWholeHandler().onMessage(message);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

    private void onError(Throwable throwable) {
        throwable.printStackTrace();
    }

    private void onClose(WebSocketRequest request, WebSocketResponse response) {
        throw new UnsupportedOperationException();
    }

    private void onPong(WebSocketRequest request, HandlerWrapper handler) {
        PongMessage message = null;
        handler.getWholeHandler().onMessage(message);
    }

    private void handleBinaryMessage(HandlerWrapper handler, byte[] data) {
        if (handler.isPartial()) {
            if (handler.getMessageType() == byte[].class) {
                handler.getPartialHandler().onMessage(data, true);
            } else {
                throw new UnsupportedOperationException();
            }
        } else {
            if (handler.getMessageType() == byte[].class) {
                handler.getWholeHandler().onMessage(data);
            } else {
                throw new UnsupportedOperationException();
            }
        }
    }

}
