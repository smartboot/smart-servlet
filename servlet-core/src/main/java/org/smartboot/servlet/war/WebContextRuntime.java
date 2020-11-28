/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebContextRuntime.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.war;

import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.DefaultServlet;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.WebAppInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class WebContextRuntime {
    private final String location;
    private final String contextPath;
    private volatile boolean started = false;
    private ContainerRuntime servletRuntime;

    public WebContextRuntime(String location, String contextPath) throws Exception {
        this.location = location;
        this.contextPath = contextPath;
        start();
    }

    public WebContextRuntime(String location) throws Exception {
        this(location, null);
    }

    public ContainerRuntime getServletRuntime() {
        return servletRuntime;
    }

    private void start() throws Exception {
        FileInputStream webXmlInputStream = null;
        try {
            //load web.xml file
            WebXmlParseEngine webXmlParse = new WebXmlParseEngine();
            File contextFile = new File(location);

            webXmlInputStream = new FileInputStream(new File(contextFile, "WEB-INF" + File.separatorChar + "web.xml"));
            WebAppInfo webAppInfo = webXmlParse.load(webXmlInputStream);

            //new runtime object
            this.servletRuntime = new ContainerRuntime();
            DeploymentInfo deploymentInfo = servletRuntime.getDeploymentInfo();
            //set session timeout
            deploymentInfo.setSessionTimeout(webAppInfo.getSessionTimeout());
            //register Servlet into deploymentInfo
//            ServletInfo servletInfo = new ServletInfo();
//            servletInfo.addMapping("*.jsp");
//            servletInfo.setServletClass("org.apache.jasper.servlet.JspServlet");
//            deploymentInfo.addServlet(servletInfo);
            webAppInfo.getServlets().values().forEach(deploymentInfo::addServlet);

            //register Filter
            webAppInfo.getFilters().values().forEach(deploymentInfo::addFilter);
            //register servletContext into deploymentInfo
            webAppInfo.getContextParams().forEach(deploymentInfo::addInitParameter);

            //register ServletContextListener into deploymentInfo
            webAppInfo.getListeners().forEach(value -> deploymentInfo.addEventListener(value));

            //register filterMapping into deploymentInfo
            webAppInfo.getFilterMappings().forEach(filterMappingInfo -> deploymentInfo.addFilterMapping(filterMappingInfo));

            if (StringUtils.isBlank(contextPath)) {
                deploymentInfo.setContextPath("/" + contextFile.getName());
            } else {
                deploymentInfo.setContextPath(contextPath);
            }

            System.out.println(contextFile.toURI().toURL());
            deploymentInfo.setContextUrl(contextFile.toURI().toURL());

            //默认页面
            for (String welcomeFile : webAppInfo.getWelcomeFileList()) {
                File file = new File(contextFile, welcomeFile);
                if (file.isFile()) {
                    deploymentInfo.setWelcomeFile(welcomeFile);
                    break;
                }
            }
            //默认Servlet
            deploymentInfo.setDefaultServlet(new DefaultServlet());


            //自定义ClassLoader
            ContainerClassLoader webContextClassLoader = new ContainerClassLoader(location);
            ClassLoader webClassLoader = webContextClassLoader.getClassLoader();
            deploymentInfo.setClassLoader(webClassLoader);
        } finally {
            if (webXmlInputStream != null) {
                try {
                    webXmlInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

}
