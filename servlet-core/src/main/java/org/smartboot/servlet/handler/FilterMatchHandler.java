/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: FilterMatchHandler.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.handler;

import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.HandlerContext;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.FilterMappingInfo;
import org.smartboot.servlet.enums.FilterMappingType;
import org.smartboot.servlet.impl.FilterChainImpl;
import org.smartboot.servlet.util.ServletPathMatcher;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
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

    @Override
    public void handleRequest(HandlerContext handlerContext) throws Exception {
        HttpServletRequest request = handlerContext.getRequest();
        String contextPath = handlerContext.getServletContext().getContextPath();
        //匹配Filter
        List<Filter> filters = new ArrayList<>();
        List<FilterMappingInfo> filterMappings = handlerContext.getServletContext().getDeploymentInfo().getFilterMappings();
        Map<String, FilterInfo> allFilters = handlerContext.getServletContext().getDeploymentInfo().getFilters();
        filterMappings.forEach(filterInfo -> {
            if (filterInfo.getMappingType() == FilterMappingType.URL) {
                if (PATH_MATCHER.matches(contextPath + filterInfo.getMapping(), request.getRequestURI())) {
                    filters.add(allFilters.get(filterInfo.getFilterName()).getFilter());
                }
            } else if (filterInfo.getMappingType() == FilterMappingType.SERVLET) {
                if (StringUtils.equals(filterInfo.getMapping(), handlerContext.getServlet().getServletConfig().getServletName())) {
                    filters.add(allFilters.get(filterInfo.getFilterName()).getFilter());
                }
            } else {
                throw new UnsupportedOperationException();
            }
        });

        //Filter执行完毕后执行Servlet
        if (!filters.isEmpty()) {
            FilterChain filterChain = new FilterChainImpl(filters, () -> FilterMatchHandler.this.doNext(handlerContext));
            filterChain.doFilter(handlerContext.getRequest(), handlerContext.getResponse());
        } else {
            doNext(handlerContext);
        }
    }
}
