/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.springframework.web.socket.server.standard;

import org.smartboot.servlet.WebSocketServerContainer;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.server.HandshakeFailureException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TomcatRequestUpgradeStrategy extends AbstractStandardUpgradeStrategy {

    @Override
    protected void upgradeInternal(ServerHttpRequest request, ServerHttpResponse response, String selectedProtocol, List<Extension> selectedExtensions, Endpoint endpoint) throws HandshakeFailureException {
        HttpServletRequestImpl servletRequest = (HttpServletRequestImpl)getHttpServletRequest(request);
        HttpServletResponse servletResponse = getHttpServletResponse(response);

        StringBuffer requestUrl = servletRequest.getRequestURL();
        String path = servletRequest.getRequestURI();  // shouldn't matter
        Map<String, String> pathParams = Collections.<String, String>emptyMap();

        ServerEndpointRegistration endpointConfig = new ServerEndpointRegistration(path, endpoint);
        endpointConfig.setSubprotocols(Collections.singletonList(selectedProtocol));
        endpointConfig.setExtensions(selectedExtensions);

        try {
            getContainer(servletRequest).doUpgrade(servletRequest, servletResponse, endpointConfig, endpoint, pathParams);
        } catch (ServletException ex) {
            throw new HandshakeFailureException(
                    "Servlet request failed to upgrade to WebSocket: " + requestUrl, ex);
        } catch (IOException ex) {
            throw new HandshakeFailureException(
                    "Response update failed during upgrade to WebSocket: " + requestUrl, ex);
        }
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[]{"13"};
    }

    @Override
    protected WebSocketServerContainer getContainer(HttpServletRequest request) {
        return (WebSocketServerContainer) super.getContainer(request);
    }
}
