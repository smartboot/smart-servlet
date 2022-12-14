/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ConfigurableSmartWebServerFactory.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.springboot.starter;

import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import java.io.File;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/12
 */
public class ConfigurableSmartWebServerFactory extends AbstractServletWebServerFactory implements ResourceLoaderAware {
    private ResourceLoader resourceLoader;

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        File root = getValidDocumentRoot();
        File docBase = (root != null) ? root : createTempDir("smart-servlet");
        System.out.println(docBase.getAbsoluteFile());
        SmartContainerInitializer initializer = new SmartContainerInitializer(initializers);
        ServletContextRuntime servletRuntime = new ServletContextRuntime(getContextPath());
        DeploymentInfo deployment = servletRuntime.getDeploymentInfo();
        deployment.setClassLoader(getServletClassLoader());
        deployment.setDisplayName(getDisplayName());
        deployment.addServletContainerInitializer(initializer);
        return new SmartServletServer(servletRuntime, getPort());
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    private ClassLoader getServletClassLoader() {
        if (this.resourceLoader != null) {
            return this.resourceLoader.getClassLoader();
        }
        return getClass().getClassLoader();
    }
}
