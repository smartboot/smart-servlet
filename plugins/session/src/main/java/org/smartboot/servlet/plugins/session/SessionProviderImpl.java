/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MemorySessionManager.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.session;

import org.smartboot.servlet.provider.SessionProvider;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/21
 */
class SessionProviderImpl implements SessionProvider, HttpSessionContext {
    /**
     * 会话检查周期
     */
    private static final int SESSION_TIME_CHECK_STEP = 2000;
    private static final String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";
    private static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    private final Map<String, HttpSessionImpl> sessionMap = new ConcurrentHashMap<>();
    private final int maxInactiveInterval = 60 * 30;
    private long lastTime;


    public void clearExpireSession() {
        List<HttpSessionImpl> list = new ArrayList<>(sessionMap.values());
        list.stream().filter(httpSession -> httpSession.getMaxInactiveInterval() > 0
                && httpSession.getLastAccessedTime() + httpSession.getMaxInactiveInterval() > System.currentTimeMillis())
                .forEach(httpSession -> {
                    try {
                        httpSession.invalid();
                    } finally {
                        sessionMap.remove(httpSession.getId());
                    }
                });
    }

    @Override
    public HttpSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

    @Override
    public Enumeration<String> getIds() {
        return Collections.enumeration(sessionMap.keySet());
    }


    @Override
    public HttpSession getSession(HttpServletRequest request, boolean create) {
        HttpSessionImpl httpSession = getSession(request);
        if (create && httpSession == null) {
            httpSession = new HttpSessionImpl(this, String.valueOf(System.currentTimeMillis()), request.getServletContext());
            httpSession.setMaxInactiveInterval(maxInactiveInterval);
            sessionMap.put(httpSession.getId(), httpSession);
        }
        return httpSession;
    }

    private HttpSessionImpl getSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String sessionId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (DEFAULT_SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        if (sessionId == null) {
            sessionId = request.getParameter(DEFAULT_SESSION_PARAMETER_NAME);
        }
        HttpSessionImpl session = sessionId == null ? null : sessionMap.get(sessionId);
        if (session != null) {
            session.setLastAccessed(System.currentTimeMillis());
        }
        return session;
    }
}
