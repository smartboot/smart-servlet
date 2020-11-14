/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionManager.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.session;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/21
 */
public interface SessionManager {

    public static final String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";
    public static final String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";

    public HttpSession getSession(HttpServletRequest request);

    public HttpSession createSession();
}
