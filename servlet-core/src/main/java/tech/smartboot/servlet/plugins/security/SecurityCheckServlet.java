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

package tech.smartboot.servlet.plugins.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import tech.smartboot.servlet.conf.DeploymentInfo;
import tech.smartboot.servlet.conf.LoginConfig;
import tech.smartboot.servlet.provider.SecurityProvider;

import java.io.IOException;

public class SecurityCheckServlet extends HttpServlet {
    private DeploymentInfo deploymentInfo;

    public SecurityCheckServlet(DeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LoginConfig loginConfig = deploymentInfo.getLoginConfig();
        if (loginConfig != null && "FORM".equals(loginConfig.getAuthMethod())) {
            req.login(req.getParameter("j_username"), req.getParameter("j_password"));
            if(req.getUserPrincipal()==null){
                req.getRequestDispatcher(loginConfig.getErrorPage()).forward(req, resp);
            }else{
                HttpSession session = req.getSession();
                req.setAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD, session.getAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD));
                req.getRequestDispatcher((String) session.getAttribute(SecurityProvider.LOGIN_REDIRECT_URI)).forward(req, resp);
            }

        }
    }
}
