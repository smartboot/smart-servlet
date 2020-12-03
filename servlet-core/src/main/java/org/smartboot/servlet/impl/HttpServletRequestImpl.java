/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: HttpServletRequestImpl.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.utils.NumberUtils;
import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.SmartHttpServletRequest;
import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.servlet.util.DateUtil;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HttpServletRequestImpl implements SmartHttpServletRequest {
    private final HttpRequest request;
    private final ServletContextImpl servletContext;
    private final DispatcherType dispatcherType;
    private final SessionProvider sessionProvider;
    private final ContainerRuntime runtime;
    private String characterEncoding;
    private Map<String, Object> attributes;
    private HttpSession httpSession;
    private Cookie[] cookies;
    private String servletPath;
    private String pathInfo;
    private String requestURI;

    public HttpServletRequestImpl(HttpRequest request, ContainerRuntime runtime, DispatcherType dispatcherType) {
        this.request = request;
        this.dispatcherType = dispatcherType;
        this.servletContext = runtime.getServletContext();
        this.sessionProvider = runtime.getSessionProvider();
        this.runtime = runtime;
        this.requestURI = request.getRequestURI();
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cookie[] getCookies() {

        if (cookies != null) {
            return cookies;
        }
        org.smartboot.http.server.Cookie[] cookie = request.getCookies();
        if (cookie == null) {
            cookies = new Cookie[0];
        } else {
            cookies = new Cookie[cookie.length];
            for (int i = 0; i < cookie.length; i++) {
                cookies[i] = new Cookie(cookie[i].getName(), cookie[i].getValue());
            }
        }
        return cookies;
    }

    @Override
    public long getDateHeader(String name) {
        String value = this.getHeader(name);
        if (value == null) {
            return -1L;
        } else {
            return DateUtil.parseDateHeader(name, value);
        }
    }

    @Override
    public String getHeader(String name) {
        return request.getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(request.getHeaders(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(request.getHeaderNames());
    }

    @Override
    public int getIntHeader(String name) {
        return NumberUtils.toInt(getHeader(name), -1);
    }

    @Override
    public String getMethod() {
        return request.getMethod();
    }

    @Override
    public String getPathInfo() {
        return pathInfo;
    }

    @Override
    public void setPathInfo(String pathInfo) {
        this.pathInfo = pathInfo;
    }

    @Override
    public String getPathTranslated() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getContextPath() {
        return servletContext.getContextPath();
    }

    @Override
    public String getQueryString() {
        return request.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        RunLogger.getLogger().log(Level.SEVERE, "unSupport getUserPrincipal");
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRequestURI() {
        return requestURI;
    }

    @Override
    public void setRequestUri(String requestURI) {
        this.requestURI = requestURI;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(request.getRequestURL());
    }

    @Override
    public String getServletPath() {
        return servletPath;
    }

    @Override
    public void setServletPath(String servletPath) {
        this.servletPath = servletPath;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (httpSession != null) {
            return httpSession;
        }
        httpSession = sessionProvider.getSession(this, create);
        return httpSession;
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String changeSessionId() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object getAttribute(String name) {
        return attributes == null ? null : attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes == null ? new ArrayList<String>(0) : attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        return characterEncoding;
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
        this.characterEncoding = characterEncoding;
    }

    @Override
    public int getContentLength() {
        return request.getContentLength();
    }

    @Override
    public long getContentLengthLong() {
        return request.getContentLength();
    }

    @Override
    public String getContentType() {
        return request.getContentType();
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getParameter(String name) {
        return request.getParameter(name);
    }

    @Override
    public Enumeration<String> getParameterNames() {
        return Collections.enumeration(request.getParameters().keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        return request.getParameterValues(name);
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        return request.getParameters();
    }

    @Override
    public String getProtocol() {
        return request.getProtocol();
    }

    @Override
    public String getScheme() {
        return request.getScheme();
    }

    @Override
    public String getServerName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getServerPort() {
        throw new UnsupportedOperationException();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getRemoteAddr() {
        return getAddress(request.getRemoteAddress());
    }

    @Override
    public String getRemoteHost() {
        return request.getRemoteAddress().getHostString();
    }

    @Override
    public void setAttribute(String name, Object o) {
        if (attributes == null) {
            attributes = new HashMap<>();
        }
        attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        if (attributes != null) {
            attributes.remove(name);
        }
    }

    @Override
    public Locale getLocale() {
        return request.getLocale();
    }

    @Override
    public Enumeration<Locale> getLocales() {
        return request.getLocales();
    }

    @Override
    public boolean isSecure() {
        throw new UnsupportedOperationException();
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return runtime.getDispatcherProvider().getRequestDispatcher(this, path);
    }

    @Override
    public String getRealPath(String path) {
        return servletContext.getRealPath(path);
    }

    @Override
    public int getRemotePort() {
        return request.getRemoteAddress().getPort();
    }

    @Override
    public String getLocalName() {
        return request.getLocalAddress().getHostString();
    }

    @Override
    public String getLocalAddr() {
        return getAddress(request.getLocalAddress());
    }

    private String getAddress(InetSocketAddress inetSocketAddress) {
        if (inetSocketAddress == null) {
            return "";
        }
        return inetSocketAddress.getAddress() == null ? inetSocketAddress.getHostString() : inetSocketAddress.getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return request.getLocalAddress().getPort();
    }

    @Override
    public ServletContextImpl getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isAsyncStarted() {
        return false;
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        throw new UnsupportedOperationException();
    }

    @Override
    public DispatcherType getDispatcherType() {
        return dispatcherType;
    }
}
