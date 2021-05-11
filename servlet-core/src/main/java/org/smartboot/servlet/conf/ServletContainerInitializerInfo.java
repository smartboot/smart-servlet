/*
 * Copyright (c) 2017-2021, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletContainerInitializerInfo.java
 * Date: 2021-05-10
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import javax.servlet.ServletContainerInitializer;
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
