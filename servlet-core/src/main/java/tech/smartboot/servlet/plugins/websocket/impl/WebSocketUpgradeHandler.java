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

import jakarta.servlet.http.HttpUpgradeHandler;
import jakarta.servlet.http.WebConnection;
import jakarta.websocket.CloseReason;
import jakarta.websocket.PongMessage;
import tech.smartboot.feat.core.common.utils.WebSocketUtil;
import tech.smartboot.feat.core.server.HttpRequest;
import tech.smartboot.feat.core.server.WebSocketRequest;
import tech.smartboot.feat.core.server.WebSocketResponse;
import tech.smartboot.servlet.impl.WebConnectionImpl;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class WebSocketUpgradeHandler implements HttpUpgradeHandler {
    private HttpRequest request;

    @Override
    public void init(WebConnection wc) {
        WebConnectionImpl webConnection = (WebConnectionImpl) wc;
        this.request = webConnection.getRequest();
//        webConnection.getRequest().upgrade(new WebsocketProviderImpl());
    }

    public void upgrade(WebsocketSession session) throws IOException {
        final ClassLoader servletClassLoader = Thread.currentThread().getContextClassLoader();
        request.upgrade(new tech.smartboot.feat.core.server.upgrade.websocket.WebSocketUpgradeHandler() {
            @Override
            public void handle(WebSocketRequest request, WebSocketResponse response) throws Throwable {
                final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                try {
                    Thread.currentThread().setContextClassLoader(servletClassLoader);
                    switch (request.getFrameOpcode()) {
                        case WebSocketUtil.OPCODE_TEXT:
                            if (session.getTextMessageHandler() == null) {
                                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "none messageHandler found"));
                            } else {
                                WebSocketUpgradeHandler.this.handleTextMessage(session.getTextMessageHandler(), new String(request.getPayload(), StandardCharsets.UTF_8));
                            }
                            break;
                        case WebSocketUtil.OPCODE_BINARY:
                            if (session.getBinaryMessageHandler() == null) {
                                session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "none messageHandler found"));
                            } else {
                                WebSocketUpgradeHandler.this.handleBinaryMessage(session.getBinaryMessageHandler(), request.getPayload());
                            }

                            break;
                        case WebSocketUtil.OPCODE_CLOSE:
                            try {
                                WebSocketUpgradeHandler.this.onClose(request, response);
                            } finally {
                                response.close();
                            }
                            break;
                        case WebSocketUtil.OPCODE_PING:
//                            onPing(request, response);
                            throw new UnsupportedOperationException();
//                            break;
                        case WebSocketUtil.OPCODE_PONG:
                            onPong(request, session.getPongMessageHandler());
                            break;
                        default:
                            throw new UnsupportedOperationException();
                    }

                } catch (Throwable throwable) {
                    WebSocketUpgradeHandler.this.onError(throwable);
                } finally {
                    Thread.currentThread().setContextClassLoader(classLoader);
                }
            }
        });
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
//        throw new UnsupportedOperationException();
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

    @Override
    public void destroy() {

    }
}
