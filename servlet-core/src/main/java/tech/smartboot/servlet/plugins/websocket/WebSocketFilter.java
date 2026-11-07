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

package tech.smartboot.servlet.plugins.websocket;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.server.ServerContainer;
import tech.smartboot.feat.core.common.FeatUtils;
import tech.smartboot.feat.core.common.HeaderName;
import tech.smartboot.feat.core.common.HeaderValue;
import tech.smartboot.servlet.plugins.websocket.impl.AnnotatedEndpoint;
import tech.smartboot.servlet.plugins.websocket.impl.AnnotatedEndpointConfig;
import tech.smartboot.servlet.plugins.websocket.impl.PathNode;
import tech.smartboot.servlet.plugins.websocket.impl.ServerEndpointConfigProxy;
import tech.smartboot.servlet.plugins.websocket.impl.WebSocketServerContainerImpl;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSocketFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        //nothing
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        String upgrade = req.getHeader(HeaderName.UPGRADE.getName());
        if (!HeaderValue.Upgrade.WEBSOCKET.equalsIgnoreCase(upgrade)) {
            chain.doFilter(request, response);
            return;
        }
        WebSocketServerContainerImpl container = (WebSocketServerContainerImpl) request.getServletContext().getAttribute(ServerContainer.class.getName());
        List<PathNode> requestPathNodes = PathNode.convertToPathNodes(req.getRequestURI());
        for (AnnotatedEndpointConfig serverEndpointConfig : container.getEndpointConfigs()) {
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
                } else if (!FeatUtils.equals(requestNode.getNodeName(), node.getNodeName())) {
                    matched = false;
                    break;
                }
            }
            //匹配成功
            if (matched) {
                AnnotatedEndpoint endpoint = new AnnotatedEndpoint(serverEndpointConfig, matchData);
                ServerEndpointConfigProxy proxy = new ServerEndpointConfigProxy(serverEndpointConfig.getServerEndpointConfig(), endpoint);
                try {
                    container.upgradeHttpToWebSocket(req, resp, proxy, matchData);
                } catch (DeploymentException e) {
                    throw new ServletException(e);
                }
                return;
            }
        }
        //匹配失败
        chain.doFilter(request, response);
    }
}
