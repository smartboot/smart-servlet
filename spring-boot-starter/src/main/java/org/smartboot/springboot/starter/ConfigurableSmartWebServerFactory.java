/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ConfigurableSmartWebServerFactory.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.springboot.starter;

import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.net.MalformedURLException;

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
        ContainerRuntime servletRuntime = new ContainerRuntime();
        DeploymentInfo deployment = servletRuntime.getServletContext().getDeploymentInfo();
        deployment.setClassLoader(getServletClassLoader());
        deployment.setContextPath(getContextPath());
        deployment.setDisplayName(getDisplayName());
        try {
            deployment.setContextUrl(docBase.toURL());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
//        if (isRegisterDefaultServlet()) {
//            ServletInfo servletInfo = new ServletInfo();
//            servletInfo.setServletName("default");
//            servletInfo.setServletClass(DefaultServlet.class.getName());
//            servletInfo.addMapping("/*");
//            deployment.addServlet(servletInfo);
//        }
        deployment.addServletContainerInitializer(initializer);
        return new SmartServletServer(servletRuntime);
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
