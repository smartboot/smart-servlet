/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionManager.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionContext;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/21
 */
public interface SessionManager extends HttpSessionContext {

    public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    public static final String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";

    public HttpSession getSession(HttpServletRequest request);

    public HttpSession createSession(ServletContext servletContext);

    /**
     * 移除会话
     *
     * @param sessionId
     */
    void removeSession(String sessionId);
}
