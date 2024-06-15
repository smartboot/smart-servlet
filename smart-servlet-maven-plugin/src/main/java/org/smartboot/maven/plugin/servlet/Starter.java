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

import org.smartboot.http.common.codec.websocket.CloseReason;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.http.server.impl.WebSocketResponseImpl;
import org.smartboot.servlet.Container;
import org.smartboot.servlet.provider.WebsocketProvider;

import java.util.concurrent.CompletableFuture;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/4
 */
public class Starter {

    public Starter(String path, String contentPath, int port, ClassLoader classLoader) throws Throwable {
        System.out.println("path: " + path);
        System.out.println("contentPath: " + contentPath);
        Container container = new Container();
        container.addRuntime(path, contentPath, classLoader);
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024);
        bootstrap.httpHandler(new HttpServerHandler() {

            @Override
            public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) {
                container.doHandle(request, response, completableFuture);
            }
        }).webSocketHandler(new WebSocketHandler() {
            @Override
            public void whenHeaderComplete(WebSocketRequestImpl request, WebSocketResponseImpl response) {
                CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                try {
                    container.doHandle(request, response, completableFuture);
                } finally {
                    if (request.getAttachment() == null || request.getAttachment().get(WebsocketProvider.WEBSOCKET_SESSION_ATTACH_KEY) == null) {
                        response.close(CloseReason.UNEXPECTED_ERROR, "");
                    }
                }
            }

            @Override
            public void handle(WebSocketRequest request, WebSocketResponse response) {
                container.doHandle(request, response);
            }
        });
        container.start(bootstrap.configuration());
        bootstrap.setPort(port).start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            container.stop();
            bootstrap.shutdown();
        }));
    }
}
