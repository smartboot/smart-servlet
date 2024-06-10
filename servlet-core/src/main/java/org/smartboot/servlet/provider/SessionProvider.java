/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.provider;

import org.smartboot.servlet.impl.HttpServletRequestImpl;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.function.Function;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public interface SessionProvider {
    String DEFAULT_SESSION_PARAMETER_NAME = "jsessionid";
    String DEFAULT_SESSION_COOKIE_NAME = "JSESSIONID";

    HttpSession getSession(HttpServletRequestImpl request, HttpServletResponse response, boolean create);

    void changeSessionId(HttpSession httpSession);

    void updateAccessTime(HttpServletRequestImpl request);

    boolean isRequestedSessionIdValid(HttpServletRequestImpl request);

    void sessionIdFactory(Function<HttpServletRequestImpl, String> factory);
}
