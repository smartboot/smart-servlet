/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MockSessionManager.java
 * Date: 2020-11-27
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.session.SessionManager;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
class MockSessionManager implements SessionManager {
    private static final String WARNING = "please install session plugin";

    @Override
    public HttpSession getSession(HttpServletRequest request) {
        throw new PluginException(WARNING);
    }

    @Override
    public HttpSession createSession(ServletContext servletContext) {
        throw new PluginException(WARNING);
    }

    @Override
    public void clearExpireSession() {
        throw new PluginException(WARNING);
    }

    @Override
    public HttpSession getSession(String sessionId) {
        throw new PluginException(WARNING);
    }

    @Override
    public Enumeration<String> getIds() {
        throw new PluginException(WARNING);
    }


}
