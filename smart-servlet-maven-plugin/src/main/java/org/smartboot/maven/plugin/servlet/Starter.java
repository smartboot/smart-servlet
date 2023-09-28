/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
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

    public Starter(String path, String contentPath, int port, ClassLoader classLoader) throws Throwable {
        System.out.println("path: " + path);
        System.out.println("contentPath: " + contentPath);
        ContainerRuntime containerRuntime = new ContainerRuntime();
        containerRuntime.addRuntime(path, contentPath, classLoader);
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024);
        bootstrap.httpHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response) {
                containerRuntime.doHandle(request, response);
            }
        }).webSocketHandler(new WebSocketHandler() {
            @Override
            public void handle(WebSocketRequest request, WebSocketResponse response) throws Throwable {
                containerRuntime.doHandle(request, response);
            }
        });
        containerRuntime.start(bootstrap.configuration());
        bootstrap.setPort(port).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            containerRuntime.stop();
            bootstrap.shutdown();
        }));
    }
}
