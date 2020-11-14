/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartServletServer.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.springboot.starter;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.ServletHttpHandle;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/12
 */
public class SmartServletServer implements WebServer {
    private final Object monitor = new Object();
    private final ServletHttpHandle httpHandle;
    private HttpBootstrap bootstrap;
    private volatile boolean started = false;


    public SmartServletServer(ContainerRuntime runtime) {
        httpHandle = new ServletHttpHandle();
        try {
            httpHandle.addRuntime(runtime);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void start() throws WebServerException {
        synchronized (this.monitor) {
            if (this.started) {
                return;
            }
            try {
                httpHandle.start();
                if (this.bootstrap == null) {
                    this.bootstrap = new HttpBootstrap();
                    bootstrap.pipeline().next(httpHandle);
                    bootstrap.setReadBufferSize(1024 * 1024).setPort(8080).start();
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

    }

    @Override
    public int getPort() {
        return 0;
    }
}
