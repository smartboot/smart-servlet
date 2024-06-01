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

import org.smartboot.servlet.SmartHttpServletRequest;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.conf.ServletMappingInfo;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.util.PathMatcherUtil;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
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
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        ServletContextImpl servletContext = handlerContext.getServletContext();
        String contextPath = servletContext.getContextPath();
        Map<String, ServletInfo> servletInfoMap = handlerContext.getServletContext().getDeploymentInfo().getServlets();
        SmartHttpServletRequest request = handlerContext.getOriginalRequest();

        //通过ServletContext.getNamedDispatcher触发的请求已经指定了Servlet
        if (handlerContext.isNamedDispatcher()) {
            if (handlerContext.getServletInfo() == null) {
                throw new WrappedRuntimeException(new ServletException("servlet is null"));
            }
        } else if (handlerContext.getServletInfo() != null) {
            throw new WrappedRuntimeException(new ServletException("servlet is not null"));
        }
        //通过缓存匹配Servlet
        CacheServlet cacheServlet = handlerContext.isNamedDispatcher() ? null : cacheServletMap.get(request.getRequestURI());
        if (cacheServlet != null) {
            handlerContext.setServletInfo(cacheServlet.servletInfo);
            request.setServletPath(cacheServlet.servletPathStart, cacheServlet.servletPathEnd);
            request.setPathInfo(cacheServlet.pathInfoStart, cacheServlet.pathInfoEnd);
        }
        //匹配Servlet
        if (handlerContext.getServletInfo() == null) {
            Servlet servlet = null;
            for (Map.Entry<String, ServletInfo> entry : servletInfoMap.entrySet()) {
                final ServletInfo servletInfo = entry.getValue();
                if (!servletInfo.initialized()) {
                    servletInfo.init(handlerContext.getServletContext());
                }
                if (ServletInfo.DEFAULT_SERVLET_NAME.equals(servletInfo.getServletName())) {
                    continue;
                }
                for (ServletMappingInfo path : servletInfo.getMappings()) {
                    int servletPathEnd = PathMatcherUtil.matches(request.getRequestURI(), contextPath.length(), path);
                    //匹配失败
                    if (servletPathEnd < 0) {
                        continue;
                    }
                    servlet = servletInfo.getServlet();
                    //《Servlet3.1规范中文版》3.5请求路径元素
                    int servletPathStart = request.getContextPath().length();
                    int pathInfoStart;
                    int pathInfoEnd;
                    if (servletPathEnd == 0) {
                        //精确匹配和后缀匹配的 PathInfo 都为null
                        servletPathEnd = servletPathStart;
                    }
                    if (servletPathEnd == request.getRequestURI().length()) {
                        pathInfoStart = pathInfoEnd = -1;
                    } else {
                        pathInfoStart = servletPathEnd;
                        pathInfoEnd = request.getRequestURI().length();
                    }
                    request.setServletPath(servletPathStart, servletPathEnd);
                    request.setPathInfo(pathInfoStart, pathInfoEnd);
                    cacheServletMap.put(request.getRequestURI(), new CacheServlet(servletInfo, servletPathStart, servletPathEnd, pathInfoStart, pathInfoEnd));
                    break;
                }
                if (servlet != null) {
                    handlerContext.setServletInfo(servletInfo);
                    break;
                }
            }
        }

        doNext(handlerContext);
    }

    static class CacheServlet {
        private final ServletInfo servletInfo;
        private final int servletPathStart;
        private final int servletPathEnd;
        private final int pathInfoStart;
        private final int pathInfoEnd;

        public CacheServlet(ServletInfo servletInfo, int servletPathStart, int servletPathEnd, int pathInfoStart, int pathInfoEnd) {
            this.servletInfo = servletInfo;
            this.servletPathStart = servletPathStart;
            this.servletPathEnd = servletPathEnd;
            this.pathInfoStart = pathInfoStart;
            this.pathInfoEnd = pathInfoEnd;
        }
    }
}
