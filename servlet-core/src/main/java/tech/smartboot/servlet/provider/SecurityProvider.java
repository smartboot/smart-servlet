/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tech.smartboot.servlet.provider;

import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletResponse;
import tech.smartboot.servlet.SmartHttpServletRequest;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.plugins.security.LoginAccount;
import tech.smartboot.servlet.plugins.security.SecurityAccount;

import java.io.IOException;
import java.util.Set;

public interface SecurityProvider {
    String LOGIN_REDIRECT_URI = SecurityProvider.class.getName() + "_login_redirect_uri";
    String LOGIN_REDIRECT_METHOD = SecurityProvider.class.getName() + "_login_redirect_method";

    void addUser(String username, String password, Set<String> roles);

    public SecurityAccount login(String username, String password) throws ServletException;

    public boolean authenticate(HttpServletRequestImpl httpServletRequest, HttpServletResponse response) throws IOException, ServletException;

    public boolean isUserInRole(String role, LoginAccount loginAccount, HttpServletRequestImpl httpServletRequest);


    public void logout(HttpServletRequestImpl httpServletRequest) throws ServletException;

    public boolean login(SmartHttpServletRequest request, ServletResponse response, ServletInfo servletInfo) throws ServletException, IOException;
}
