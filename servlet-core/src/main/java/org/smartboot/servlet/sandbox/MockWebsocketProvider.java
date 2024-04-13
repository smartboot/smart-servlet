/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.WebSocketServerContainer;
import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.provider.WebsocketProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpointConfig;
import java.io.IOException;
import java.net.URI;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/30
 */
public class MockWebsocketProvider implements WebsocketProvider {

    @Override
    public WebSocketServerContainer getWebSocketServerContainer() {
        return new WebSocketServerContainer() {
            @Override
            public void doUpgrade(HttpServletRequest request, HttpServletResponse response, ServerEndpointConfig sec, Endpoint endpoint, Map<String, String> pathParams) throws ServletException, IOException {
                throw new PluginException(SandBox.UPGRADE_MESSAGE_ZH);
            }

            @Override
            public void addEndpoint(Class<?> endpointClass) throws DeploymentException {

            }

            @Override
            public void addEndpoint(ServerEndpointConfig serverConfig) throws DeploymentException {

            }

            @Override
            public long getDefaultAsyncSendTimeout() {
                return 0;
            }

            @Override
            public void setAsyncSendTimeout(long timeoutmillis) {

            }

            @Override
            public Session connectToServer(Object annotatedEndpointInstance, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Class<?> annotatedEndpointClass, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Endpoint endpointInstance, ClientEndpointConfig cec, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Class<? extends Endpoint> endpointClass, ClientEndpointConfig cec, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public long getDefaultMaxSessionIdleTimeout() {
                return 0;
            }

            @Override
            public void setDefaultMaxSessionIdleTimeout(long timeout) {

            }

            @Override
            public int getDefaultMaxBinaryMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxBinaryMessageBufferSize(int max) {

            }

            @Override
            public int getDefaultMaxTextMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxTextMessageBufferSize(int max) {

            }

            @Override
            public Set<Extension> getInstalledExtensions() {
                return Collections.emptySet();
            }
        };
    }

    @Override
    public void doHandle(WebSocketRequest request, WebSocketResponse response) {
        throw new PluginException(SandBox.UPGRADE_MESSAGE_ZH);
    }
}
