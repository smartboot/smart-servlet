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
