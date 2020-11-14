/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: FilterChainImpl.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.handler.FilterChainCallback;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class FilterChainImpl implements FilterChain {
    final List<Filter> filters;
    int location = 0;
    private FilterChainCallback function;

    public FilterChainImpl(List<Filter> filters, FilterChainCallback function) {
        this.filters = filters;
        this.function = function;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        int index = location++;
        if (index < filters.size()) {
            filters.get(index).doFilter(request, response, this);
        } else {
            try {
                function.callback();
            } catch (Exception e) {
                throw new ServletException(e);
            }
        }
    }
}
