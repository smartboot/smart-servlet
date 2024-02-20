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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.Principal;

public interface SecurityProvider {
    public void login(String username, String password) throws ServletException;
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException;
    public boolean isUserInRole(String role);
    public Principal getUserPrincipal();
    public String getAuthType();
    public void logout() throws ServletException;
}
