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
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smartboot.http.common.enums.HttpStatus;

import java.io.IOException;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletServiceHandler extends Handler {

    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        ServletRequest request = handlerContext.getRequest();
        ServletResponse response = handlerContext.getResponse();
        if (handlerContext.getServletInfo() == null) {
            throw new ServletException("servlet is null");
        }
        //成功匹配到Servlet,直接执行
        try {
            if (!handlerContext.getServletInfo().initialized()) {
                handlerContext.getServletInfo().init(handlerContext.getServletContext());
            }
            handlerContext.getServletInfo().getServlet().service(request, response);
        } catch (Throwable e) {
            ((HttpServletResponse) response).setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            Throwable throwable = e;
            String location = handlerContext.getServletContext().getDeploymentInfo().getErrorPageLocation(throwable);
            while (location == null && throwable.getCause() != null) {
                location = handlerContext.getServletContext().getDeploymentInfo().getErrorPageLocation(throwable.getCause());
                throwable = throwable.getCause();
            }

            if (location == null) {
                location = handlerContext.getServletContext().getDeploymentInfo().getErrorPageLocation(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            if (location != null) {
                handlerContext.getServletContext().getRuntime().getDispatcherProvider().error(handlerContext.getServletContext(), location, (HttpServletRequest) request, (HttpServletResponse) response, throwable, handlerContext.getServletInfo().getServletName(), throwable.getMessage());
            } else {
                throw e;
            }
        }
    }
}
