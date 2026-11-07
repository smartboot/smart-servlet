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

import jakarta.servlet.ServletContainerInitializer;

import java.util.Set;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/5/10
 */
public class ServletContainerInitializerInfo {
    private final ServletContainerInitializer servletContainerInitializer;
    private final Set<Class<?>> handlesTypes;

    public ServletContainerInitializerInfo(ServletContainerInitializer servletContainerInitializer, Set<Class<?>> handlesTypes) {
        this.servletContainerInitializer = servletContainerInitializer;
        this.handlesTypes = handlesTypes;
    }

    public ServletContainerInitializer getServletContainerInitializer() {
        return servletContainerInitializer;
    }

    public Set<Class<?>> getHandlesTypes() {
        return handlesTypes;
    }
}
