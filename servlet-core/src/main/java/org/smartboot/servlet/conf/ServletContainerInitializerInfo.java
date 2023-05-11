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
