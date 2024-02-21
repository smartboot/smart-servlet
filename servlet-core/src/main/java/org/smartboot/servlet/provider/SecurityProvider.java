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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface SecurityProvider {
    public void login(String username, String password, HttpServletRequestImpl httpServletRequest) throws ServletException;

    public boolean authenticate(HttpServletRequestImpl httpServletRequest, HttpServletResponse response) throws IOException, ServletException;

    public boolean isUserInRole(String role, HttpServletRequestImpl httpServletRequest);


    public void logout(HttpServletRequestImpl httpServletRequest) throws ServletException;
}
