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
