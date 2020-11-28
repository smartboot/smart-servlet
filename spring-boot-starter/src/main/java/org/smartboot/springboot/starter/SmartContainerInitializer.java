/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: SmartContainerInitializer.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
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
