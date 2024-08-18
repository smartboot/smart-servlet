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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.smartboot.servlet.conf.SecurityConstraint;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.plugins.security.LoginAccount;
import tech.smartboot.servlet.plugins.security.SecurityAccount;

import java.io.IOException;
import java.util.List;
import java.util.Set;

public interface SecurityProvider {
    void addUser(String username, String password, Set<String> roles);

    void init(List<SecurityConstraint> constraints);

    public SecurityAccount login(String username, String password) throws ServletException;

    public boolean authenticate(HttpServletRequestImpl httpServletRequest, HttpServletResponse response) throws IOException, ServletException;

    public boolean isUserInRole(String role, LoginAccount loginAccount, HttpServletRequestImpl httpServletRequest);


    public void logout(HttpServletRequestImpl httpServletRequest) throws ServletException;

    public SecurityAccount login(HttpServletRequest request) throws ServletException;
}
