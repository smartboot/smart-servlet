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
        });
        getFilters().values().stream().filter(filterInfo -> !webAppInfo.getFilters().containsKey(filterInfo.getFilterName())).forEach(webAppInfo::addFilter);
        webAppInfo.getListeners().addAll(getListeners());
    }
}
