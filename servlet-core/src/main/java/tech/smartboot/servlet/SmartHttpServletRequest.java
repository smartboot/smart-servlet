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

package tech.smartboot.servlet;

import jakarta.servlet.http.HttpServletRequest;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.plugins.security.LoginAccount;

import javax.net.ssl.SSLEngine;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/22
 */
public interface SmartHttpServletRequest extends HttpServletRequest {
    void setRequestUri(String requestUri);

    void setServletInfo(ServletInfo servletInfo);

    ServletInfo getServletInfo();

    void setServletMappingInfo(ServletMappingInfo servletMappingInfo);

    ServletMappingInfo getServletMappingInfo();

    void setAsyncSupported(boolean supported);

    void setLoginAccount(LoginAccount loginAccount);

    SSLEngine getSslEngine();
}
