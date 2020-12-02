/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: Starter.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.maven.plugin.servlet;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.ServletHttpHandle;
import org.smartboot.servlet.war.WebContextRuntime;

import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/4
 */
public class Starter extends HttpHandle {
    private final ServletHttpHandle httpHandle;

    public Starter(String path, int port) throws Exception {
        System.out.println("hahaha:" + Starter.class.getClassLoader());
        WebContextRuntime webContextRuntime = new WebContextRuntime(path, "/");
        httpHandle = new ServletHttpHandle();
        httpHandle.addRuntime(webContextRuntime.getServletRuntime());
        httpHandle.start();
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap
                .setReadBufferSize(1024 * 1024)
                .pipeline().next(httpHandle);
        bootstrap.setPort(port).start();
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        httpHandle.doHandle(request, response);
    }
}
