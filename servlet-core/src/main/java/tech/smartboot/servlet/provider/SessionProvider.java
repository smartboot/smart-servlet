/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.provider;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;

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

    void pauseAccessTime(HttpServletRequestImpl request);

    void destroy();

    boolean isRequestedSessionIdValid(HttpServletRequestImpl request);
}
