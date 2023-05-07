/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.plugins.PluginException;
import org.smartboot.servlet.provider.SessionProvider;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
class MockSessionProvider implements SessionProvider {


    @Override
    public HttpSession getSession(HttpServletRequestImpl request, HttpServletResponse response, boolean create) {
        throw new PluginException("Please install the [session] plugin to enable the [getSessionManager] function");
    }

}
