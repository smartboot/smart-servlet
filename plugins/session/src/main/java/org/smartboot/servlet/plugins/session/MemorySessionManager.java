/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MemorySessionManager.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins.session;

import org.smartboot.servlet.session.SessionManager;

import javax.servlet.ServletContext;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
public class MemorySessionManager implements SessionManager {

    /**
     * 会话检查周期
     */
    private static final int SESSION_TIME_CHECK_STEP = 2000;
    private final Map<String, HttpSessionImpl> sessionMap = new ConcurrentHashMap<>();
    private final int maxInactiveInterval = 60 * 30;
    private long lastTime;


    @Override
    public HttpSession getSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        String sessionId = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (SessionManager.DEFAULT_SESSION_COOKIE_NAME.equals(cookie.getName())) {
                    sessionId = cookie.getValue();
                    break;
                }
            }
        }
        if (sessionId == null) {
            sessionId = request.getParameter(SessionManager.DEFAULT_SESSION_PARAMETER_NAME);
        }
        HttpSessionImpl session = sessionId == null ? null : sessionMap.get(sessionId);
        if (session != null) {
            session.setLastAccessed(System.currentTimeMillis());
        }
        return session;
    }

    @Override
    public HttpSession createSession(ServletContext servletContext) {
        HttpSessionImpl session = new HttpSessionImpl(this, String.valueOf(System.currentTimeMillis()), servletContext);
        session.setMaxInactiveInterval(maxInactiveInterval);
        sessionMap.put(session.getId(), session);
        return session;
    }

    @Override
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

    private void removeTimeoutSession() {
        if (lastTime + SESSION_TIME_CHECK_STEP > System.currentTimeMillis()) {
            return;
        }
        sessionMap.values().forEach(session -> {
        });
    }
}
