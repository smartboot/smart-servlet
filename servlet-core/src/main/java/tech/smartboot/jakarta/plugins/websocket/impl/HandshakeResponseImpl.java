/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.jakarta.plugins.websocket.impl;

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
