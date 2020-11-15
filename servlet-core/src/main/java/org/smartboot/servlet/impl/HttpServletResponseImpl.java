/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: HttpServletResponseImpl.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.HttpResponse;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.servlet.util.DateUtil;
import org.smartboot.servlet.util.ServletPathMatcher;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HttpServletResponseImpl implements HttpServletResponse {
    private final HttpResponse response;
    private final HttpServletRequest request;
    private List<Cookie> cookies;
    private String contentType;
    private PrintWriter writer;
    private ServletOutputStreamImpl servletOutputStream;

    public HttpServletResponseImpl(HttpServletRequest request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    @Override
    public void addCookie(Cookie cookie) {
        if (cookies == null) {
            cookies = new ArrayList<>();
        }
        cookies.add(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return response.getHeader(name) != null;
    }

    @Override
    public String encodeURL(String url) {
        RunLogger.getLogger().log(Level.SEVERE, "url");
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        RunLogger.getLogger().log(Level.SEVERE, "url");
        return url;
    }


    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeRedirectURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        HttpStatus httpStatus = HttpStatus.valueOf(sc);
        response.setHttpStatus(httpStatus);
    }

    @Override
    public void sendError(int sc) throws IOException {
        HttpStatus status = HttpStatus.valueOf(sc);
        sendError(sc, status != null ? status.getReasonPhrase() : "Unknow");
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        response.setHttpStatus(HttpStatus.FOUND);
        RunLogger.getLogger().log(Level.INFO, "location:" + location);
        String redirect;
        if (ServletPathMatcher.isAbsoluteUrl(location)) {
            redirect = location;
        } else if (location.charAt(0) == '/') {
            redirect = request.getScheme() + "://" + request.getHeader(HttpHeaderConstant.Names.HOST) + location;
        } else {
            String url = request.getRequestURL().toString();
            int last = url.lastIndexOf("/");
            if (last != 1) {
                redirect = url.substring(0, last + 1) + location;
            } else {
                redirect = url + location;
            }
        }
        RunLogger.getLogger().log(Level.INFO, "sendRedirect:" + redirect);
        response.setHeader(HttpHeaderConstant.Names.LOCATION, redirect);
    }

    @Override
    public void setDateHeader(String name, long date) {
        setHeader(name, DateUtil.formatDate(date));
    }

    @Override
    public void addDateHeader(String name, long date) {
        response.addHeader(name, DateUtil.formatDate(date));
    }

    @Override
    public void setHeader(String name, String value) {
        response.setHeader(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        response.addHeader(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        response.setHeader(name, String.valueOf(value));
    }

    @Override
    public void addIntHeader(String name, int value) {
        response.addHeader(name, String.valueOf(value));
    }

    @Override
    public void setStatus(int sc, String sm) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getStatus() {
        if (response.getHttpStatus() == null) {
            return HttpStatus.OK.value();
        }
        return response.getHttpStatus().value();
    }

    @Override
    public void setStatus(int sc) {
        response.setHttpStatus(HttpStatus.valueOf(sc));
    }

    @Override
    public String getHeader(String name) {
        return response.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return response.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return response.getHeaderNames();
    }

    @Override
    public String getCharacterEncoding() {
        return response.getCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String charset) {
        response.setCharacterEncoding(charset);
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public void setContentType(String type) {
        contentType = type;
        response.setContentType(type);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        if (servletOutputStream == null) {
            servletOutputStream = new ServletOutputStreamImpl(response.getOutputStream());
        }
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer == null) {
            writer = new PrintWriter(new ServletPrintWriter(getOutputStream(), getCharacterEncoding()));
        }
        return writer;
    }

    @Override
    public void setContentLength(int len) {
        setContentLengthLong(len);
    }

    @Override
    public void setContentLengthLong(long len) {
        response.setContentLength((int) len);
    }

    @Override
    public int getBufferSize() {
        return servletOutputStream == null ? 0 : servletOutputStream.getBufferSize();
    }

    @Override
    public void setBufferSize(int size) {
        if (servletOutputStream != null && (servletOutputStream.getCount() > 0 || servletOutputStream.isCommitted())) {
            throw new IllegalStateException();
        }
        if (servletOutputStream != null) {
            servletOutputStream.updateBufferSize(size);
        }
    }

    @Override
    public void flushBuffer() throws IOException {
        getOutputStream().flush();
    }

    @Override
    public void resetBuffer() {
        if (servletOutputStream == null) {
            return;
        }
        if (servletOutputStream.isCommitted()) {
            throw new IllegalStateException();
        }
        servletOutputStream.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return servletOutputStream != null && servletOutputStream.isCommitted();
    }

    @Override
    public void reset() {
        if (isCommitted()) {
            throw new IllegalStateException();
        }
        response.getHeaderNames().forEach(headerName -> response.setHeader(headerName, null));
        setContentLength(-1);
        setContentType(null);
        setCharacterEncoding(null);
        response.setHttpStatus(null);
        writer = null;
        if (servletOutputStream != null) {
            servletOutputStream.resetBuffer();
        }
    }

    @Override
    public Locale getLocale() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setLocale(Locale loc) {
        RunLogger.getLogger().log(Level.SEVERE, "unSupport setLocal now");
    }
}
