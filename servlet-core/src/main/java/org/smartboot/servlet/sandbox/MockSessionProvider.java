/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MockSessionProvider.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.provider.SessionProvider;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
class MockSessionProvider implements SessionProvider {


    @Override
    public HttpSession getSession(HttpServletRequest request, boolean create) {
        throw new PluginException("Please install the [session] plugin to enable the [getSessionManager] function");
    }

}
