/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServerEndpointConfigImpl.java
 * Date: 2021-04-24
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.websocket.impl;

import org.smartboot.servlet.conf.UriMappingInfo;
import org.smartboot.servlet.util.PathMatcherUtil;

import javax.websocket.server.ServerEndpointConfig;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/28
 */
public class SmartServerEndpointConfig {
    private final ServerEndpointConfig serverEndpointConfig;
    private final UriMappingInfo path;
    private final AnnotatedEndpoint endpoint;

    public SmartServerEndpointConfig(ServerEndpointConfig serverEndpointConfig) {
        this.serverEndpointConfig = serverEndpointConfig;
        path = PathMatcherUtil.addMapping(serverEndpointConfig.getPath());
        endpoint = new AnnotatedEndpoint(serverEndpointConfig);
    }

    public ServerEndpointConfig getServerEndpointConfig() {
        return serverEndpointConfig;
    }

    public UriMappingInfo getPath() {
        return path;
    }

    public AnnotatedEndpoint getEndpoint() {
        return endpoint;
    }
}
