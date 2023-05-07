/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: HttpServletRequestImpl.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.NumberUtils;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.SmartHttpServletRequest;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.fileupload.SmartHttpRequestContext;
import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.servlet.third.commons.fileupload.FileItem;
import org.smartboot.servlet.third.commons.fileupload.FileUpload;
import org.smartboot.servlet.third.commons.fileupload.FileUploadException;
import org.smartboot.servlet.third.commons.fileupload.disk.DiskFileItemFactory;
import org.smartboot.servlet.util.DateUtil;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
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
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HttpServletRequestImpl implements SmartHttpServletRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletRequestImpl.class);
    private static final Cookie[] NONE_COOKIE = new Cookie[0];
    private final HttpRequest request;
    private final ServletContextImpl servletContext;
    private final DispatcherType dispatcherType;
    private final ServletContextRuntime runtime;
    private String characterEncoding;
    private Map<String, Object> attributes;
    private HttpSession httpSession;
    private Cookie[] cookies;
    private String servletPath;
    private int servletPathStart;
    private int servletPathEnd;
    private String pathInfo;
    private int pathInfoStart;
    private int pathInfoEnd;
    private String requestUri;
    private HttpServletResponse httpServletResponse;
    private ServletInputStream servletInputStream;
    /**
     * 请求中携带的sessionId
     */
    private String requestedSessionId;

    /**
     * sessionId是否来源于Cookie
     */
    private boolean sessionIdFromCookie;

    /**
     * 匹配的Servlet
     */
    private ServletInfo servletInfo;

    public HttpServletRequestImpl(HttpRequest request, ServletContextRuntime runtime, DispatcherType dispatcherType) {
        this.request = request;
        this.dispatcherType = dispatcherType;
        this.servletContext = runtime.getServletContext();
        this.runtime = runtime;
        this.requestUri = request.getRequestURI();
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public String getAuthType() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Cookie[] getCookies() {
        if (cookies != null) {
            return cookies == NONE_COOKIE ? null : cookies;
        }
        org.smartboot.http.common.Cookie[] cookie = request.getCookies();
        if (cookie == null || cookie.length == 0) {
            cookies = NONE_COOKIE;
        } else {
            List<Cookie> list = new ArrayList<>(cookie.length);
            for (org.smartboot.http.common.Cookie value : cookie) {
                if ("Path".equals(value.getName())) {
                    LOGGER.warn("invalid cookie name: " + value.getName());
                    continue;
                }
                list.add(new Cookie(value.getName(), value.getValue()));
            }
            cookies = new Cookie[list.size()];
            list.toArray(cookies);
        }
        return getCookies();
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
        if (pathInfoStart < 0) {
            return null;
        }
        if (pathInfo != null) {
            return pathInfo;
        }
        pathInfo = getRequestURI().substring(pathInfoStart, pathInfoEnd);
        return pathInfo;
    }

    @Override
    public void setPathInfo(int start, int end) {
        this.pathInfoStart = start;
        this.pathInfoEnd = end;
    }

    @Override
    public void setServletInfo(ServletInfo servletInfo) {
        this.servletInfo = servletInfo;
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
        Principal principal = getUserPrincipal();
        return principal == null ? null : principal.getName();
    }

    @Override
    public boolean isUserInRole(String role) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Principal getUserPrincipal() {
        LOGGER.info("unSupport getUserPrincipal");
        return null;
    }

    @Override
    public String getRequestedSessionId() {
        if (requestedSessionId != null) {
            return StringUtils.EMPTY.equals(requestedSessionId) ? null : requestedSessionId;
        }
        Cookie[] cookies = getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SessionProvider.DEFAULT_SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    requestedSessionId = cookie.getValue();
                    sessionIdFromCookie = true;
                    break;
                }
            }
        }
        if (StringUtils.isBlank(requestedSessionId)) {
            requestedSessionId = request.getParameter(SessionProvider.DEFAULT_SESSION_PARAMETER_NAME);
            sessionIdFromCookie = false;
        }
        if (StringUtils.isBlank(requestedSessionId)) {
            requestedSessionId = StringUtils.EMPTY;
        }
        return getRequestedSessionId();
    }

    @Override
    public String getRequestURI() {
        return requestUri;
    }

    @Override
    public void setRequestUri(String requestUri) {
        this.requestUri = requestUri;
    }

    @Override
    public StringBuffer getRequestURL() {
        return new StringBuffer(request.getRequestURL());
    }

    @Override
    public String getServletPath() {
        if (servletPathStart < 0) {
            return null;
        }
        if (servletPath != null) {
            return servletPath;
        }
        servletPath = getRequestURI().substring(servletPathStart, servletPathEnd);
        return servletPath;
    }

    @Override
    public void setServletPath(int start, int end) {
        this.servletPathStart = start;
        this.servletPathEnd = end;
    }

    @Override
    public HttpSession getSession(boolean create) {
        if (httpSession != null) {
            return httpSession;
        }
        httpSession = runtime.getSessionProvider().getSession(this, httpServletResponse, create);
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
        return getSession(false) != null;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return getRequestedSessionId() != null && sessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return getRequestedSessionId() != null && !sessionIdFromCookie;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return isRequestedSessionIdFromURL();
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
        parseParts();
        if (partsParseException != null) {
            if (partsParseException instanceof IOException) {
                throw (IOException) partsParseException;
            } else if (partsParseException instanceof ServletException) {
                throw (ServletException) partsParseException;
            }
        }
        return parts;
    }

    private Collection<Part> parts = null;
    private Exception partsParseException = null;

    private void parseParts() {
        if (parts != null || partsParseException != null) {
            return;
        }
        try {
            MultipartConfigElement multipartConfigElement = servletInfo.getMultipartConfig();
            if (multipartConfigElement == null) {
                multipartConfigElement = new MultipartConfigElement("");
            }
            //获取文件存放目录
            File location = getLocation(multipartConfigElement);
            if (!location.isDirectory()) {
                throw new IOException("there's no upload-file directory!");
            }

            DiskFileItemFactory factory = new DiskFileItemFactory();
            factory.setRepository(location.getCanonicalFile());
            factory.setSizeThreshold(multipartConfigElement.getFileSizeThreshold());

            FileUpload upload = new FileUpload();
            upload.setFileItemFactory(factory);
            upload.setFileSizeMax(multipartConfigElement.getMaxFileSize());
            upload.setSizeMax(multipartConfigElement.getMaxRequestSize());
            parts = new ArrayList<>();

            List<FileItem> items = upload.parseRequest(new SmartHttpRequestContext(request));
            for (FileItem item : items) {
                PartImpl part = new PartImpl(item, location);
                parts.add(part);
                if (part.getSubmittedFileName() == null) {
                    String name = part.getName();
                    String value = null;
                    try {
                        value = item.getString(getCharacterEncoding());
                    } catch (UnsupportedEncodingException uee) {
                        // Not possible
                    }
                    request.getParameters().put(name, new String[]{value});
                }
            }
        } catch (FileUploadException | IOException e) {
            partsParseException = e;
        }
    }

    private File getLocation(MultipartConfigElement multipartConfigElement) {
        File location;
        String locationStr = multipartConfigElement.getLocation();
        //未指定location，采用临时目录
        if (StringUtils.isBlank(locationStr)) {
            location = ((File) servletContext.getAttribute(
                    ServletContext.TEMPDIR));
        } else {
            location = new File(locationStr);
            //非绝对路径，则存放于临时目录下
            if (!location.isAbsolute()) {
                location = new File(
                        (File) servletContext.getAttribute(ServletContext.TEMPDIR),
                        locationStr).getAbsoluteFile();
            }
        }
        if (!location.exists()) {
            LOGGER.warn("create upload-file directory：{}", location.getAbsolutePath());
            if (!location.mkdirs()) {
                LOGGER.warn("fail to create upload-file directory,{}", location.getAbsolutePath());
            }
        }
        return location;
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        for (Part part : getParts()) {
            if (name.equals(part.getName())) {
                return part;
            }
        }
        return null;
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
        if (servletInputStream == null) {
            servletInputStream = new ServletInputStreamImpl(request.getInputStream());
        }
        return servletInputStream;
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
        String host = getHeader(HeaderNameEnum.HOST.getName());
        if (StringUtils.isBlank(host)) {
            return request.getLocalAddress().getHostName();
        }
        int index = host.indexOf(":");
        if (index < 0) {
            return host;
        } else {
            return host.substring(0, index);
        }
    }

    @Override
    public int getServerPort() {
        String host = getHeader(HeaderNameEnum.HOST.getName());
        if (StringUtils.isBlank(host)) {
            throw new UnsupportedOperationException();
        }
        int index = host.indexOf(":");
        if (index < 0) {
            return request.getRemoteAddress().getPort();
        } else {
            return NumberUtils.toInt(host.substring(index + 1), -1);
        }
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
        return request.isSecure();
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
