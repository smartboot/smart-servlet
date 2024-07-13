package org.smartboot.servlet.plugins.websocket;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.server.ServerContainer;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.plugins.websocket.impl.PathNode;
import org.smartboot.servlet.plugins.websocket.impl.SmartServerEndpointConfig;
import org.smartboot.servlet.plugins.websocket.impl.WebSocketServerContainerImpl;

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
        if (req.getHeader(HeaderNameEnum.UPGRADE.getName()) == null) {
            chain.doFilter(request, response);
            return;
        }
        WebSocketServerContainerImpl container = (WebSocketServerContainerImpl) request.getServletContext().getAttribute(ServerContainer.class.getName());
        List<PathNode> requestPathNodes = PathNode.convertToPathNodes(req.getRequestURI());
        for (SmartServerEndpointConfig serverEndpointConfig : container.getEndpointConfigs()) {
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
                } else if (!StringUtils.equals(requestNode.getNodeName(), node.getNodeName())) {
                    matched = false;
                    break;
                }
            }
            //匹配成功
            if (matched) {
                container.upgradeHttpToWebSocket(req, resp, serverEndpointConfig.getServerEndpointConfig(), matchData);
                return;
            }
        }
        //匹配失败
        chain.doFilter(request, response);
    }
}
