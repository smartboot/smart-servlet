/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletServiceHandler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.exception.WrappedRuntimeException;

import javax.servlet.Servlet;
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
            Servlet servlet = handlerContext.getServlet();
            HttpServletRequest request = handlerContext.getRequest();
            HttpServletResponse response = handlerContext.getResponse();
            //成功匹配到Servlet,直接执行
            if (handlerContext.getServlet() != null) {
                servlet.service(request, response);
            } else {
                handlerContext.getServletContext().getDeploymentInfo().getDefaultServlet().service(request, response);
            }
        } catch (ServletException | IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }
}
