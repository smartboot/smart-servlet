/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WelcomeFileHandler.java
 * Date: 2020-11-29
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.impl.ServletContextImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * 《Servlet3.1规范中文版》10.10 欢迎文件
 *
 * @author 三刀
 * @version V1.0 , 2020/11/29
 */
public class WelcomeFileHandler extends Handler {
    @Override
    public void handleRequest(HandlerContext handlerContext) {
        //已成功匹配到Servlet，执行下一步骤
        if (handlerContext.getServlet() != null) {
            doNext(handlerContext);
            return;
        }
        ServletContextImpl servletContext = handlerContext.getServletContext();
        HttpServletRequest request = handlerContext.getRequest();
        try {
            //requestURI为本地资源文件
            if (servletContext.getResource(request.getRequestURI().substring(request.getContextPath().length())) != null) {
                handlerContext.setServlet(servletContext.getDeploymentInfo().getDefaultServlet());
                doNext(handlerContext);
                return;
            }
            //尝试跳转welcome文件
            String welcome = forwardWelcome(handlerContext);
            if (welcome == null) {
                //无welcome file，执行下一步触发 404
                doNext(handlerContext);
            } else if (welcome.endsWith("/")) {
                // 以"/"通过302跳转触发 welcome file逻辑
                handlerContext.getResponse().sendRedirect(welcome);
            } else {
                //找到有效welcome file，执行服务端跳转
                handlerContext.getRequest().getRequestDispatcher(welcome).forward(handlerContext.getRequest(), handlerContext.getResponse());
            }
        } catch (ServletException | IOException e) {
            throw new WrappedRuntimeException(e);
        }
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
            if (servletContext.getResource(requestUri.substring(handlerContext.getRequest().getContextPath().length())) != null) {
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
}
