/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: Starter.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.maven.plugin.servlet;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.ContainerRuntime;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/4
 */
public class Starter {

    public Starter(String path, String contentPath, int port, ClassLoader classLoader) throws Exception {
        ContainerRuntime containerRuntime = new ContainerRuntime();
        containerRuntime.addRuntime(path, contentPath, classLoader);
        containerRuntime.start();
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024);
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) {
                containerRuntime.doHandle(request, response);
            }
        }).webSocketHandler(new WebSocketHandler() {
            @Override
            public void handle(WebSocketRequest request, WebSocketResponse response) throws IOException {
                containerRuntime.doHandle(request, response);
            }
        });
        bootstrap.setPort(port).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            containerRuntime.stop();
            bootstrap.shutdown();
        }));
    }
}
