/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletHttpHandle.java
 * Date: 2020-11-14
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
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.util.LRUCache;

import javax.servlet.DispatcherType;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletHttpHandle extends HttpHandle {
    private final HandlePipeline pipeline = new HandlePipeline();
    private final List<ContainerRuntime> runtimes = new ArrayList<>();
    /**
     * 请求映射的Servlet运行环境
     */
    private final LRUCache<String, ContainerRuntime> contextCache = new LRUCache<>();
    private volatile boolean started = false;

    public void start() {
        if (started) {
            return;
        }
        started = true;
        pipeline.next(new ServletRequestListenerHandler())
                .next(new ServletMatchHandler())
                .next(new FilterMatchHandler())
                .next(new ServletServiceHandler());
        //启动运行环境
        runtimes.forEach(runtime -> {
            ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
            Thread.currentThread().setContextClassLoader(runtime.getServletContext().getClassLoader());
            try {
                runtime.start();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Thread.currentThread().setContextClassLoader(originalClassLoader);
            }
        });
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
            if (runtime == null) {
                response.setHttpStatus(HttpStatus.NOT_FOUND);
                return;
            }
            Thread.currentThread().setContextClassLoader(runtime.getServletContext().getClassLoader());

            //封装上下文对象
            HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request, runtime, DispatcherType.REQUEST);
            HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(servletRequest, response);
            HandlerContext handlerContext = new HandlerContext(servletRequest, servletResponse, runtime.getServletContext());

            // just do it
            pipeline.handleRequest(handlerContext);
        } catch (WrappedRuntimeException e) {
            e.getThrowable().printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }

    }

    public void stop() {

    }

    public boolean isStarted() {
        return started;
    }

    public ContainerRuntime matchRuntime(String servletPath) {
        ContainerRuntime runtime = contextCache.get(servletPath);
        if (runtime != null) {
            return runtime;
        }
        for (ContainerRuntime matchRuntime : runtimes) {
            if (StringUtils.startsWith(servletPath, matchRuntime.getServletContext().getDeploymentInfo().getContextPath())) {
                runtime = matchRuntime;
                contextCache.put(servletPath, runtime);
                break;
            }
        }
        return runtime;
    }
}
