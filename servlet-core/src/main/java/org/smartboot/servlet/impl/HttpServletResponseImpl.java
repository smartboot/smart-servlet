/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.servlet.util.DateUtil;
import org.smartboot.servlet.util.PathMatcherUtil;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Locale;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HttpServletResponseImpl implements HttpServletResponse {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletResponseImpl.class);
    private final HttpResponse response;
    private final HttpServletRequestImpl request;
    private String contentType;
    private String charset;
    private PrintWriter writer;
    private ServletOutputStreamImpl servletOutputStream;
    private int bufferSize = -1;

    private Locale locale;

    public HttpServletResponseImpl(HttpServletRequestImpl request, HttpResponse response) {
        this.request = request;
        this.response = response;
    }

    private org.smartboot.http.common.Cookie sessionCookie;

    @Override
    public void addCookie(Cookie cookie) {
        if (sessionCookie != null && cookie.getName().equals(request.getServletContext().getSessionCookieConfig().getName())) {
            sessionCookie.setValue(cookie.getValue());
            sessionCookie.setPath(cookie.getPath());
            sessionCookie.setDomain(cookie.getDomain());
            sessionCookie.setMaxAge(cookie.getMaxAge());
            sessionCookie.setSecure(cookie.getSecure());
            sessionCookie.setVersion(cookie.getVersion());
            return;
        }
        org.smartboot.http.common.Cookie httpCookie = new org.smartboot.http.common.Cookie(cookie.getName(), cookie.getValue());
        httpCookie.setComment(cookie.getComment());
        httpCookie.setDomain(cookie.getDomain());
        httpCookie.setHttpOnly(cookie.isHttpOnly());
        httpCookie.setPath(cookie.getPath());
        httpCookie.setMaxAge(cookie.getMaxAge());
        httpCookie.setSecure(cookie.getSecure());
        httpCookie.setVersion(cookie.getVersion());
        response.addCookie(httpCookie);
        if (cookie.getName().equals(request.getServletContext().getSessionCookieConfig().getName())) {
            sessionCookie = httpCookie;
        }
    }

    @Override
    public boolean containsHeader(String name) {
        return response.getHeader(name) != null;
    }

    @Override
    public String encodeURL(String url) {
        LOGGER.info("url: " + url);
        return url;
    }

    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }


    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        //If the response has already been committed, this method throws an IllegalStateException.
        if (isCommitted()) {
            throw new IllegalStateException();
        }
        //After using this method, the response should be considered to be committed and should not be written to.
        if (servletOutputStream != null) {
            servletOutputStream.resetBuffer();
        }
        response.setHttpStatus(sc, msg);

        String location = request.getServletContext().getRuntime().getDeploymentInfo().getErrorPageLocation(HttpStatus.NOT_FOUND.value());
        if (StringUtils.isNotBlank(location)) {
            request.getServletContext().getRuntime().getDispatcherProvider().error(request.getServletContext(), location, request, this);
        }
    }

    @Override
    public void sendError(int sc) throws IOException {
        HttpStatus status = HttpStatus.valueOf(sc);
        sendError(sc, status != null ? status.getReasonPhrase() : "Unknow");
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        //If the response has already been committed, this method throws an IllegalStateException
        if (isCommitted()) {
            throw new IllegalStateException();
        }
        response.setHttpStatus(HttpStatus.FOUND);
        LOGGER.info("location:" + location);
        String redirect;
        if (PathMatcherUtil.isAbsoluteUrl(location)) {
            redirect = location;
        } else if (location.charAt(0) == '/') {
            redirect = request.getScheme() + "://" + request.getHeader(HeaderNameEnum.HOST.getName()) + location;
        } else {
            String url = request.getRequestURL().toString();
            int last = url.lastIndexOf("/");
            if (last != 1) {
                redirect = url.substring(0, last + 1) + location;
            } else {
                redirect = url + location;
            }
        }
        LOGGER.info("sendRedirect:" + redirect);
        response.setHeader(HeaderNameEnum.LOCATION.getName(), redirect);
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
        return response.getHttpStatus();
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
        if (charset != null) {
            return charset;
        }
        String charset = request.getServletContext().getResponseCharacterEncoding();
        if (charset == null) {
            charset = StandardCharsets.ISO_8859_1.name();
        }
        setCharacterEncoding(charset);
        return charset;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        if (isCommitted() || writer != null) {
            return;
        }
        this.charset = charset;
        if (contentType != null) {
            response.setContentType(getContentType());
        }
    }

    @Override
    public String getContentType() {
        if (contentType == null) {
            return null;
        }
        return contentType + ";charset=" + getCharacterEncoding();

    }

    @Override
    public void setContentType(String type) {
        //. It does not set the response's character
        // encoding if it is called after getWriter has been called or after the response has been committed.
        if (isCommitted()) {
            return;
        }
        int split = type.indexOf(";");
        if (split == -1) {
            contentType = type;
        } else {
            contentType = type.substring(0, split);
            split = type.indexOf("charset=", split);
            if (split != -1) {
                setCharacterEncoding(type.substring(split + 8));
            }
        }
        response.setContentType(getContentType());
    }

    @Override
    public ServletOutputStreamImpl getOutputStream() {
        if (writer != null) {
            reset();
            throw new IllegalStateException("getWriter has already been called.");
        }
        if (servletOutputStream == null) {
            byte[] buffer = null;
            if (bufferSize == -1) {
                buffer = new byte[512];
            } else if (bufferSize > 0) {
                buffer = new byte[bufferSize];
            }
            servletOutputStream = new ServletOutputStreamImpl(response.getOutputStream(), buffer);
        }
        return servletOutputStream;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        if (writer != null) {
            return writer;
        }
        //if the getOutputStream method has already been called for this response object
        if (servletOutputStream != null) {
            throw new IllegalStateException("getOutputStream has already been called.");
        }
        writer = new PrintWriter(new ServletPrintWriter(getOutputStream(), getCharacterEncoding()));
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
        return bufferSize;
    }

    @Override
    public void setBufferSize(int size) {
        if (servletOutputStream != null && (servletOutputStream.getCount() > 0 || servletOutputStream.isCommitted())) {
            throw new IllegalStateException();
        }
        bufferSize = size;
    }

    @Override
    public void flushBuffer() throws IOException {
        if (writer != null) {
            writer.flush();
        } else {
            getOutputStream().flush();
        }
    }

    public void flushServletBuffer() throws IOException {
        if (servletOutputStream != null) {
            servletOutputStream.flushServletBuffer();
        }
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
        contentType = null;
        setCharacterEncoding(null);
        response.setHttpStatus(HttpStatus.OK);
        writer = null;
        if (servletOutputStream != null) {
            servletOutputStream.resetBuffer();
        }
    }

    @Override
    public Locale getLocale() {
        return locale == null ? Locale.getDefault() : locale;
    }

    @Override
    public void setLocale(Locale loc) {
        if (isCommitted()) {
            return;
        }
        this.locale = loc;
        String encoding = request.getServletContext().getDeploymentInfo().getLocaleEncodingMappings().get(loc.getLanguage());
        if (StringUtils.isNotBlank(encoding)) {
            setCharacterEncoding(encoding);
        }
        setHeader(HeaderNameEnum.CONTENT_LANGUAGE.getName(), loc.getLanguage() + "-" + loc.getCountry());
    }
}
