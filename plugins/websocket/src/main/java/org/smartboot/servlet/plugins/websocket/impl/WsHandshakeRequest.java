/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WsHandshakeRequest.java
 * Date: 2021-04-24
 * Author: sandao (zhengjunweimail@163.com)
 *
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
