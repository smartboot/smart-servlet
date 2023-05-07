/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.springboot.starter;

import org.springframework.boot.web.servlet.ServletContextInitializer;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/13
 */
public class SmartContainerInitializer implements ServletContainerInitializer {

    private final ServletContextInitializer[] initializers;

    public SmartContainerInitializer(ServletContextInitializer[] initializers) {
        this.initializers = initializers;
    }

    @Override
    public void onStartup(Set<Class<?>> c, ServletContext ctx) throws ServletException {
        if (initializers == null) {
            return;
        }
        for (ServletContextInitializer initializer : initializers) {
            initializer.onStartup(ctx);
        }
    }
}
