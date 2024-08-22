///*
// *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
// *
// *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
// *
// *   Enterprise users are required to use this project reasonably
// *   and legally in accordance with the AGPL-3.0 open source agreement
// *  without special permission from the smartboot organization.
// */
//
//package tech.smartboot.servlet.impl;
//
//import jakarta.servlet.http.Cookie;
//import jakarta.servlet.http.HttpSession;
//import jakarta.servlet.http.PushBuilder;
//import org.smartboot.http.common.HeaderValue;
//import org.smartboot.http.common.enums.HeaderNameEnum;
//import org.smartboot.http.common.enums.HttpMethodEnum;
//
//import java.util.Collections;
//import java.util.Enumeration;
//import java.util.HashMap;
//import java.util.HashSet;
//import java.util.Iterator;
//import java.util.Map;
//import java.util.Set;
//
///**
// * @author Stuart Douglas
// * @author <a href="mailto:ropalka@redhat.com">Richard Opalka</a>
// */
//public class PushBuilderImpl implements PushBuilder {
//
//    private static final Set<String> IGNORE;
//    private static final Set<String> CONDITIONAL;
//    private static final Set<String> INVALID_METHOD;
//
//    static {
//        final Set<String> ignore = new HashSet<>();
//        ignore.add(HeaderNameEnum.IF_MATCH.getName());
//        ignore.add(HeaderNameEnum.IF_NONE_MATCH.getName());
//        ignore.add(HeaderNameEnum.IF_MODIFIED_SINCE.getName());
//        ignore.add(HeaderNameEnum.IF_UNMODIFIED_SINCE.getName());
//        ignore.add(HeaderNameEnum.IF_RANGE.getName());
//        ignore.add(HeaderNameEnum.RANGE.getName());
////        ignore.add(HeaderNameEnum.ACCEPT_RANGES);
//        ignore.add(HeaderNameEnum.EXPECT.getName());
//        ignore.add(HeaderNameEnum.REFERER.getName());
//        IGNORE = Collections.unmodifiableSet(ignore);
//
//        final Set<String> conditional = new HashSet<>();
//        conditional.add(HeaderNameEnum.IF_MATCH.getName());
//        conditional.add(HeaderNameEnum.IF_NONE_MATCH.getName());
//        conditional.add(HeaderNameEnum.IF_MODIFIED_SINCE.getName());
//        conditional.add(HeaderNameEnum.IF_UNMODIFIED_SINCE.getName());
//        conditional.add(HeaderNameEnum.IF_RANGE.getName());
//        CONDITIONAL = Collections.unmodifiableSet(conditional);
//        final Set<String> invalid = new HashSet<>();
//        invalid.add(HttpMethodEnum.OPTIONS.getMethod());
//        invalid.add(HttpMethodEnum.PUT.getMethod());
//        invalid.add(HttpMethodEnum.POST.getMethod());
//        invalid.add(HttpMethodEnum.DELETE.getMethod());
//        invalid.add(HttpMethodEnum.CONNECT.getMethod());
//        invalid.add(HttpMethodEnum.TRACE.getMethod());
//        invalid.add("");
//        INVALID_METHOD = Collections.unmodifiableSet(invalid);
//    }
//
//    private HttpServletRequestImpl servletRequest;
//    private String method;
//    private String queryString;
//    private String sessionId;
//    private Map<String, HeaderValue> headers = Collections.emptyMap();
//    private int headerSize = 0;
//    private String path;
//
//    public PushBuilderImpl(HttpServletRequestImpl servletRequest) {
//        //TODO: auth
//        this.servletRequest = servletRequest;
//        this.method = "GET";
//        this.queryString = servletRequest.getQueryString();
//        HttpSession session = servletRequest.getSession(false);
//        if (session != null) {
//            this.sessionId = session.getId();
//        } else {
//            this.sessionId = servletRequest.getRequestedSessionId();
//        }
//
//        Enumeration<String> headerNames = servletRequest.getHeaderNames();
//        while (headerNames.hasMoreElements()) {
//            String name = headerNames.nextElement();
//            if (!IGNORE.contains(name)) {
//                Enumeration<String> headValues = servletRequest.getHeaders(name);
//                while (headValues.hasMoreElements()) {
//                    addHeader(name, headValues.nextElement());
//                }
//            }
//        }
//
//        if (servletRequest.getQueryString() == null) {
//            addHeader(HeaderNameEnum.REFERER.getName(), servletRequest.getRequestURL().toString());
//        } else {
//            addHeader(HeaderNameEnum.REFERER.getName(), servletRequest.getRequestURL() + "?" + servletRequest.getQueryString());
//        }
//        this.path = null;
//        for (Cookie cookie : servletRequest. ().responseCookies()){
//            if (cookie.getMaxAge() != null && cookie.getMaxAge() <= 0) {
//                //remove cookie
//                HeaderValues existing = headers.get(Headers.COOKIE);
//                if (existing != null) {
//                    Iterator<String> it = existing.iterator();
//                    while (it.hasNext()) {
//                        String val = it.next();
//                        if (val.startsWith(cookie.getName() + "=")) {
//                            it.remove();
//                        }
//                    }
//                }
//            } else if (!cookie.getName().equals(servletRequest.getServletContext().getSessionCookieConfig().getName())) {
//                headers.add(Headers.COOKIE, cookie.getName() + "=" + cookie.getValue());
//            }
//        }
//
//    }
//
//
//    @Override
//    public PushBuilder method(String method) {
//        if (method == null) {
//            throw new NullPointerException();
//        }
//        if (INVALID_METHOD.contains(method)) {
//            throw new IllegalArgumentException();
//        }
//        this.method = method;
//        return this;
//    }
//
//    @Override
//    public PushBuilder queryString(String queryString) {
//        this.queryString = queryString;
//        return this;
//    }
//
//    @Override
//    public PushBuilder sessionId(String sessionId) {
//        this.sessionId = sessionId;
//        return this;
//    }
//
//    @Override
//    public PushBuilder setHeader(String name, String value) {
//        setHeader(name, value, true);
//        return this;
//    }
//
//    private void setHeader(String name, String value, boolean replace) {
//        Map<String, HeaderValue> emptyHeaders = Collections.emptyMap();
//        if (headers == emptyHeaders) {
//            headers = new HashMap<>();
//        }
//        if (replace) {
//            if (value == null) {
//                headers.remove(name);
//            } else {
//                headers.put(name, new HeaderValue(null, value));
//            }
//            return;
//        }
//
//        HeaderValue headerValue = headers.get(name);
//        if (headerValue == null) {
//            setHeader(name, value, true);
//            return;
//        }
//        HeaderValue preHeaderValue = null;
//        while (headerValue != null && !headerValue.getValue().equals(value)) {
//            preHeaderValue = headerValue;
//            headerValue = headerValue.getNextValue();
//        }
//        if (headerValue == null) {
//            preHeaderValue.setNextValue(new HeaderValue(null, value));
//        }
//    }
//
//    @Override
//    public PushBuilder addHeader(String name, String value) {
//        setHeader(name, value, false);
//        return this;
//    }
//
//    @Override
//    public PushBuilder removeHeader(String name) {
//        headers.remove(name);
//        return this;
//    }
//
//    @Override
//    public PushBuilder path(String path) {
//        this.path = path;
//        return this;
//    }
//
//    @Override
//    public void push() {
//        if (path == null) {
//            throw UndertowServletMessages.MESSAGES.pathWasNotSet();
//        }
//        ServerConnection con = servletRequest.getExchange().getConnection();
//        if (con.isPushSupported()) {
//            HeaderMap newHeaders = new HeaderMap();
//            for (HeaderValues entry : headers) {
//                newHeaders.addAll(entry.getHeaderName(), entry);
//            }
//            if (sessionId != null) {
//                newHeaders.put(Headers.COOKIE, "JSESSIONID=" + sessionId); //TODO: do this properly, may be a different tracking method or a different cookie name
//            }
//            String path = this.path;
//            if (!path.startsWith("/")) {
//                path = servletRequest.getContextPath() + "/" + path;
//            }
//            if (queryString != null && !queryString.isEmpty()) {
//                if (path.contains("?")) {
//                    path += "&" + queryString;
//                } else {
//                    path += "?" + queryString;
//                }
//            }
//            con.pushResource(path, new HttpString(method), newHeaders);
//        }
//        path = null;
//        for (String h : CONDITIONAL) {
//            headers.remove(h);
//        }
//    }
//
//    @Override
//    public String getMethod() {
//        return method;
//    }
//
//    @Override
//    public String getQueryString() {
//        return queryString;
//    }
//
//    @Override
//    public String getSessionId() {
//        return sessionId;
//    }
//
//    @Override
//    public Set<String> getHeaderNames() {
//        return headers.keySet();
//    }
//
//    @Override
//    public String getHeader(String name) {
//        HeaderValue headerValue = headers.get(name);
//        if (headerValue != null) {
//            return headerValue.getValue();
//        }
//        return null;
//    }
//
//    @Override
//    public String getPath() {
//        return path;
//    }
//
//}
