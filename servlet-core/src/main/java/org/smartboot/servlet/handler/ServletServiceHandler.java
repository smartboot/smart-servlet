/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletServiceHandler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletServiceHandler extends Handler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletServiceHandler.class);

    @Override
    public void handleRequest(HandlerContext handlerContext) {
        try {
            Servlet servlet = handlerContext.getServlet();
            HttpServletRequest request = handlerContext.getRequest();
            HttpServletResponse response = handlerContext.getResponse();
            //成功匹配到Servlet,直接执行
            if (handlerContext.getServlet() != null) {
                servlet.service(request, response);
                return;
            }

            ServletContextImpl servletContext = handlerContext.getServletContext();
            //requestURI为本地资源文件
            if (isFile(servletContext.getResource(request.getRequestURI().substring(request.getContextPath().length())))) {
                servletContext.getDeploymentInfo().getDefaultServlet().service(request, response);
                return;
            }

            //尝试跳转welcome文件
            String welcome = forwardWelcome(handlerContext);
            if (welcome == null) {
                //无welcome file，触发 404
                servletContext.getDeploymentInfo().getDefaultServlet().service(request, response);
            } else if (welcome.endsWith("/")) {
                // 以"/"通过302跳转触发 welcome file逻辑
                LOGGER.info("执行 welcome 302跳转...");
                handlerContext.getResponse().sendRedirect(welcome);
            } else {
                //找到有效welcome file，执行服务端跳转
                LOGGER.info("执行 welcome 服务端跳转...");
                handlerContext.getRequest().getRequestDispatcher(welcome).forward(handlerContext.getRequest(), handlerContext.getResponse());
            }
        } catch (ServletException | URISyntaxException | IOException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    private String forwardWelcome(HandlerContext handlerContext) throws MalformedURLException, URISyntaxException {
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
            // 例如: /abc/d.html ,由于d.html不存在而走到该分支
            if (requestUri.indexOf(".") > 0) {
                return null;
            }
            if (isFile(servletContext.getResource(requestUri.substring(handlerContext.getRequest().getContextPath().length())))) {
                return null;
            }
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

    private boolean isFile(URL url) throws URISyntaxException {
        return url != null && new File(url.toURI()).isFile();
    }
}
