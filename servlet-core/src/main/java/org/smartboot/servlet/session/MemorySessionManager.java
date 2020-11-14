/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MemorySessionManager.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.session;

import org.smartboot.servlet.impl.HttpSessionImpl;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/21
 */
public class MemorySessionManager implements SessionManager {

    private Map<String, HttpSession> sessionMap = new ConcurrentHashMap<>();


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
        return sessionId == null ? null : sessionMap.get(sessionId);
    }

    @Override
    public HttpSession createSession() {
        HttpSession session = new HttpSessionImpl(String.valueOf(System.currentTimeMillis()));
        sessionMap.put(session.getId(), session);
        return session;
    }
}
