/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletMatchHandler.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.conf.ServletMappingInfo;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.util.ServletPathMatcher;

import javax.servlet.Servlet;
import java.util.Map;
import java.util.logging.Level;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletMatchHandler extends Handler {
    private static final ServletPathMatcher PATH_MATCHER = new ServletPathMatcher();

    @Override
    public void handleRequest(HandlerContext handlerContext) {
        //匹配Servlet
        Servlet servlet = null;
        ServletContextImpl servletContext = handlerContext.getServletContext();
        String contextPath = servletContext.getContextPath();
        Map<String, ServletInfo> servletInfoMap = handlerContext.getServletContext().getDeploymentInfo().getServlets();
        HttpServletRequestImpl request = handlerContext.getRequest();

        //默认地址改写
        DeploymentInfo deploymentInfo = servletContext.getDeploymentInfo();
        if (StringUtils.isBlank(deploymentInfo.getWelcomeFile())) {
            request.setRequestUri(request.getRequestURI());
        } else {
            int i = request.getRequestURI().length() - servletContext.getContextPath().length();
            if (i == 0) {
                request.setRequestUri(request.getRequestURI() + deploymentInfo.getWelcomeFile());
            } else if (i == 1 && request.getRequestURI().charAt(request.getRequestURI().length() - 1) == '/') {
                request.setRequestUri(request.getRequestURI().substring(0, request.getRequestURI().length() - 1) + servletContext.getDeploymentInfo().getWelcomeFile());
            } else {
                request.setRequestUri(request.getRequestURI());
            }
        }

        for (Map.Entry<String, ServletInfo> entry : servletInfoMap.entrySet()) {
            final ServletInfo servletInfo = entry.getValue();
            for (ServletMappingInfo path : servletInfo.getMappings()) {
                RunLogger.getLogger().log(Level.SEVERE, "servlet match: " + (contextPath + path.getMapping()) + " requestURI: " + request.getRequestURI());
                if ("/".equals(path.getMapping()) || PATH_MATCHER.matches(contextPath + path.getMapping(), request.getRequestURI())) {
                    servlet = servletInfo.getServlet();
                    setServletInfo(request, path);
                    break;
                }
            }
            if (servlet != null) {
                break;
            }
        }
        if (servlet == null) {
            servlet = servletContext.getDeploymentInfo().getDefaultServlet();
        }
        handlerContext.setServlet(servlet);
        doNext(handlerContext);
    }

    /**
     * 《Servlet3.1规范中文版》3.5请求路径元素
     *
     * @param request
     * @param path
     */
    private void setServletInfo(HttpServletRequestImpl request, ServletMappingInfo path) {
        String servletPath = null;
        String pathInfo = null;
        switch (path.getMappingType()) {
            case EXACT_MATCH:
                servletPath = path.getMapping();
                pathInfo = "/" + StringUtils.substringAfter(request.getRequestURI(), request.getContextPath() + servletPath);
                if ("/".equals(servletPath)) {
                    servletPath = pathInfo;
                    pathInfo = null;
                }
                break;
            case PREFIX_MATCH:
                servletPath = path.getMapping().substring(0, path.getMapping().length() - 2);
                pathInfo = StringUtils.substringAfter(request.getRequestURI(), request.getContextPath() + servletPath);
                break;
            case EXTENSION_MATCH:
                servletPath = request.getRequestURI().substring(request.getContextPath().length());
                pathInfo = null;
                break;
            default:
        }
        request.setServletPath(servletPath);
        request.setPathInfo(pathInfo);
        RunLogger.getLogger().log(Level.SEVERE, "contextPath: " + request.getContextPath() + " , servletPath: " + request.getServletPath() + " ,pathInfo: " + request.getPathInfo());
    }
}
