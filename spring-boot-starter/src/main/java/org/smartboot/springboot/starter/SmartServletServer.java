/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.springboot.starter;

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
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.provider.WebsocketProvider;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import java.util.concurrent.CompletableFuture;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/12
 */
public class SmartServletServer implements WebServer {
    private final Object monitor = new Object();
    private final Container container;
    private final HttpBootstrap bootstrap;
    private volatile boolean started = false;
    private final int port;


    public SmartServletServer(ServletContextRuntime runtime, int port) throws Throwable {
        this.port = port;
        container = new Container();
        container.addRuntime(runtime);
        this.bootstrap = new HttpBootstrap();
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
        bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024).debug(false);
        bootstrap.setPort(port);
        container.start(this.bootstrap.configuration());
    }

    @Override
    public void start() throws WebServerException {
        synchronized (this.monitor) {
            if (this.started) {
                return;
            }
            try {
                bootstrap.start();
                System.out.println("启动成功");
                this.started = true;
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void stop() throws WebServerException {
        synchronized (this.monitor) {
            if (!this.started) {
                return;
            }
            this.started = false;
            container.stop();
            bootstrap.shutdown();
        }
    }

    @Override
    public int getPort() {
        return port;
    }
}
