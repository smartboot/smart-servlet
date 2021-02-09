/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: FilterMatchHandler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.enums.FilterMappingType;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.util.PathMatcherUtil;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 匹配并执行符合当前请求的Filter
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class FilterMatchHandler extends Handler {
    private static final Servlet NONE = new HttpServlet() {
    };

    /**
     * 缓存Servlet关联的过滤器
     */
    private final Map<Servlet, Map<String, List<Filter>>> requestDispatcherFilterChainMap = new HashMap<>();

    private final Map<Servlet, Map<String, List<Filter>>> forwardDispatcherFilterChainMap = new HashMap<>();
    private final ThreadLocal<FilterChainImpl> filterChainThreadLocal = ThreadLocal.withInitial(FilterChainImpl::new);

    @Override
    public void handleRequest(HandlerContext handlerContext) {
        //查找缓存中的 Filter 集合
        List<Filter> filters = filterCacheFilterList(handlerContext);
        if (filters == null) {
            //匹配 Filter 集合
            filters = matchFilters(handlerContext);
            //缓存 Filter 集合
            cacheFilterList(handlerContext, filters);
        }

        FilterChainImpl filterChain = filterChainThreadLocal.get();
        filterChain.init(filters, handlerContext);
        try {
            filterChain.doFilter(handlerContext.getRequest(), handlerContext.getResponse());
        } catch (IOException | ServletException e) {
            throw new WrappedRuntimeException(e);
        } finally {
            filterChain.init(null, null);
        }
    }

    /**
     * 匹配Filter
     */
    private List<Filter> matchFilters(HandlerContext handlerContext) {
        String contextPath = handlerContext.getServletContext().getContextPath();
        HttpServletRequest request = handlerContext.getRequest();
        List<Filter> filters = new ArrayList<>();
        List<FilterMappingInfo> filterMappings = handlerContext.getServletContext().getDeploymentInfo().getFilterMappings();
        Map<String, FilterInfo> allFilters = handlerContext.getServletContext().getDeploymentInfo().getFilters();
        filterMappings.stream()
                .filter(filterMappingInfo -> filterMappingInfo.getDispatcher().contains(request.getDispatcherType()))
                .forEach(filterInfo -> {
                    if (filterInfo.getMappingType() == FilterMappingType.URL) {
                        if (PathMatcherUtil.matches(request.getRequestURI(), contextPath.length(), filterInfo.getServletUrlMapping()) > -1) {
                            filters.add(allFilters.get(filterInfo.getFilterName()).getFilter());
                        }
                    } else if (filterInfo.getMappingType() == FilterMappingType.SERVLET) {
                        if (handlerContext.getServlet() != null && StringUtils.equals(filterInfo.getServletNameMapping(), handlerContext.getServlet().getServletConfig().getServletName())) {
                            filters.add(allFilters.get(filterInfo.getFilterName()).getFilter());
                        }
                    } else {
                        throw new IllegalStateException();
                    }
                });
        return filters;
    }

    private List<Filter> filterCacheFilterList(HandlerContext handlerContext) {
        Map<Servlet, Map<String, List<Filter>>> map;
        switch (handlerContext.getRequest().getDispatcherType()) {
            case REQUEST:
                map = requestDispatcherFilterChainMap;
                break;
            case FORWARD:
                map = forwardDispatcherFilterChainMap;
                break;
            default:
                map = Collections.emptyMap();
        }
        if (map.isEmpty()) {
            return null;
        }
        Servlet servlet = handlerContext.getServlet() == null ? NONE : handlerContext.getServlet();
        Map<String, List<Filter>> urlMap = map.get(servlet);
        return urlMap == null ? null : urlMap.get(handlerContext.getRequest().getRequestURI());
    }

    private void cacheFilterList(HandlerContext handlerContext, List<Filter> filters) {
        Map<Servlet, Map<String, List<Filter>>> map;
        switch (handlerContext.getRequest().getDispatcherType()) {
            case REQUEST:
                map = requestDispatcherFilterChainMap;
                break;
            case FORWARD:
                map = forwardDispatcherFilterChainMap;
                break;
            default:
                return;
        }
        Servlet servlet = handlerContext.getServlet() == null ? NONE : handlerContext.getServlet();
        Map<String, List<Filter>> urlMap = map.get(servlet);
        if (urlMap == null) {
            urlMap = new ConcurrentHashMap<>();
            map.put(servlet, urlMap);
        }
        urlMap.put(handlerContext.getRequest().getRequestURI(), filters);
    }

    class FilterChainImpl implements FilterChain {
        private List<Filter> filters;
        private int location = 0;
        private HandlerContext handlerContext;

        public void init(List<Filter> filters, HandlerContext handlerContext) {
            this.filters = filters;
            this.handlerContext = handlerContext;
            location = 0;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            int index = location++;
            if (index < filters.size()) {
                filters.get(index).doFilter(request, response, this);
            } else {
                FilterMatchHandler.this.doNext(handlerContext);
            }
        }
    }

}
