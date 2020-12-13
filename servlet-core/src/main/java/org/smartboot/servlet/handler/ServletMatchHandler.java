/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletMatchHandler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.SmartHttpServletRequest;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.conf.ServletMappingInfo;
import org.smartboot.servlet.enums.ServletMappingTypeEnum;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.util.ServletPathMatcher;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Map;

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
        SmartHttpServletRequest request = handlerContext.getRequest();

        //通过ServletContext.getNamedDispatcher触发的请求已经指定了Servlet
        if (handlerContext.isNamedDispatcher()) {
            if (handlerContext.getServlet() == null) {
                throw new WrappedRuntimeException(new ServletException("servlet is null"));
            }
        } else if (handlerContext.getServlet() != null) {
            throw new WrappedRuntimeException(new ServletException("servlet is not null"));
        }
        if (handlerContext.getServlet() == null) {
            for (Map.Entry<String, ServletInfo> entry : servletInfoMap.entrySet()) {
                final ServletInfo servletInfo = entry.getValue();
                for (ServletMappingInfo path : servletInfo.getMappings()) {
                    int index = PATH_MATCHER.matches(request.getRequestURI(), contextPath.length(), path);
                    if (index > -1) {
                        servlet = servletInfo.getServlet();
                        //《Servlet3.1规范中文版》3.5请求路径元素
                        request.setServletPath(request.getContextPath().length(), index);
                        if (path.getMappingType() != ServletMappingTypeEnum.PREFIX_MATCH) {
                            //精确匹配和后缀匹配的 PathInfo 都为null
                            request.setPathInfo(-1, -1);
                        } else {
                            request.setPathInfo(0, request.getRequestURI().length());
                        }

                        break;
                    }
                }
                if (servlet != null) {
                    break;
                }
            }
        }

        handlerContext.setServlet(servlet);
        doNext(handlerContext);
    }

}
