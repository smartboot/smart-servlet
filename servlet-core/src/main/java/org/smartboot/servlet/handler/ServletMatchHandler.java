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
import org.smartboot.servlet.conf.UriMappingInfo;
import org.smartboot.servlet.enums.UriMappingTypeEnum;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.util.PathMatcherUtil;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 匹配并执行符合当前请求的Servlet
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletMatchHandler extends Handler {
    /**
     * 缓存URI-Servlet映射关系
     */
    private final Map<String, CacheServlet> cacheServletMap = new ConcurrentHashMap<>();

    @Override
    public void handleRequest(HandlerContext handlerContext) {
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
        //通过缓存匹配Servlet
        CacheServlet cacheServlet = handlerContext.isNamedDispatcher() ? null : cacheServletMap.get(request.getRequestURI());
        if (cacheServlet != null) {
            handlerContext.setServlet(cacheServlet.servlet);
            request.setServletPath(cacheServlet.servletPathStart, cacheServlet.servletPathEnd);
            request.setPathInfo(cacheServlet.pathInfoStart, cacheServlet.pathInfoEnd);
        }
        //匹配Servlet
        if (handlerContext.getServlet() == null) {
            Servlet servlet = null;
            for (Map.Entry<String, ServletInfo> entry : servletInfoMap.entrySet()) {
                final ServletInfo servletInfo = entry.getValue();
                for (UriMappingInfo path : servletInfo.getMappings()) {
                    int servletPathEndIndex = PathMatcherUtil.matches(request.getRequestURI(), contextPath.length(), path);
                    if (servletPathEndIndex > -1) {
                        servlet = servletInfo.getServlet();
                        //《Servlet3.1规范中文版》3.5请求路径元素
                        int servletPathStart = request.getContextPath().length();
                        int servletPathEnd = servletPathEndIndex;
                        int pathInfoStart;
                        int pathInfoEnd;
                        if (path.getMappingType() != UriMappingTypeEnum.PREFIX_MATCH || servletPathEndIndex == request.getRequestURI().length()) {
                            //精确匹配和后缀匹配的 PathInfo 都为null
                            pathInfoStart = -1;
                            pathInfoEnd = -1;
                            request.setPathInfo(-1, -1);
                        } else {
                            pathInfoStart = servletPathEndIndex;
                            pathInfoEnd = request.getRequestURI().length();

                        }
                        request.setServletPath(servletPathStart, servletPathEnd);
                        request.setPathInfo(pathInfoStart, pathInfoEnd);
                        cacheServletMap.put(request.getRequestURI(), new CacheServlet(servlet, servletPathStart, servletPathEnd, pathInfoStart, pathInfoEnd));
                        break;
                    }
                }
                if (servlet != null) {
                    handlerContext.setServlet(servlet);
                    break;
                }
            }
        }

        doNext(handlerContext);
    }

    static class CacheServlet {
        private final Servlet servlet;
        private final int servletPathStart;
        private final int servletPathEnd;
        private final int pathInfoStart;
        private final int pathInfoEnd;

        public CacheServlet(Servlet servlet, int servletPathStart, int servletPathEnd, int pathInfoStart, int pathInfoEnd) {
            this.servlet = servlet;
            this.servletPathStart = servletPathStart;
            this.servletPathEnd = servletPathEnd;
            this.pathInfoStart = pathInfoStart;
            this.pathInfoEnd = pathInfoEnd;
        }
    }
}
