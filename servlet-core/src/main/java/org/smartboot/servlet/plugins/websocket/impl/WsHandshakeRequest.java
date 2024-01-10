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

import org.smartboot.http.server.WebSocketRequest;

import javax.websocket.server.HandshakeRequest;
import java.net.URI;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public class WsHandshakeRequest implements HandshakeRequest {

    private WebSocketRequest request;

    public WsHandshakeRequest(WebSocketRequest request) {
        this.request = request;
    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return null;
    }

    @Override
    public Principal getUserPrincipal() {
        return null;
    }

    @Override
    public URI getRequestURI() {
        return URI.create(request.getRequestURI());
    }

    @Override
    public boolean isUserInRole(String s) {
        return false;
    }

    @Override
    public Object getHttpSession() {
        return null;
    }

    @Override
    public Map<String, List<String>> getParameterMap() {
        Map<String, String[]> parameters = request.getParameters();
        if (parameters == null || parameters.size() == 0) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> map = new HashMap<>();
        parameters.forEach((key, value) -> {
            map.put(key, Arrays.asList(value));
        });
        return Collections.unmodifiableMap(map);
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }
}
