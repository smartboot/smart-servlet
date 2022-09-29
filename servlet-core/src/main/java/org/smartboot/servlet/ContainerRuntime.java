/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletContainerRuntime.java
 * Date: 2020-12-31
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.http.server.impl.Request;
import org.smartboot.http.server.impl.WebSocketRequestImpl;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.WebAppInfo;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.handler.FilterMatchHandler;
import org.smartboot.servlet.handler.HandlerPipeline;
import org.smartboot.servlet.handler.ServletMatchHandler;
import org.smartboot.servlet.handler.ServletRequestListenerHandler;
import org.smartboot.servlet.handler.ServletServiceHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.Plugin;

import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.ServiceLoader;

/**
 * Servlet容器运行环境
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2020/12/31
 */
public class ContainerRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerRuntime.class);
    /**
     * http://patorjk.com/software/taag/
     * Font Name: Puffy
     */
    private static final String BANNER = "                               _                                 _           _   \n" +
            "                              ( )_                              (_ )        ( )_ \n" +
            "  ___   ___ ___     _ _  _ __ | ,_)     ___    __   _ __  _   _  | |    __  | ,_)\n" +
            "/',__)/' _ ` _ `\\ /'_` )( '__)| |     /',__) /'__`\\( '__)( ) ( ) | |  /'__`\\| |  \n" +
            "\\__, \\| ( ) ( ) |( (_| || |   | |_    \\__, \\(  ___/| |   | \\_/ | | | (  ___/| |_ \n" +
            "(____/(_) (_) (_)`\\__,_)(_)   `\\__)   (____/`\\____)(_)   `\\___/'(___)`\\____)`\\__)";
    private static final String VERSION = "0.1.7-SNAPSHOT";
    /**
     * 注册在当前 Servlet 容器中的运行环境
     */
    private final List<ServletContextRuntime> runtimes = new ArrayList<>();
    /**
     * 注册至当前容器中的插件集
     */
    private final List<Plugin> plugins = new ArrayList<>();
    /**
     * Servlet容器运行环境是否完成启动
     */
    private volatile boolean started = false;

    public void start() {
        if (started) {
            return;
        }
        started = true;
        HandlerPipeline pipeline = new HandlerPipeline();
        pipeline.next(new ServletRequestListenerHandler())
                .next(new ServletMatchHandler())
                .next(new FilterMatchHandler())
                .next(new ServletServiceHandler());
        //扫描插件
        loadAndInstallPlugins();

        //必须有一个 contextPath 为 "/" 的环境
        initRootContainer();

        //启动运行环境
        runtimes.forEach(runtime -> {
            runtime.getServletContext().setPipeline(pipeline);
            runtime.setPlugins(plugins);
            ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                //有些场景下ServletContainerInitializer初始化依赖当前容器的类加载器
                Thread.currentThread().setContextClassLoader(runtime.getDeploymentInfo().getClassLoader());
                runtime.start();
            } catch (Exception e) {
                e.printStackTrace();
                runtime.getPlugins().forEach(plugin -> plugin.whenContainerStartError(runtime, e));
            } finally {
                Thread.currentThread().setContextClassLoader(currentClassLoader);
            }
        });
        //按contextPath长度倒序,防止被"/"优先匹配
        runtimes.sort((o1, o2) -> o2.getContextPath().length() - o1.getContextPath().length());

        System.out.println(BANNER + "\r\n :: smart-servlet :: (" + VERSION + ")");

    }

    /**
     * 加载并安装插件
     */
    private void loadAndInstallPlugins() {
        for (Plugin plugin : ServiceLoader.load(Plugin.class, ContainerRuntime.class.getClassLoader())) {
            LOGGER.info("load plugin: " + plugin.pluginName());
            plugins.add(plugin);
        }
        //安装插件
        plugins.forEach(plugin -> {
            LOGGER.info("install plugin: " + plugin.pluginName());
            plugin.install(this);
        });
    }

    /**
     * 注册 Servlet 子容器
     *
     * @param runtime Servlet 子容器
     */
    public void addRuntime(ServletContextRuntime runtime) {
        if (runtimes.stream().anyMatch(containerRuntime -> containerRuntime.getContextPath().equals(runtime.getContextPath()))) {
            throw new IllegalArgumentException("contextPath: " + runtime.getContextPath() + " is already exists!");
        }
        runtimes.add(runtime);
    }

    /**
     * 注册 Servlet 子容器
     *
     * @param location    本地目录
     * @param contextPath 注册的 Context 路径
     * @throws Exception
     */
    public void addRuntime(String location, String contextPath) throws Exception {
        addRuntime(location, contextPath, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 注册 Servlet 子容器
     *
     * @param location          本地目录
     * @param contextPath       注册的 Context 路径
     * @param parentClassLoader 父类加载
     * @throws Exception
     */
    public void addRuntime(String location, String contextPath, ClassLoader parentClassLoader) throws Exception {
        addRuntime(getServletRuntime(location, contextPath, parentClassLoader));
    }

    public void onHeaderComplete(Request request) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ServletContextRuntime runtime = matchRuntime(request.getRequestURI());
            if (!runtime.isStarted()) {
                throw new IllegalStateException("container is not started");
            }

            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());
            WebSocketRequestImpl webSocketRequest = request.newWebsocketRequest();
            runtime.getWebsocketProvider().onHandShark(runtime, webSocketRequest, webSocketRequest.getResponse());
        } catch (WrappedRuntimeException e) {
            e.getThrowable().printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    public void doHandle(WebSocketRequest request, WebSocketResponse response) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ServletContextRuntime runtime = matchRuntime(request.getRequestURI());
            if (!runtime.isStarted()) {
                throw new IllegalStateException("container is not started");
            }
            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());
            runtime.getWebsocketProvider().doHandle(runtime, request, response);
        } catch (WrappedRuntimeException e) {
            e.getThrowable().printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

    }

    /**
     * 执行 Http 请求
     *
     * @param request
     * @param response
     */
    public void doHandle(HttpRequest request, HttpResponse response) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ServletContextRuntime runtime = matchRuntime(request.getRequestURI());
            if (!runtime.isStarted()) {
                throw new IllegalStateException("container is not started");
            }
            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());

            //封装上下文对象
            HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request, runtime, DispatcherType.REQUEST);
            HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(servletRequest, response, runtime);
            servletRequest.setHttpServletResponse(servletResponse);
            HandlerContext handlerContext = new HandlerContext(servletRequest, servletResponse, runtime.getServletContext(), false);
            // just do it
            servletContext.getPipeline().handleRequest(handlerContext);
            //输出buffer中的数据
            servletResponse.flushServletBuffer();
        } catch (WrappedRuntimeException e) {
            e.getThrowable().printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

    }

    public void stop() {
        runtimes.forEach(ServletContextRuntime::stop);
        //卸载插件
        plugins.forEach(Plugin::uninstall);
    }

    public boolean isStarted() {
        return started;
    }

    private ServletContextRuntime matchRuntime(String requestUri) {
        for (ServletContextRuntime matchRuntime : runtimes) {
            String contextPath = matchRuntime.getContextPath();
            if (requestUri.startsWith(contextPath)) {
                return matchRuntime;
            }
        }
        throw new IllegalStateException("No match container runtime!");
    }

    /**
     * 若不存在根级容器，则初始化一个
     */
    private void initRootContainer() {
        if (runtimes.stream().noneMatch(runtime -> "/".equals(runtime.getContextPath()))) {
            ServletContextRuntime runtime = new ServletContextRuntime("/");
            runtime.getDeploymentInfo().setDefaultServlet(new DefaultServlet());
            runtimes.add(runtime);
        }
    }

    private ServletContextRuntime getServletRuntime(String location, String contextPath, ClassLoader parentClassLoader) throws Exception {
        ServletContextRuntime servletRuntime;
        //load web.xml file
        File contextFile = new File(location);
        WebAppInfo webAppInfo = new WebXmlParseEngine().load(contextFile);

        URLClassLoader urlClassLoader = getClassLoader(location, parentClassLoader);
        //new runtime object
        servletRuntime = new ServletContextRuntime(urlClassLoader, StringUtils.isBlank(contextPath) ? "/" + contextFile.getName() : contextPath);
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

        deploymentInfo.setHandlesTypesLoader(new HandlesTypesLoader(deploymentInfo.getClassLoader()));
        for (ServletContainerInitializer containerInitializer : ServiceLoader.load(ServletContainerInitializer.class, deploymentInfo.getClassLoader())) {
            LOGGER.info("load ServletContainerInitializer:" + containerInitializer.getClass().getName());
            deploymentInfo.addServletContainerInitializer(containerInitializer);
        }
        // ServletContainerInitializer 可能注解 handlesTypes
//        if (CollectionUtils.isNotEmpty(deploymentInfo.getServletContainerInitializers())) {
//            deploymentInfo.setHandlesTypesLoader(new HandlesTypesLoader(deploymentInfo.getClassLoader()));
//        }

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


        return servletRuntime;
    }

    private URLClassLoader getClassLoader(String location, ClassLoader parentClassLoader) throws MalformedURLException {
        List<URL> list = new ArrayList<>();
        File libDir = new File(location, "WEB-INF" + File.separator + "lib/");
        if (libDir.isDirectory()) {
            File[] files = libDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    list.add(file.toURI().toURL());
                }
            }
        }
        //list.sort((o1, o2) -> o2.toString().compareTo(o1.toString()));

        File classDir = new File(location, "WEB-INF" + File.separator + "classes/");
        if (classDir.isDirectory()) {
            list.add(classDir.toURI().toURL());
        }
        URL[] urls = new URL[list.size()];
        list.toArray(urls);
        return new URLClassLoader(urls, parentClassLoader);
    }
}
