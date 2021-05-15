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
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.WebSocketHandle;
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
        bootstrap.pipeline(new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) {
                containerRuntime.doHandle(request, response);
            }
        }).wsPipeline().next(new WebSocketHandle() {
            @Override
            public void doHandle(WebSocketRequest request, WebSocketResponse response) throws IOException {
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
