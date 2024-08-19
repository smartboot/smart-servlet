/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
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
            HttpSession session = req.getSession();
            req.setAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD, session.getAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD));
            req.getRequestDispatcher((String) session.getAttribute(SecurityProvider.LOGIN_REDIRECT_URI)).forward(req, resp);
        }
    }
}
