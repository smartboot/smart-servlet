/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.smartboot.servlet.conf.SecurityConstraint;
import tech.smartboot.servlet.conf.UrlPattern;
import tech.smartboot.servlet.plugins.security.UserTO;
import tech.smartboot.servlet.util.PathMatcherUtil;

import java.io.IOException;
import java.util.List;

public class SecurityHandler extends Handler {
    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) handlerContext.getRequest();
//        UserTO userTO = null;
//        if (!handlerContext.getServletInfo().getSecurityRoles().isEmpty()) {
//            userTO = handlerContext.getServletContext().getRuntime().getSecurityProvider().getUser(request);
//            if (userTO == null) {
//                ((HttpServletResponse) handlerContext.getResponse()).sendError(403);
//                return;
//            }
//            boolean valid = false;
//            for (String role : handlerContext.getServletInfo().getSecurityRoles().values()) {
//                if (userTO.getRoles().contains(role)) {
//                    valid = true;
//                    break;
//                }
//            }
//            if (!valid) {
//                ((HttpServletResponse) handlerContext.getResponse()).sendError(403);
//                return;
//            }
//        }

        List<SecurityConstraint> constraints = handlerContext.getServletInfo().getSecurityConstraints();
        if (constraints.isEmpty()) {
            constraints = handlerContext.getServletContext().getDeploymentInfo().getSecurityConstraints().stream().filter(securityConstraint -> {
                for (UrlPattern urlPattern : securityConstraint.getUrlPatterns()) {
                    if (PathMatcherUtil.matches((HttpServletRequest) handlerContext.getRequest(), urlPattern)) {
                        return true;
                    }
                }
                return false;
            }).toList();
        }
        if (constraints.isEmpty()) {
            doNext(handlerContext);
            return;
        }
        constraints = constraints.stream().filter(securityConstraint -> securityConstraint.getRoleNames() == null || securityConstraint.getHttpMethods().isEmpty() || securityConstraint.getHttpMethods().contains(request.getMethod())).toList();
        if (constraints.isEmpty()) {
            ((HttpServletResponse) handlerContext.getResponse()).sendError(403);
            return;
        }
        UserTO userTO = handlerContext.getServletContext().getRuntime().getSecurityProvider().getUser(request);
        constraints = constraints.stream().filter(securityConstraint -> {
            for (String role : securityConstraint.getRoleNames()) {
                if (userTO.getRoles().contains(role)) {
                    return true;
                }
            }
            return false;
        }).toList();
        if (constraints.isEmpty()) {
            ((HttpServletResponse) handlerContext.getResponse()).sendError(403);
            return;
        }
        doNext(handlerContext);
    }
}
