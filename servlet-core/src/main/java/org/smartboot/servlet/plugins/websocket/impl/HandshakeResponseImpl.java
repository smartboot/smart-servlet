package org.smartboot.servlet.plugins.websocket.impl;

import javax.servlet.http.HttpServletResponse;
import javax.websocket.HandshakeResponse;
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
