/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.plugins.websocket;

import org.smartboot.http.common.utils.WebSocketUtil;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.servlet.WebSocketServerContainer;
import org.smartboot.servlet.plugins.websocket.impl.HandlerWrapper;
import org.smartboot.servlet.plugins.websocket.impl.WebSocketServerContainerImpl;
import org.smartboot.servlet.plugins.websocket.impl.WebsocketSession;
import org.smartboot.servlet.provider.WebsocketProvider;
import org.smartboot.socket.util.Attachment;

import javax.websocket.CloseReason;
import javax.websocket.PongMessage;
import java.nio.charset.StandardCharsets;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/31
 */
public class WebsocketProviderImpl implements WebsocketProvider {

    private final WebSocketServerContainerImpl serverContainer = new WebSocketServerContainerImpl();

    @Override
    public WebSocketServerContainer getWebSocketServerContainer() {
        return serverContainer;
    }

    @Override
    public void doHandle(WebSocketRequest request, WebSocketResponse response) {
        Attachment attachment = ((WebSocketRequestImpl) request).getAttachment();
        WebsocketSession session = (WebsocketSession) attachment.get(WebsocketProvider.WEBSOCKET_SESSION_ATTACH_KEY);
        try {
            switch (request.getFrameOpcode()) {
                case WebSocketUtil.OPCODE_TEXT:
                    if (session.getTextMessageHandler() == null) {
                        session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "none messageHandler found"));
                    } else {
                        handleTextMessage(session.getTextMessageHandler(), new String(request.getPayload(), StandardCharsets.UTF_8));
                    }
                    break;
                case WebSocketUtil.OPCODE_BINARY:
                    if (session.getBinaryMessageHandler() == null) {
                        session.close(new CloseReason(CloseReason.CloseCodes.CANNOT_ACCEPT, "none messageHandler found"));
                    } else {
                        handleBinaryMessage(session.getBinaryMessageHandler(), request.getPayload());
                    }

                    break;
                case WebSocketUtil.OPCODE_CLOSE:
                    try {
                        onClose(request, response);
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
            onError(throwable);
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
