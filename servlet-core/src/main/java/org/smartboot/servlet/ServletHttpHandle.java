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
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.handler.FilterMatchHandler;
import org.smartboot.servlet.handler.HandlePipeline;
import org.smartboot.servlet.handler.ServletMatchHandler;
import org.smartboot.servlet.handler.ServletRequestListenerHandler;
import org.smartboot.servlet.handler.ServletServiceHandler;
import org.smartboot.servlet.handler.WelcomeFileHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.Plugin;
import org.smartboot.servlet.util.LRUCache;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

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
    private static final String VERSION = "1.0.0-SNAPSHOT";
    private final List<ContainerRuntime> runtimes = new ArrayList<>();
    /**
     * 请求映射的Servlet运行环境
     */
    private final LRUCache<String, ContainerRuntime> contextCache = new LRUCache<>();
    private final List<Plugin> plugins = new ArrayList<>();
    private volatile boolean started = false;
    private String defaultContext = null;

    public void start() {
        if (started) {
            return;
        }
        started = true;
        HandlePipeline pipeline = new HandlePipeline();
        pipeline.next(new ServletRequestListenerHandler())
                .next(new ServletMatchHandler())
                .next(new WelcomeFileHandler())
                .next(new FilterMatchHandler())
                .next(new ServletServiceHandler());
        //扫描插件
        for (Plugin plugin : ServiceLoader.load(Plugin.class)) {
            System.out.println("扫描插件 -- " + plugin.pluginName());
            plugins.add(plugin);
        }
        //安装插件
        plugins.forEach(plugin -> {
            System.out.println("安装插件 -- " + plugin.pluginName());
            plugin.install();
        });

        //启动运行环境
        runtimes.forEach(runtime -> {
            runtime.getServletContext().setPipeline(pipeline);
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(runtime.getServletContext().getClassLoader());
            try {
                runtime.start();
                plugins.forEach(plugin -> {
                    System.out.println("启动插件 - " + plugin.pluginName());
                    plugin.startContainer(runtime);
                });
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        });
        System.out.println(BANNER + "\r\n :: smart-servlet :: (" + VERSION + ")");

    }

    public void addRuntime(ContainerRuntime runtime) {
        runtimes.add(runtime);
    }

    @Override
    public void doHandle(HttpRequest request, HttpResponse response) throws IOException {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境
            ContainerRuntime runtime = matchRuntime(request.getRequestURI());
            ContainerRuntime defaultRuntime = null;
            if (runtime == null) {
                defaultRuntime = matchDefaultRuntime(request.getRequestURI());
                runtime = defaultRuntime;
            }
            if (runtime == null) {
                response.setHttpStatus(HttpStatus.NOT_FOUND);
                return;
            }
            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());

            //封装上下文对象
            HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request, runtime, DispatcherType.REQUEST);
            HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(servletRequest, response);
            HandlerContext handlerContext = new HandlerContext(servletRequest, servletResponse, runtime.getServletContext(), false);
            if (defaultRuntime != null) {
                servletRequest.setRequestURI(defaultRuntime.getDeploymentInfo().getContextPath() + servletRequest.getRequestURI());
            } else if (!servletRequest.getRequestURI().startsWith(runtime.getDeploymentInfo().getContextPath())) {
                servletRequest.setRequestURI(runtime.getDeploymentInfo().getContextPath() + servletRequest.getRequestURI());
            }
            // just do it
            servletContext.getPipeline().handleRequest(handlerContext);
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
        plugins.forEach(Plugin::uninstall);
    }

    public boolean isStarted() {
        return started;
    }

    public ContainerRuntime matchRuntime(String requestUri) {
        ContainerRuntime runtime = contextCache.get(requestUri);
        if (runtime != null) {
            return runtime;
        }
        for (ContainerRuntime matchRuntime : runtimes) {
            //todo 兼容 请求 uri 为 servletPath结尾不带 '/' 的情况
            String contextPath = matchRuntime.getServletContext().getDeploymentInfo().getContextPath();
            if (StringUtils.startsWith(requestUri, contextPath) || requestUri.equals(contextPath.substring(0, contextPath.length() - 1))) {
                runtime = matchRuntime;
                contextCache.put(requestUri, runtime);
                break;
            }
        }
        return runtime;
    }

    public ContainerRuntime matchDefaultRuntime(String requestUri) {
        if (defaultContext == null) {
            return null;
        }

        ContainerRuntime containerRuntime = runtimes.stream().filter(runtime -> runtime.getDeploymentInfo().getContextPath().equals(defaultContext)).findFirst().orElse(null);
        if (containerRuntime != null) {
            contextCache.put(requestUri, containerRuntime);
        }
        return containerRuntime;
    }

    public void setDefaultContext(String defaultContext) {
        this.defaultContext = defaultContext;
    }
}
