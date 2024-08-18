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
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smartboot.http.common.enums.HttpStatus;
import tech.smartboot.servlet.conf.SecurityConstraint;
import tech.smartboot.servlet.conf.UrlPattern;
import tech.smartboot.servlet.plugins.security.LoginAccount;
import tech.smartboot.servlet.plugins.security.SecurityAccount;
import tech.smartboot.servlet.util.CollectionUtils;
import tech.smartboot.servlet.util.PathMatcherUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class SecurityHandler extends Handler {
    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) handlerContext.getRequest();
        List<SecurityConstraint> constraints = new ArrayList<>(handlerContext.getServletInfo().getSecurityConstraints());

        constraints.addAll(handlerContext.getServletContext().getDeploymentInfo().getSecurityConstraints().stream().filter(securityConstraint -> {
            for (UrlPattern urlPattern : securityConstraint.getUrlPatterns()) {
                if (PathMatcherUtil.matches((HttpServletRequest) handlerContext.getRequest(), urlPattern)) {
                    return true;
                }
            }
            return false;
        }).toList());
        //移除不匹配的httpMethod
        constraints = constraints.stream().filter(securityConstraint -> !securityConstraint.getHttpMethodOmissions().contains(request.getMethod())).toList();
        //不存在匹配的安全约束
        if (constraints.isEmpty()) {
            doNext(handlerContext);
            return;
        }


        constraints = constraints.stream().filter(securityConstraint -> (
                        //存在角色或者为PERMIT
                        (CollectionUtils.isNotEmpty(securityConstraint.getRoleNames()) || securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.PERMIT))
                        //为配置httpMethod，或者包含指定method
                        && (securityConstraint.getHttpMethods().isEmpty() || securityConstraint.getHttpMethods().contains(request.getMethod())))
                .toList();
        if (constraints.isEmpty()) {
            ((HttpServletResponse) handlerContext.getResponse()).sendError(403);
            return;
        }
        SecurityAccount securityAccount = handlerContext.getServletContext().getRuntime().getSecurityProvider().login(request);
        if (securityAccount == null) {
            ((HttpServletResponse) handlerContext.getResponse()).sendError(HttpStatus.UNAUTHORIZED.value());
            return;
        }
        LoginAccount loginAccount = new LoginAccount(securityAccount.getUsername(), securityAccount.getPassword(), new HashSet<>());
        constraints = constraints.stream().filter(securityConstraint -> {
            if (securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.PERMIT && CollectionUtils.isEmpty(securityConstraint.getRoleNames())) {
                return true;
            }
            for (String role : securityConstraint.getRoleNames()) {
                if (securityAccount.getRoles().contains(role)) {
                    //匹配的角色
                    loginAccount.getRoles().add(role);
                    return true;
                }
            }
            return false;
        }).toList();
        if (constraints.isEmpty()) {
            ((HttpServletResponse) handlerContext.getResponse()).sendError(HttpStatus.FORBIDDEN.value());
            return;
        }
        handlerContext.getOriginalRequest().setLoginAccount(loginAccount);
        doNext(handlerContext);
    }
}
