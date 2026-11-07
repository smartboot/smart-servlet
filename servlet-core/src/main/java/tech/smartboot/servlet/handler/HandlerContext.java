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

package tech.smartboot.servlet.handler;

import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import tech.smartboot.servlet.SmartHttpServletRequest;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.impl.ServletContextImpl;

/**
 * 请求处理上下文对象
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class HandlerContext {
    /**
     * 请求
     */
    private final SmartHttpServletRequest originalRequest;
    /**
     * 响应
     */
    private ServletResponse response;
    /**
     * 匹配的Servlet上下文
     */
    private final ServletContextImpl servletContext;
    private final boolean namedDispatcher;

    /**
     * 匹配的Servlet处理器
     */
    private ServletInfo servletInfo;

    private ServletRequest request;

    public HandlerContext(SmartHttpServletRequest originalRequest, ServletResponse response, ServletContextImpl servletContext, boolean namedDispatcher) {
        this.originalRequest = originalRequest;
        this.response = response;
        this.servletContext = servletContext;
        this.namedDispatcher = namedDispatcher;
        this.request = originalRequest;
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }


    public SmartHttpServletRequest getOriginalRequest() {
        return originalRequest;
    }

    public ServletResponse getResponse() {
        return response;
    }

    public void setResponse(ServletResponse response) {
        this.response = response;
    }

    public ServletRequest getRequest() {
        return request;
    }

    public void setRequest(ServletRequest request) {
        this.request = request;
    }

    public ServletInfo getServletInfo() {
        return servletInfo;
    }

    public void setServletInfo(ServletInfo servletInfo) {
        this.servletInfo = servletInfo;
        this.originalRequest.setServletInfo(servletInfo);
    }

    public boolean isNamedDispatcher() {
        return namedDispatcher;
    }
}
