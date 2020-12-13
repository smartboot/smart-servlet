/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: FilterMatchHandler.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.enums.FilterMappingType;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.impl.FilterChainImpl;
import org.smartboot.servlet.util.ServletPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 匹配并执行符合当前请求的Filter
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class FilterMatchHandler extends Handler {
    private static final ServletPathMatcher PATH_MATCHER = new ServletPathMatcher();

    /**
     * 缓存Servlet关联的过滤器
     */
//    private final Map<DispatcherType, Map<Servlet, List<Filter>>> dispatcherFilterChainMap = new HashMap<>();

//    {
//        for (DispatcherType value : DispatcherType.values()) {
//            dispatcherFilterChainMap.put(value, new ConcurrentHashMap<>());
//        }
//    }
    @Override
    public void handleRequest(HandlerContext handlerContext) {
        //begin 暂时不缓存,实时匹配
//        Map<Servlet, List<Filter>> filterChainMap = dispatcherFilterChainMap.get(handlerContext.getRequest().getDispatcherType());
//        List<Filter> cacheFilters = filterChainMap.get(handlerContext.getServlet());
//        if (cacheFilters != null) {
//            FilterChain filterChain = new FilterChainImpl(cacheFilters, () -> FilterMatchHandler.this.doNext(handlerContext));
//            try {
//                filterChain.doFilter(handlerContext.getRequest(), handlerContext.getResponse());
//            } catch (IOException | ServletException e) {
//                throw new WrappedRuntimeException(e);
//            }
//            return;
//        }
        //end 暂时不缓存,实时匹配

        HttpServletRequest request = handlerContext.getRequest();
        String contextPath = handlerContext.getServletContext().getContextPath();
        //匹配Filter
        List<Filter> filters = new ArrayList<>();
        List<FilterMappingInfo> filterMappings = handlerContext.getServletContext().getDeploymentInfo().getFilterMappings();
        Map<String, FilterInfo> allFilters = handlerContext.getServletContext().getDeploymentInfo().getFilters();
        filterMappings.stream()
                .filter(filterMappingInfo -> filterMappingInfo.getDispatcher().contains(request.getDispatcherType()))
                .forEach(filterInfo -> {
                    if (filterInfo.getMappingType() == FilterMappingType.URL) {
                        if (PATH_MATCHER.matches(request.getRequestURI(), contextPath.length(), filterInfo.getServletUrlMapping()) > -1) {
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

        //begin 暂时不缓存,实时匹配
        //cache for performance
//        filterChainMap.put(handlerContext.getServlet(), filters);
//        handleRequest(handlerContext);
        //end 暂时不缓存,实时匹配

        FilterChain filterChain = new FilterChainImpl(filters, () -> FilterMatchHandler.this.doNext(handlerContext));
        try {
            filterChain.doFilter(handlerContext.getRequest(), handlerContext.getResponse());
        } catch (IOException | ServletException e) {
            throw new WrappedRuntimeException(e);
        }
    }

}
