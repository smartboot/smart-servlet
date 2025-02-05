/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.handler;

import jakarta.servlet.DispatcherType;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import tech.smartboot.feat.core.common.utils.StringUtils;
import tech.smartboot.servlet.conf.FilterInfo;
import tech.smartboot.servlet.conf.FilterMappingInfo;
import tech.smartboot.servlet.util.PathMatcherUtil;

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
    private final Map<Servlet, Map<String, List<FilterInfo>>> requestDispatcherFilterChainMap = new HashMap<>();

    private final Map<Servlet, Map<String, List<FilterInfo>>> forwardDispatcherFilterChainMap = new HashMap<>();

    @Override
    public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
        //查找缓存中的 Filter 集合
        List<FilterInfo> filters = filterCacheFilterList(handlerContext);
        if (filters == null) {
            //匹配 Filter 集合
            filters = matchFilters(handlerContext);
            //缓存 Filter 集合
            cacheFilterList(handlerContext, filters);
        }

        FilterChainImpl filterChain = new FilterChainImpl();
        filterChain.init(filters, handlerContext);
        try {
            filterChain.doFilter(handlerContext.getRequest(), handlerContext.getResponse());
        } finally {
            filterChain.init(null, null);
        }
    }

    /**
     * 匹配Filter
     */
    private List<FilterInfo> matchFilters(HandlerContext handlerContext) {
        String contextPath = handlerContext.getServletContext().getContextPath();
        HttpServletRequest request = (HttpServletRequest) handlerContext.getRequest();
        List<FilterInfo> filters = new ArrayList<>();
        List<FilterMappingInfo> mappings = handlerContext.getServletContext().getDeploymentInfo().getFilterMappings();
        mappings.stream().filter(filterMappingInfo -> filterMappingInfo.contains(request.getDispatcherType())).forEach(mappingInfo -> {
            if (mappingInfo.isServletMappingType()) {
                if (handlerContext.getServletInfo() != null && StringUtils.equals(mappingInfo.getServletNameMapping(), handlerContext.getServletInfo().getServlet().getServletConfig().getServletName())) {
                    filters.add(handlerContext.getServletContext().getDeploymentInfo().getFilters().get(mappingInfo.getFilterName()));
                }
            } else {
                String requestURI = request.getRequestURI();
                if (request.getDispatcherType() == DispatcherType.INCLUDE) {
                    requestURI = (String) request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI);
                }
                if (PathMatcherUtil.matches(requestURI, contextPath.length(), mappingInfo)) {
                    filters.add(handlerContext.getServletContext().getDeploymentInfo().getFilters().get(mappingInfo.getFilterName()));
                }
            }
        });
        filters.sort((o1, o2) -> o1.getOrder() - o2.getOrder());
        return filters;
    }

    private List<FilterInfo> filterCacheFilterList(HandlerContext handlerContext) {
        Map<Servlet, Map<String, List<FilterInfo>>> map;
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
        Servlet servlet = handlerContext.getServletInfo() == null ? NONE : handlerContext.getServletInfo().getServlet();
        Map<String, List<FilterInfo>> urlMap = map.get(servlet);
        return urlMap == null ? null : urlMap.get(handlerContext.getOriginalRequest().getRequestURI());
    }

    private void cacheFilterList(HandlerContext handlerContext, List<FilterInfo> filters) {
        Map<Servlet, Map<String, List<FilterInfo>>> map;
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
        Servlet servlet = handlerContext.getServletInfo() == null ? NONE : handlerContext.getServletInfo().getServlet();
        Map<String, List<FilterInfo>> urlMap = map.get(servlet);
        if (urlMap == null) {
            urlMap = new ConcurrentHashMap<>();
            map.put(servlet, urlMap);
        }
        urlMap.put(handlerContext.getOriginalRequest().getRequestURI(), filters);
    }

    class FilterChainImpl implements FilterChain {
        private List<FilterInfo> filters;
        private int location = 0;
        private HandlerContext handlerContext;

        public void init(List<FilterInfo> filters, HandlerContext handlerContext) {
            this.filters = filters;
            this.handlerContext = handlerContext;
            location = 0;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            int index = location++;
            if (index < filters.size()) {
                FilterInfo filterInfo = filters.get(index);
                if (!filterInfo.isAsyncSupported()) {
                    handlerContext.getOriginalRequest().setAsyncSupported(false);
                }
                filterInfo.getFilter().doFilter(request, response, this);
                return;
            }

            handlerContext.setRequest(request);
            handlerContext.setResponse(response);
            FilterMatchHandler.this.doNext(handlerContext);
        }
    }

}
