/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebsocketProviderImpl.java
 * Date: 2021-04-25
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.exception.HttpException;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.servlet.ApplicationRuntime;
import org.smartboot.servlet.plugins.websocket.impl.AnnotatedEndpoint;
import org.smartboot.servlet.plugins.websocket.impl.HandlerWrapper;
import org.smartboot.servlet.plugins.websocket.impl.SmartServerEndpointConfig;
import org.smartboot.servlet.plugins.websocket.impl.WebsocketServerContainer;
import org.smartboot.servlet.plugins.websocket.impl.WebsocketSession;
import org.smartboot.servlet.provider.WebsocketProvider;

import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.Session;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.nio.charset.StandardCharsets;

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
    public void doHandle(ApplicationRuntime runtime, WebSocketRequest request, WebSocketResponse response) {
        try {
            switch (request.getWebsocketStatus()) {
                case HandShake:
                    onHandShark(runtime, request, response);
                    break;
                case DataFrame: {
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
                }
                break;
                default:
                    throw new HttpException(HttpStatus.BAD_REQUEST);
            }
        } catch (Throwable throwable) {
            onError(throwable);
            throw throwable;
        }
    }

    private void onHandShark(ApplicationRuntime runtime, WebSocketRequest request, WebSocketResponse response) {
        try {
            SmartServerEndpointConfig serverEndpointConfig = container.match(runtime.getContextPath(), request);
            AnnotatedEndpoint endpoint = serverEndpointConfig.getEndpoint();

            WebsocketSession websocketSession = new WebsocketSession(container, endpoint, URI.create(request.getRequestURI()));
            request.setAttachment(websocketSession);

            //注册 OnMessage 回调
            endpoint.getOnMessageConfigs().forEach(messageConfig -> {
                websocketSession.addMessageHandler(messageConfig.getMessageType(), new MessageHandler.Whole<Object>() {
                    @Override
                    public void onMessage(Object message) {
                        try {
                            Method method = messageConfig.getMethod();
                            Object[] args = new Override[method.getParameterTypes().length];
                            int i = 0;
                            for (Class<?> paramType : method.getParameterTypes()) {
                                Object value = null;
                                if (Session.class == paramType) {
                                    value = websocketSession;
                                }
                                if (messageConfig.getMessageType() == paramType) {
                                    value = message;
                                }
                                args[i++] = value;
                            }
                            method.invoke(messageConfig.getInstance(), args);
                        } catch (IllegalAccessException | InvocationTargetException e) {
                            e.printStackTrace();
                        }
                    }
                });
            });
            endpoint.onOpen(websocketSession, serverEndpointConfig.getServerEndpointConfig());

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
