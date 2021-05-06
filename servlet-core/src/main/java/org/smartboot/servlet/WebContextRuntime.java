/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WebContextRuntime.java
 * Date: 2020-12-31
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.WebAppInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
class WebContextRuntime {
    private final String location;
    private final String contextPath;
    private final ClassLoader parentClassLoader;


    public WebContextRuntime(String location, String contextPath, ClassLoader parentClassLoader) throws Exception {
        this.location = location;
        this.contextPath = contextPath;
        this.parentClassLoader = parentClassLoader;
    }

    public ApplicationRuntime getServletRuntime() throws Exception {
        FileInputStream webXmlInputStream = null;
        ApplicationRuntime servletRuntime;
        try {
            //load web.xml file
            WebXmlParseEngine webXmlParse = new WebXmlParseEngine();
            File contextFile = new File(location);

            webXmlInputStream = new FileInputStream(new File(contextFile, "WEB-INF" + File.separatorChar + "web.xml"));
            WebAppInfo webAppInfo = webXmlParse.load(webXmlInputStream);

            //new runtime object
            servletRuntime = new ApplicationRuntime(getClassLoader(location), StringUtils.isBlank(contextPath) ? "/" + contextFile.getName() : contextPath);
            DeploymentInfo deploymentInfo = servletRuntime.getDeploymentInfo();
            //set session timeout
            deploymentInfo.setSessionTimeout(webAppInfo.getSessionTimeout());
            //register Servlet into deploymentInfo
            webAppInfo.getServlets().values().forEach(deploymentInfo::addServlet);

            //register Filter
            webAppInfo.getFilters().values().forEach(deploymentInfo::addFilter);
            //register servletContext into deploymentInfo
            webAppInfo.getContextParams().forEach(deploymentInfo::addInitParameter);

            //register ServletContextListener into deploymentInfo
            webAppInfo.getListeners().forEach(deploymentInfo::addEventListener);

            //register filterMapping into deploymentInfo
            webAppInfo.getFilterMappings().forEach(deploymentInfo::addFilterMapping);

            deploymentInfo.setContextUrl(contextFile.toURI().toURL());

            //默认页面
            //《Servlet3.1规范中文版》10.10 欢迎文件
            // 欢迎文件列表是一个没有尾随或前导/的局部 URL 有序列表
//            for (String welcomeFile : webAppInfo.getWelcomeFileList()) {
//                if (welcomeFile.startsWith("/")) {
//                    throw new IllegalArgumentException("invalid welcome file " + welcomeFile + " is startWith /");
//                } else if (welcomeFile.endsWith("/")) {
//                    throw new IllegalArgumentException("invalid welcome file " + welcomeFile + " is endWith /");
//                }
//            }
            if (webAppInfo.getWelcomeFileList() == null || webAppInfo.getWelcomeFileList().size() == 0) {
                deploymentInfo.setWelcomeFiles(Arrays.asList("index.html", "index.jsp"));
            } else {
                //实际使用中存在"/"开头的情况，将其矫正过来
                List<String> welcomeFiles = new ArrayList<>(webAppInfo.getWelcomeFileList().size());
                webAppInfo.getWelcomeFileList().forEach(file -> {
                    if (file.startsWith("/")) {
                        welcomeFiles.add(file.substring(1));
                    } else {
                        welcomeFiles.add(file);
                    }
                });
                deploymentInfo.setWelcomeFiles(welcomeFiles);
            }

            //默认Servlet
            deploymentInfo.setDefaultServlet(new DefaultServlet(deploymentInfo.getWelcomeFiles()));

        } finally {
            if (webXmlInputStream != null) {
                try {
                    webXmlInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return servletRuntime;
    }

    private ClassLoader getClassLoader(String location) throws MalformedURLException {
        List<URL> list = new ArrayList<>();
        File libDir = new File(location, "WEB-INF" + File.separator + "lib/");
        File[] files = libDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                list.add(file.toURI().toURL());
            }
        }

        File classDir = new File(location, "WEB-INF" + File.separator + "classes/");
        list.add(classDir.toURI().toURL());
        URL[] urls = new URL[list.size()];
        list.toArray(urls);
        return new URLClassLoader(urls, parentClassLoader);
    }
}
