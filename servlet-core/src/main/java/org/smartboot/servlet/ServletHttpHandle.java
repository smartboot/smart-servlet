/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletHttpHandle.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.conf.DeploymentInfo;
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
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHttpHandle extends HttpHandle {
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
    private static final String VERSION = "0.1.2-SNAPSHOT";
    private final List<ContainerRuntime> runtimes = new ArrayList<>();
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
        for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
            RunLogger.getLogger().log(Level.FINE, "load plugin: " + plugin.pluginName());
            plugins.add(plugin);
        }
        //安装插件
        plugins.forEach(plugin -> {
            RunLogger.getLogger().log(Level.FINE, "install plugin: " + plugin.pluginName());
            plugin.install();
        });

        //必须有一个 contextPath 为 "/" 的环境
        initRootContainer();
        //启动运行环境
        runtimes.forEach(runtime -> {
            runtime.getServletContext().setPipeline(pipeline);
            runtime.setPlugins(plugins);
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(runtime.getServletContext().getClassLoader());
            try {
                runtime.start();
            } catch (Exception e) {
                e.printStackTrace();
                runtime.getPlugins().forEach(plugin -> plugin.whenContainerStartError(runtime, e));
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        });
        //按contextPath长度倒序,防止被"/"优先匹配
        runtimes.sort((o1, o2) -> o2.getContextPath().length() - o1.getContextPath().length());

        System.out.println(BANNER + "\r\n :: smart-servlet :: (" + VERSION + ")");

    }

    /**
     * 若不存在根级容器，则初始化一个
     */
    private void initRootContainer() {
        if (runtimes.stream().noneMatch(runtime -> "/".equals(runtime.getContextPath()))) {
            ContainerRuntime runtime = new ContainerRuntime("/");
            DeploymentInfo deploymentInfo = runtime.getDeploymentInfo();
            deploymentInfo.setDefaultServlet(new DefaultServlet());
            deploymentInfo.setClassLoader(Thread.currentThread().getContextClassLoader());
            runtimes.add(runtime);
        }
    }

    public void addRuntime(ContainerRuntime runtime) {
        if (runtimes.stream().anyMatch(containerRuntime -> containerRuntime.getContextPath().equals(runtime.getContextPath()))) {
            throw new IllegalArgumentException("contextPath: " + runtime.getContextPath() + " is already exists!");
        }
        runtimes.add(runtime);
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ContainerRuntime runtime = matchRuntime(request.getRequestURI());
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
        runtimes.forEach(ContainerRuntime::stop);
        //卸载插件
        plugins.forEach(Plugin::uninstall);
    }

    public boolean isStarted() {
        return started;
    }

    public ContainerRuntime matchRuntime(String requestUri) {
        for (ContainerRuntime matchRuntime : runtimes) {
            String contextPath = matchRuntime.getContextPath();
            if (requestUri.startsWith(contextPath)) {
                return matchRuntime;
            }
        }
        throw new IllegalStateException("No match container runtime!");
    }
}
