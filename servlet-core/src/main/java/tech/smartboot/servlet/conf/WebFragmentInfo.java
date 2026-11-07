/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
            if (webAppInfo.getServletMappings().get(servletInfo.getServletName()) == null && getServletMappings().get(servletInfo.getServletName()) != null) {
                webAppInfo.getServletMappings().put(servletInfo.getServletName(), getServletMappings().get(servletInfo.getServletName()));
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
