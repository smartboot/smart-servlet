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
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.servlet.ApplicationRuntime;
import org.smartboot.servlet.ContainerRuntime;
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


    public SmartServletServer(ApplicationRuntime runtime) {
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
                    bootstrap.pipeline(new HttpServerHandle() {
                        @Override
                        public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
                            containerRuntime.doHandle(request, response);
                        }
                    });
                    bootstrap.configuration().bannerEnabled(false).readBufferSize(1024 * 1024);
                    bootstrap.setPort(8080).start();
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
        return 8080;
    }
}
