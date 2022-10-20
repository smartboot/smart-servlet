/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartServletServer.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.springboot.starter;

import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import org.smartboot.http.server.WebSocketHandler;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.Request;
import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.ServletContextRuntime;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/12
 */
public class SmartServletServer implements WebServer {
    private final Object monitor = new Object();
    private final ContainerRuntime containerRuntime;
    private HttpBootstrap bootstrap;
    private volatile boolean started = false;
    private int port;


    public SmartServletServer(ServletContextRuntime runtime, int port) {
        this.port = port;
        containerRuntime = new ContainerRuntime();
        containerRuntime.addRuntime(runtime);
        containerRuntime.start();
    }

    @Override
    public void start() throws WebServerException {
        synchronized (this.monitor) {
            if (this.started) {
                return;
            }
            try {
                if (this.bootstrap == null) {
                    this.bootstrap = new HttpBootstrap();
                    bootstrap.httpHandler(new HttpServerHandler() {
                        @Override
                        public void handle(HttpRequest request, HttpResponse response) throws IOException {
                            containerRuntime.doHandle(request, response);
                        }
                    }).webSocketHandler(new WebSocketHandler() {
                        @Override
                        public void onHeaderComplete(Request request) throws IOException {
                            super.onHeaderComplete(request);
                            containerRuntime.onHeaderComplete(request);
                        }

                        @Override
                        public void handle(WebSocketRequest request, WebSocketResponse response) throws IOException {
                            containerRuntime.doHandle(request, response);
                        }
                    });
                    bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024).debug(true);
                    bootstrap.setPort(port).start();
                    System.out.println("启动成功");
                }
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
            containerRuntime.stop();
            bootstrap.shutdown();
        }
    }

    @Override
    public int getPort() {
        return port;
    }
}
