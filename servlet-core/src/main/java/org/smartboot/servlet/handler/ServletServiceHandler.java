/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletServiceHandler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.exception.HttpException;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

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
            if (servlet == handlerContext.getServletContext().getDeploymentInfo().getDefaultServlet()) {
                String welcome = forwardWelcome(handlerContext);
                if (welcome != null) {
                    handlerContext.getRequest().getRequestDispatcher(welcome).forward(handlerContext.getRequest(), handlerContext.getResponse());
                    return;
                }
            }
            if (servlet == null) {
                throw new HttpException(HttpStatus.NOT_FOUND);
            }
            servlet.service(handlerContext.getRequest(), handlerContext.getResponse());
        } catch (ServletException | IOException e) {
            throw new WrappedRuntimeException(e);
        }
        doNext(handlerContext);
    }

    private String forwardWelcome(HandlerContext handlerContext) throws MalformedURLException {
        ServletContextImpl servletContext = handlerContext.getServletContext();
        List<String> welcomeFiles = servletContext.getDeploymentInfo().getWelcomeFiles();
        String requestUri = handlerContext.getRequest().getRequestURI();
        //已经是以welcomeFile结尾的不再进行匹配
        for (String file : welcomeFiles) {
            if (requestUri.endsWith(file)) {
                return null;
            }
        }
        if (!requestUri.endsWith("/")) {
            return requestUri + "/";
        } else {
            for (String file : welcomeFiles) {
                String uri = requestUri.substring(handlerContext.getRequest().getContextPath().length());
                URL welcomeUrl = servletContext.getResource(uri + file);
                if (welcomeUrl != null) {
                    return file;
                }
            }
        }

        return null;
    }
}
