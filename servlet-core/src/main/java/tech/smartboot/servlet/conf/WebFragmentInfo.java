/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.conf;

import java.util.Optional;

public class WebFragmentInfo extends WebAppInfo {
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void mergeTo(WebAppInfo webAppInfo) {
        getServlets().values().forEach(servletInfo -> {
            ServletInfo mainServletInfo = webAppInfo.getServlets().get(servletInfo.getServletName());
            if (mainServletInfo == null) {
                webAppInfo.addServlet(servletInfo);
            } else {
                servletInfo.getInitParams().forEach((key, val) -> {
                    if (!mainServletInfo.getInitParams().containsKey(key)) {
                        mainServletInfo.getInitParams().put(key, val);
                    }
                });
            }
            //具有相同<servlet-name>的<servlet-mapping>元素可以添加到多个 web-fragment。在 web.xml 中
            //指定的<servlet-mapping>覆盖在 web-fragment 中指定的同名的<servlet-name>的<servlet-mapping>
            if (webAppInfo.getServletMappings().stream().noneMatch(mapping -> mapping.getServletName().equals(servletInfo.getServletName()))) {
                webAppInfo.getServletMappings().addAll(getServletMappings().stream().filter(mapping -> mapping.getServletName().equals(servletInfo.getServletName())).toList());
            }

        });
        getFilters().stream().filter(filterInfo -> webAppInfo.getFilters().stream().noneMatch(mainFilter -> filterInfo.getFilterName().equals(mainFilter.getFilterName()))).forEach(webAppInfo::addFilter);
        getFilters().forEach(filterInfo -> {
            Optional<FilterInfo> optional = webAppInfo.getFilters().stream().filter(mainFilter -> filterInfo.getFilterName().equals(mainFilter.getFilterName())).findFirst();
            if (optional.isPresent()) {
                filterInfo.getInitParams().forEach((key, val) -> {
                    if (!optional.get().getInitParams().containsKey(key)) {
                        optional.get().getInitParams().put(key, val);
                    }
                });
            } else {
                webAppInfo.addFilter(filterInfo);
            }
        });
        webAppInfo.getFilterMappingInfos().addAll(getFilterMappingInfos());


        webAppInfo.getListeners().addAll(getListeners());
        webAppInfo.getWelcomeFileList().addAll(getWelcomeFileList());
        webAppInfo.getErrorPages().addAll(getErrorPages());
        getMimeMappings().forEach((key, val) -> {
            if (!webAppInfo.getMimeMappings().containsKey(val)) {
                webAppInfo.getMimeMappings().put(key, val);
            }
        });
        getContextParams().forEach((key, val) -> {
            if (!webAppInfo.getContextParams().containsKey(val)) {
                webAppInfo.getContextParams().put(key, val);
            }
        });
    }
}
