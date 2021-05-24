/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SessionProvider.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.provider;

import org.smartboot.servlet.impl.HttpServletRequestImpl;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public interface SessionProvider {
    String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";
    String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";

    HttpSession getSession(HttpServletRequestImpl request, HttpServletResponse response, boolean create);
}
