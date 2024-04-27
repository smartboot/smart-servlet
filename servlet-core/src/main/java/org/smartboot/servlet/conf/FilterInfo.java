/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.conf;

import javax.servlet.Filter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class FilterInfo {

    private final Map<String, String> initParams = new HashMap<>();
    private String filterClass;
    private String filterName;
    private Filter filter;
    private boolean dynamic;
    private boolean asyncSupported;

    public Filter getFilter() {
        return filter;
    }

    public void setFilter(Filter filter) {
        this.filter = filter;
    }

    public String getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(String filterClass) {
        this.filterClass = filterClass;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public FilterInfo addInitParam(final String name, final String value) {
        initParams.put(name, value);
        return this;
    }

    public Map<String, String> getInitParams() {
        return Collections.unmodifiableMap(initParams);
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    @Override
    public String toString() {
        return "FilterInfo{" +
                "filterClass=" + filterClass +
                ", name='" + filterName + '\'' +
                '}';
    }
}
