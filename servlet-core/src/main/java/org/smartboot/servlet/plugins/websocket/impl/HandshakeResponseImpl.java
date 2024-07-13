package org.smartboot.servlet.plugins.websocket.impl;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.HandshakeResponse;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class HandshakeResponseImpl implements HandshakeResponse {
    public HandshakeResponseImpl(HttpServletResponse response) {

    }

    @Override
    public Map<String, List<String>> getHeaders() {
        return Collections.emptyMap();
    }
}
