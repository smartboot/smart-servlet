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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.Extension;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerContainer;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public class WebSocketServerContainerImpl implements ServerContainer {
    private final Set<Class<?>> endpointClassSet = new HashSet<>();
    private final Map<ServerEndpointConfig, AnnotatedEndpointConfig> endpointConfigs =
            new HashMap<>();
    private boolean deployed = false;
    private final List<Extension> installedExtensions = Collections.emptyList();


    @Override
    public void addEndpoint(Class<?> endpointClass) throws DeploymentException {
        if (deployed) {
            throw new DeploymentException("websocket container has already been deployed");
        }
        if (endpointClassSet.contains(endpointClass)) {
            return;
        }
        endpointClassSet.add(endpointClass);
        try {
            ServerEndpoint serverEndpoint = endpointClass.getAnnotation(ServerEndpoint.class);
            Class<? extends ServerEndpointConfig.Configurator> configuratorClass = serverEndpoint.configurator();
            ServerEndpointConfig serverEndpointConfig =
                    ServerEndpointConfig.Builder.create(endpointClass, serverEndpoint.value()).decoders(Arrays.asList(serverEndpoint.decoders())).encoders(Arrays.asList(serverEndpoint.encoders())).subprotocols(Arrays.asList(serverEndpoint.subprotocols())).encoders(Collections.emptyList()).configurator(configuratorClass.newInstance()).build();
            addEndpoint(serverEndpointConfig);
        } catch (InstantiationException | IllegalAccessException e) {
            throw new DeploymentException("", e);
        }
    }

    @Override
    public void addEndpoint(ServerEndpointConfig serverConfig) throws DeploymentException {
        if (deployed) {
            throw new DeploymentException("");
        }
        endpointConfigs.put(serverConfig, new AnnotatedEndpointConfig(serverConfig));
    }

    @Override
    public void upgradeHttpToWebSocket(Object httpServletRequest, Object httpServletResponse, ServerEndpointConfig sec, Map<String, String> pathParameters) throws IOException, DeploymentException {

        HttpServletRequest request = (HttpServletRequest) httpServletRequest;
        HttpServletResponse response = (HttpServletResponse) httpServletResponse;

        HandshakeRequestImpl handshakeRequest = new HandshakeRequestImpl(request);
        HandshakeResponseImpl handshakeResponse = new HandshakeResponseImpl(response);
        ServerEndpointConfig.Configurator c = sec.getConfigurator();
        c.modifyHandshake(sec, handshakeRequest, handshakeResponse);

        try {
            Object t = sec.getConfigurator().getEndpointInstance(sec.getEndpointClass());
            Endpoint endpoint = (Endpoint) t;
            WebsocketSession websocketSession = new WebsocketSession(this, endpoint, URI.create(request.getRequestURI()));
            endpoint.onOpen(websocketSession, sec);
            WebSocketUpgradeHandler handler = request.upgrade(WebSocketUpgradeHandler.class);
            handler.upgrade(websocketSession);
        } catch (InstantiationException | ServletException e) {
            throw new DeploymentException("", e);
        }

    }

    @Override
    public long getDefaultAsyncSendTimeout() {
        return 0;
    }

    @Override
    public void setAsyncSendTimeout(long timeoutmillis) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session connectToServer(Object annotatedEndpointInstance, URI path) throws DeploymentException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session connectToServer(Class<?> annotatedEndpointClass, URI path) throws DeploymentException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session connectToServer(Endpoint endpointInstance, ClientEndpointConfig cec, URI path) throws DeploymentException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Session connectToServer(Class<? extends Endpoint> endpointClass, ClientEndpointConfig cec, URI path) throws DeploymentException, IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public long getDefaultMaxSessionIdleTimeout() {
        return 0;
    }

    @Override
    public void setDefaultMaxSessionIdleTimeout(long timeout) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultMaxBinaryMessageBufferSize() {
        return 0;
    }

    @Override
    public void setDefaultMaxBinaryMessageBufferSize(int max) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getDefaultMaxTextMessageBufferSize() {
        return 0;
    }

    @Override
    public void setDefaultMaxTextMessageBufferSize(int max) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<Extension> getInstalledExtensions() {
        return new HashSet<>(installedExtensions);
    }

    public void deployComplete() {
        deployed = true;
    }

    public Collection<AnnotatedEndpointConfig> getEndpointConfigs() {
        return endpointConfigs.values();
    }

}
