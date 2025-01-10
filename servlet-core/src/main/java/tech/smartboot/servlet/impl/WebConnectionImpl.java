/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.WebConnection;
import tech.smartboot.feat.core.server.HttpRequest;

import java.io.IOException;

public class WebConnectionImpl implements WebConnection {
    private final HttpRequest request;
    private final ServletContext servletContext;

    public WebConnectionImpl(HttpRequest request, ServletContext servletContext) {
        this.request = request;
        this.servletContext = servletContext;
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return null;
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return null;
    }

    @Override
    public void close() throws Exception {

    }

    public HttpRequest getRequest() {
        return request;
    }

    public ServletContext getServletContext() {
        return servletContext;
    }
}
