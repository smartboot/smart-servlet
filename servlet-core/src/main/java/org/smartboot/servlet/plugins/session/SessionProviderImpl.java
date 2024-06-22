/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.plugins.session;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.socket.timer.HashedWheelTimer;

import javax.servlet.SessionCookieConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/21
 */
class SessionProviderImpl implements SessionProvider, HttpSessionContext {

    private static final Logger LOGGER = LoggerFactory.getLogger(SessionProviderImpl.class);
    private Function<HttpServletRequestImpl, String> sessionIdFactory = request -> "smart-servlet:" + new SimpleDateFormat("yyyyMMddHHmmss").format(System.currentTimeMillis());
    /**
     * 默认超时时间：30分钟
     */
    private static final int DEFAULT_MAX_INACTIVE_INTERVAL = 30 * 60;
    /**
     * 当前会话集合
     */
    private final Map<String, HttpSessionImpl> sessionMap = new ConcurrentHashMap<>();
    /**
     * 会话超时时间
     */
    private int maxInactiveInterval = DEFAULT_MAX_INACTIVE_INTERVAL;

    private final HashedWheelTimer timer = new HashedWheelTimer(r -> new Thread(r, "smartboot-session-timer"), 10, 64);

    @Override
    public HttpSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    @Override
    public Enumeration<String> getIds() {
        return Collections.enumeration(sessionMap.keySet());
    }


    @Override
    public HttpSessionImpl getSession(HttpServletRequestImpl request, HttpServletResponse response, boolean create) {
        HttpSessionImpl httpSession = getSession(request);
        if (create && httpSession == null) {
            /**
             * javax.servlet.http.HttpServletRequest#getSession(boolean)接口规范：
             * If the container is using cookies to maintain session integrity and is asked to create a new session when the response is committed, an IllegalStateException is thrown.
             */
            if (response.isCommitted()) {
                throw new IllegalStateException("response has already committed!");
            }
            //该sessionId生成策略缺乏安全性，后续重新设计
            httpSession = new HttpSessionImpl(this, sessionIdFactory.apply(request), request.getServletContext()) {
                @Override
                public void invalid() {
                    try {
                        super.invalid();
                    } finally {
                        sessionMap.remove(getId());
                    }
                }
            };
            httpSession.setMaxInactiveInterval(maxInactiveInterval);
            SessionCookieConfig sessionCookieConfig = request.getServletContext().getSessionCookieConfig();
            Cookie cookie = new Cookie(sessionCookieConfig.getName(), httpSession.getId());
            cookie.setPath(request.getRequestURI());
            cookie.setComment(sessionCookieConfig.getComment());
            if (sessionCookieConfig.getDomain() != null) {
                cookie.setDomain(sessionCookieConfig.getDomain());
            }
            cookie.setHttpOnly(sessionCookieConfig.isHttpOnly());
            cookie.setSecure(sessionCookieConfig.isSecure());
            cookie.setMaxAge(sessionCookieConfig.getMaxAge());
            response.addCookie(cookie);
            sessionMap.put(httpSession.getId(), httpSession);
            request.setActualSessionId(httpSession.getId());
        }
        if (httpSession != null) {
            httpSession.setResponse(response);
        }
        return httpSession;
    }

    @Override
    public void changeSessionId(HttpSession httpSession) {
        if (!(httpSession instanceof HttpSessionImpl)) {
            throw new IllegalStateException();
        }
        HttpSessionImpl session = sessionMap.remove(httpSession.getId());
        session.changeSessionId(String.valueOf(System.currentTimeMillis()));
        sessionMap.put(session.getId(), session);
    }

    @Override
    public void updateAccessTime(HttpServletRequestImpl request) {
        HttpSessionImpl session = getSession(request);
        if (session != null) {
            session.setLastAccessed(System.currentTimeMillis());
        }
    }

    @Override
    public boolean isRequestedSessionIdValid(HttpServletRequestImpl request) {
        HttpSessionImpl session = getSession(request);
        if (session == null) {
            return false;
        }
        return session.getId().equals(request.getRequestedSessionId());
    }

    @Override
    public void sessionIdFactory(Function<HttpServletRequestImpl, String> factory) {
        this.sessionIdFactory = factory;
    }

    private HttpSessionImpl getSession(HttpServletRequestImpl request) {
        String sessionId = request.getActualSessionId();
        if (sessionId == null) {
            sessionId = request.getRequestedSessionId();
        }
        if (sessionId == null) {
            return null;
        }
        HttpSessionImpl session = sessionMap.get(sessionId);
        if (session == null || session.isInvalid()) {
            return null;
        }
        return session;
    }

    public void setMaxInactiveInterval(int maxInactiveInterval) {
        this.maxInactiveInterval = maxInactiveInterval;
    }

    public HashedWheelTimer getTimer() {
        return timer;
    }
}
