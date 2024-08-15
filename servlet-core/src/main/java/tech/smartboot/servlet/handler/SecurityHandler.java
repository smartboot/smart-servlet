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
import tech.smartboot.servlet.util.PathMatcherUtil;

import java.io.IOException;

public class SecurityHandler extends Handler {
    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        HttpServletRequest request = (HttpServletRequest) handlerContext.getRequest();
        for (SecurityConstraint securityConstraint : handlerContext.getServletContext().getDeploymentInfo().getSecurityConstraints()) {
            boolean match = false;
            for (UrlPattern urlPattern : securityConstraint.getUrlPatterns()) {
                if (PathMatcherUtil.matches((HttpServletRequest) handlerContext.getRequest(), urlPattern)) {
                    match = true;
                    break;
                }
            }
            if (!match) {
                continue;
            }
            if (!securityConstraint.getHttpMethods().isEmpty() && !securityConstraint.getHttpMethods().contains(request.getMethod())) {
                ((HttpServletResponse) handlerContext.getResponse()).sendError(403);
                return;
            }
        }
        doNext(handlerContext);
    }
}
