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

import jakarta.websocket.Decoder;
import jakarta.websocket.Encoder;
import jakarta.websocket.Endpoint;
import jakarta.websocket.Extension;
import jakarta.websocket.HandshakeResponse;
import jakarta.websocket.server.HandshakeRequest;
import jakarta.websocket.server.ServerEndpointConfig;

import java.util.List;
import java.util.Map;

public class ServerEndpointConfigProxy implements ServerEndpointConfig {
    private final ServerEndpointConfig config;
    private final Endpoint endpoint;

    public ServerEndpointConfigProxy(ServerEndpointConfig config, Endpoint endpoint) {
        this.config = config;
        this.endpoint = endpoint;
    }

    @Override
    public Class<?> getEndpointClass() {
        return endpoint.getClass();
    }

    @Override
    public String getPath() {
        return config.getPath();
    }

    @Override
    public List<String> getSubprotocols() {
        return config.getSubprotocols();
    }

    @Override
    public List<Extension> getExtensions() {
        return config.getExtensions();
    }

    @Override
    public Configurator getConfigurator() {
        return new ServerEndpointConfig.Configurator() {
            @Override
            public Configurator getContainerDefaultConfigurator() {
                return config.getConfigurator().getContainerDefaultConfigurator();
            }

            @Override
            public String getNegotiatedSubprotocol(List<String> supported, List<String> requested) {
                return config.getConfigurator().getNegotiatedSubprotocol(supported, requested);
            }

            @Override
            public List<Extension> getNegotiatedExtensions(List<Extension> installed, List<Extension> requested) {
                return config.getConfigurator().getNegotiatedExtensions(installed, requested);
            }

            @Override
            public boolean checkOrigin(String originHeaderValue) {
                return config.getConfigurator().checkOrigin(originHeaderValue);
            }

            @Override
            public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
                config.getConfigurator().modifyHandshake(sec, request, response);
            }

            @Override
            public <T> T getEndpointInstance(Class<T> endpointClass) throws InstantiationException {
                return (T) endpoint;
            }
        };
    }

    @Override
    public List<Class<? extends Encoder>> getEncoders() {
        return config.getEncoders();
    }

    @Override
    public List<Class<? extends Decoder>> getDecoders() {
        return config.getDecoders();
    }

    @Override
    public Map<String, Object> getUserProperties() {
        return config.getUserProperties();
    }
}
