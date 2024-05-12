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

import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;

import javax.websocket.server.ServerContainer;
import java.io.File;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/12
 */
public class ConfigurableSmartWebServerFactory extends AbstractServletWebServerFactory implements ResourceLoaderAware {
    private ResourceLoader resourceLoader;

    @Override
    public WebServer getWebServer(ServletContextInitializer... initializers) {
        System.setProperty("smart-servlet-spring-boot-starter", "true");
        File root = getValidDocumentRoot();
        File docBase = (root != null) ? root : createTempDir("smart-servlet");
        System.out.println(docBase.getAbsoluteFile());
        SmartContainerInitializer initializer = new SmartContainerInitializer(initializers);
        ServletContextRuntime servletRuntime = new ServletContextRuntime(null, getServletClassLoader(), getContextPath());
        servletRuntime.getServletContext().setAttribute(ServerContainer.class.getName(), servletRuntime.getWebsocketProvider().getWebSocketServerContainer());
        DeploymentInfo deployment = servletRuntime.getDeploymentInfo();
        deployment.setDisplayName(getDisplayName());
        deployment.addServletContainerInitializer(initializer);
        try {
            return new SmartServletServer(servletRuntime, getPort());
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
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
