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
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.handler.FilterMatchHandler;
import org.smartboot.servlet.handler.HandlePipeline;
import org.smartboot.servlet.handler.ServletMatchHandler;
import org.smartboot.servlet.handler.ServletRequestListenerHandler;
import org.smartboot.servlet.handler.ServletServiceHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.Plugin;

import javax.servlet.DispatcherType;
import java.util.ArrayList;
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
    private static final String VERSION = "0.1.3-SNAPSHOT";
    private final List<ApplicationRuntime> runtimes = new ArrayList<>();
    private final List<Plugin> plugins = new ArrayList<>();
    private volatile boolean started = false;

    public void start() {
        if (started) {
            return;
        }
        started = true;
        HandlePipeline pipeline = new HandlePipeline();
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
            plugin.install();
        });
    }

    public void addRuntime(ApplicationRuntime runtime) {
        if (runtimes.stream().anyMatch(containerRuntime -> containerRuntime.getContextPath().equals(runtime.getContextPath()))) {
            throw new IllegalArgumentException("contextPath: " + runtime.getContextPath() + " is already exists!");
        }
        runtimes.add(runtime);
    }

    public void addRuntime(String location, String contextPath) throws Exception {
        addRuntime(location, contextPath, Thread.currentThread().getContextClassLoader());
    }

    public void addRuntime(String location, String contextPath, ClassLoader parentClassLoader) throws Exception {
        WebContextRuntime webContextRuntime = new WebContextRuntime(location, contextPath, parentClassLoader);
        addRuntime(webContextRuntime.getServletRuntime());
    }

    public void doHandle(WebSocketRequest request, WebSocketResponse response) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ApplicationRuntime runtime = matchRuntime(request.getRequestURI());
            if (!runtime.isStarted()) {
                throw new IllegalStateException("container is not started");
            }
            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());
            runtime.getWebsocketProvider().doHandle(runtime,request, response);
        } catch (WrappedRuntimeException e) {
            e.getThrowable().printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

    }

    public void doHandle(HttpRequest request, HttpResponse response) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ApplicationRuntime runtime = matchRuntime(request.getRequestURI());
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
        runtimes.forEach(ApplicationRuntime::stop);
        //卸载插件
        plugins.forEach(Plugin::uninstall);
    }

    public boolean isStarted() {
        return started;
    }

    private ApplicationRuntime matchRuntime(String requestUri) {
        for (ApplicationRuntime matchRuntime : runtimes) {
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
            ApplicationRuntime runtime = new ApplicationRuntime("/");
            runtime.getDeploymentInfo().setDefaultServlet(new DefaultServlet());
            runtimes.add(runtime);
        }
    }
}
