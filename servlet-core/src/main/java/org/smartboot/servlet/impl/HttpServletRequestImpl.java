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
import org.smartboot.servlet.util.CollectionUtils;
import org.smartboot.servlet.util.DateUtil;
import org.smartboot.socket.util.Attachment;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletRequestAttributeEvent;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HttpServletRequestImpl implements SmartHttpServletRequest {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpServletRequestImpl.class);
    private static final String URL_JSESSION_ID = ";" + SessionProvider.DEFAULT_SESSION_PARAMETER_NAME + "=";
    private static final Cookie[] NONE_COOKIE = new Cookie[0];
    private final HttpRequest request;
    private ServletContextImpl servletContext;
    private final DispatcherType dispatcherType;
    private final ServletContextRuntime runtime;
    private Charset characterEncoding;
    private Map<String, Object> attributes;
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
    private BufferedReader reader;
    /**
     * 请求中携带的sessionId
     */
    private String requestedSessionId;


    private String actualSessionId;

    /**
     * sessionId是否来源于Cookie
     */
    private boolean sessionIdFromCookie;

    /**
     * 匹配的Servlet
     */
    private ServletInfo servletInfo;

    private boolean asyncStarted = false;

    private volatile AsyncContext asyncContext = null;
    private final CompletableFuture<Object> completableFuture;

    public HttpServletRequestImpl(HttpRequest request, ServletContextRuntime runtime, DispatcherType dispatcherType, CompletableFuture<Object> completableFuture) {
        this.request = request;
        this.dispatcherType = dispatcherType;
        this.servletContext = runtime.getServletContext();
        this.runtime = runtime;
        this.completableFuture = completableFuture;
        int index = request.getRequestURI().indexOf(URL_JSESSION_ID);
        if (index == -1) {
            this.requestUri = request.getRequestURI();
        } else {
            this.requestUri = request.getRequestURI().substring(0, index);
            this.requestedSessionId = request.getRequestURI().substring(index + URL_JSESSION_ID.length());
        }
    }

    public void setHttpServletResponse(HttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }

    @Override
    public String getAuthType() {
        servletContext.log("unSupport getAuthType");
        return null;
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
        String strVal = getHeader(name);
        //If the request does not have a header of the specified name, this method returns -1.
        if (strVal == null) {
            return -1;
        }
        return Integer.parseInt(strVal);
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
    public Attachment getAttachment() {
        return request.getAttachment();
    }

    @Override
    public void setAttachment(Attachment attachment) {
        request.setAttachment(attachment);
    }

    @Override
    public String getPathTranslated() {
        return getRealPath(getPathInfo());
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
            requestedSessionId = StringUtils.EMPTY;
        }
        return getRequestedSessionId();
    }

    public void setActualSessionId(String sessionId) {
        this.actualSessionId = sessionId;
    }

    public String getActualSessionId() {
        return actualSessionId;
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
        return runtime.getSessionProvider().getSession(this, httpServletResponse, create);
    }

    @Override
    public HttpSession getSession() {
        return getSession(true);
    }

    @Override
    public String changeSessionId() {
        HttpSession session = getSession(false);
        if (session == null) {
            throw new IllegalStateException();
        }
        runtime.getSessionProvider().changeSessionId(session);
        setActualSessionId(session.getId());
        return session.getId();
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return runtime.getSessionProvider().isRequestedSessionIdValid(this);
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
    public boolean authenticate(HttpServletResponse response) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void login(String username, String password) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void logout() {
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
            location = ((File) servletContext.getAttribute(ServletContext.TEMPDIR));
        } else {
            location = new File(locationStr);
            //非绝对路径，则存放于临时目录下
            if (!location.isAbsolute()) {
                location = new File((File) servletContext.getAttribute(ServletContext.TEMPDIR), locationStr).getAbsoluteFile();
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
//        T t = null;
//        try {
//            t = handlerClass.newInstance();
//            t.init();
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//        return t;
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
        if (characterEncoding != null) {
            return characterEncoding.name();
        }
        String value = getHeader(HeaderNameEnum.CONTENT_TYPE.getName());
        String charset = StringUtils.substringAfter(value, "charset=");
        if (StringUtils.isNotBlank(charset)) {
            return charset;
        }
        return runtime.getServletContext().getRequestCharacterEncoding();
    }

    @Override
    public void setCharacterEncoding(String characterEncoding) throws UnsupportedEncodingException {
        if (servletInputStream != null) {
            return;
        }
        try {
            this.characterEncoding = Charset.forName(characterEncoding);
        } catch (UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException();
        }

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
        if (reader != null) {
            throw new IllegalStateException("getReader method has already been called for this request");
        }
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
        if (reader == null) {
            if (servletInputStream != null) {
                throw new IllegalStateException("getInputStream method has been called on this request");
            }
            String character = getCharacterEncoding();
            if (StringUtils.isBlank(character)) {
                reader = new BufferedReader(new InputStreamReader(getInputStream()));
            } else {
                reader = new BufferedReader(new InputStreamReader(getInputStream(), character));
            }
        }
        return reader;
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

        Object replace = attributes.put(name, o);
        if (CollectionUtils.isNotEmpty(runtime.getDeploymentInfo().getRequestAttributeListeners())) {
            ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(servletContext, this, name, o);
            if (replace == null) {
                runtime.getDeploymentInfo().getRequestAttributeListeners().forEach(request -> request.attributeAdded(event));
            } else {
                runtime.getDeploymentInfo().getRequestAttributeListeners().forEach(request -> request.attributeReplaced(event));
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        if (attributes == null) {
            return;
        }
        Object o = attributes.remove(name);
        if (CollectionUtils.isNotEmpty(runtime.getDeploymentInfo().getRequestAttributeListeners())) {
            ServletRequestAttributeEvent event = new ServletRequestAttributeEvent(servletContext, this, name, o);
            runtime.getDeploymentInfo().getRequestAttributeListeners().forEach(request -> request.attributeRemoved(event));
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

    public void setServletContext(ServletContextImpl servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public ServletContextImpl getServletContext() {
        return servletContext;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        return startAsync(this, httpServletResponse);
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        if (!isAsyncSupported()) {
            throw new IllegalStateException();
        } else if (asyncStarted) {
            throw new IllegalStateException();
        }
        asyncStarted = true;
        asyncContext = runtime.getAsyncContextProvider().startAsync(this, servletRequest, servletResponse, asyncContext);
        return asyncContext;
    }

    @Override
    public boolean isAsyncStarted() {
        return asyncStarted;
    }

    public void resetAsyncStarted() {
        asyncStarted = false;
    }


    @Override
    public boolean isAsyncSupported() {
        return servletInfo.isAsyncSupported();
    }

    @Override
    public AsyncContext getAsyncContext() {
        if (isAsyncStarted()) {
            return asyncContext;
        }
        throw new IllegalStateException();
    }

    public AsyncContext getInternalAsyncContext() {
        return asyncContext;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return dispatcherType;
    }

    public CompletableFuture<Object> getCompletableFuture() {
        return completableFuture;
    }
}
