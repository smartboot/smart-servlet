/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.exception.WrappedRuntimeException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletServiceHandler extends Handler {

    @Override
    public void handleRequest(HandlerContext handlerContext) {
        try {
            HttpServletRequest request = handlerContext.getRequest();
            HttpServletResponse response = handlerContext.getResponse();
            //成功匹配到Servlet,直接执行
            if (handlerContext.getServletInfo() != null) {
                handlerContext.getServletInfo().getServlet().service(request, response);
            } else {
                handlerContext.getServletContext().getServlet(ServletInfo.DEFAULT_SERVLET_NAME).service(request, response);
            }
        } catch (ServletException | IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
