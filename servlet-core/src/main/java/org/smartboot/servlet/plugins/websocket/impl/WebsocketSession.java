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
import javax.websocket.Extension;
import javax.websocket.MessageHandler;
import javax.websocket.PongMessage;
import javax.websocket.RemoteEndpoint;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import java.io.IOException;
import java.net.URI;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public class WebsocketSession implements Session {

    private final WebSocketContainer container;
    private final AnnotatedEndpoint endpoint;
    private final URI uri;
    private HandlerWrapper textMessageHandler;
    private HandlerWrapper binaryMessageHandler = null;
    private HandlerWrapper pongMessageHandler = null;

    public WebsocketSession(WebSocketContainer container, AnnotatedEndpoint endpoint, URI uri) {
        this.container = container;
        this.endpoint = endpoint;
        this.uri = uri;
    }

    @Override
    public WebSocketContainer getContainer() {
        return container;
    }

    @Override
    public void addMessageHandler(MessageHandler messageHandler) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> void addMessageHandler(Class<T> messageType, MessageHandler.Whole<T> whole) {
        addMessageHandler(new HandlerWrapper(whole, messageType));
    }

    @Override
    public <T> void addMessageHandler(Class<T> messageType, MessageHandler.Partial<T> partial) {
        addMessageHandler(new HandlerWrapper(partial, messageType));
    }

    private <T> void addMessageHandler(HandlerWrapper wrapper) {
        if (wrapper.getMessageType() == String.class) {
            textMessageHandler = wrapper;
        }
        if (wrapper.getMessageType() == byte[].class) {
            binaryMessageHandler = wrapper;
        }
        if (wrapper.getMessageType() == PongMessage.class) {
            pongMessageHandler = wrapper;
        }
    }

    @Override
    public Set<MessageHandler> getMessageHandlers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeMessageHandler(MessageHandler messageHandler) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getProtocolVersion() {
        return null;
    }

    @Override
    public String getNegotiatedSubprotocol() {
        return null;
    }

    @Override
    public List<Extension> getNegotiatedExtensions() {
        return null;
    }

    @Override
    public boolean isSecure() {
        return false;
    }

    @Override
    public boolean isOpen() {
        return false;
    }

    @Override
    public long getMaxIdleTimeout() {
        return 0;
    }

    @Override
    public void setMaxIdleTimeout(long l) {

    }

    @Override
    public int getMaxBinaryMessageBufferSize() {
        return 0;
    }

    @Override
    public void setMaxBinaryMessageBufferSize(int i) {

    }

    @Override
    public int getMaxTextMessageBufferSize() {
        return 0;
    }

    @Override
    public void setMaxTextMessageBufferSize(int i) {

    }

    @Override
    public RemoteEndpoint.Async getAsyncRemote() {
        return null;
    }

    @Override
    public RemoteEndpoint.Basic getBasicRemote() {
        return null;
    }

    @Override
    public String getId() {
        return null;
    }

    @Override
    public void close() throws IOException {
        close(new CloseReason(CloseReason.CloseCodes.NORMAL_CLOSURE, ""));
    }

    @Override
    public void close(CloseReason closeReason) throws IOException {
        endpoint.onClose(this, closeReason);
    }

    @Override
    public URI getRequestURI() {
        return uri;
    }

    @Override
    public Map<String, List<String>> getRequestParameterMap() {
        return null;
    }

    @Override
    public String getQueryString() {
        return null;
    }

    @Override
    public Map<String, String> getPathParameters() {
        return null;
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public Set<Session> getOpenSessions() {
        return null;
    }

    public HandlerWrapper getTextMessageHandler() {
        return textMessageHandler;
    }

    public void setTextMessageHandler(HandlerWrapper textMessageHandler) {
        this.textMessageHandler = textMessageHandler;
    }

    public HandlerWrapper getBinaryMessageHandler() {
        return binaryMessageHandler;
    }

    public void setBinaryMessageHandler(HandlerWrapper binaryMessageHandler) {
        this.binaryMessageHandler = binaryMessageHandler;
    }

    public HandlerWrapper getPongMessageHandler() {
        return pongMessageHandler;
    }
}
