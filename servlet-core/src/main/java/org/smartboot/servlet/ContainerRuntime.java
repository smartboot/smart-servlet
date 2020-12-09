/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ContainerRuntime.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.utils.StringUtils;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.FilterConfigImpl;
import org.smartboot.servlet.impl.ServletConfigImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.Plugin;
import org.smartboot.servlet.provider.DispatcherProvider;
import org.smartboot.servlet.provider.MemoryPoolProvider;
import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.servlet.sandbox.SandBox;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

/**
 * 运行时环境
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ContainerRuntime {
    /**
     * 容器部署信息
     */
    private final DeploymentInfo deploymentInfo = new DeploymentInfo();
    /**
     * 服务上下文
     */
    private final ServletContextImpl servletContext = new ServletContextImpl(this);
    /**
     * 上下文路径
     */
    private final String contextPath;
    /**
     * Dispatcher服务提供者
     */
    private DispatcherProvider dispatcherProvider = SandBox.INSTANCE.getDispatcherProvider();
    /**
     * Session服务提供者
     */
    private SessionProvider sessionProvider = SandBox.INSTANCE.getSessionProvider();

    /**
     * 内存池服务提供者
     */
    private MemoryPoolProvider memoryPoolProvider = SandBox.INSTANCE.getMemoryPoolProvider();
    /**
     * 关联至本运行环境的插件集合
     */
    private List<Plugin> plugins = Collections.emptyList();

    private boolean started = false;

    public ContainerRuntime(String contextPath) {
        if (StringUtils.isBlank(contextPath)) {
            this.contextPath = "/";
        } else {
            this.contextPath = contextPath;
        }
    }

    public List<Plugin> getPlugins() {
        return plugins;
    }

    public void setPlugins(List<Plugin> plugins) {
        this.plugins = plugins;
    }

    /**
     * 启动容器
     */
    public void start() throws Exception {
        plugins.forEach(plugin -> {
            plugin.willStartContainer(this);
        });

        DeploymentInfo deploymentInfo = servletContext.getDeploymentInfo();
        //设置ServletContext参数
        Map<String, String> params = deploymentInfo.getInitParameters();
        params.forEach(servletContext::setInitParameter);

        //初始化容器
        initContainer(deploymentInfo);

        //启动Listener
        for (String eventListenerInfo : deploymentInfo.getEventListeners()) {
            EventListener listener = (EventListener) Thread.currentThread().getContextClassLoader().loadClass(eventListenerInfo).newInstance();
            if (ServletContextListener.class.isAssignableFrom(listener.getClass())) {
                ServletContextListener contextListener = (ServletContextListener) listener;
                ServletContextEvent event = new ServletContextEvent(servletContext);
                contextListener.contextInitialized(event);
                deploymentInfo.addServletContextListener(contextListener);
                RunLogger.getLogger().log(Level.FINE, "contextInitialized listener:" + listener);
            } else if (ServletRequestListener.class.isAssignableFrom(listener.getClass())) {
                deploymentInfo.addServletRequestListener((ServletRequestListener) listener);
                RunLogger.getLogger().log(Level.FINE, "ServletRequestListener listener:" + listener);
            } else if (ServletContextAttributeListener.class.isAssignableFrom(listener.getClass())) {
                deploymentInfo.addServletContextAttributeListener((ServletContextAttributeListener) listener);
                RunLogger.getLogger().log(Level.FINE, "ServletContextAttributeListener listener:" + listener);
            } else {
                throw new RuntimeException(listener.toString());
            }
        }

        //启动Servlet
        initServlet(deploymentInfo);

        //启动Filter
        initFilter(deploymentInfo);
        started = true;

        plugins.forEach(plugin -> {
            plugin.onContainerStartSuccess(this);
        });
    }

    /**
     * 初始化容器
     *
     * @param deploymentInfo 部署信息
     */
    private void initContainer(DeploymentInfo deploymentInfo) throws ServletException {
        for (ServletContainerInitializer servletContainerInitializer : deploymentInfo.getServletContainerInitializers()) {
            servletContainerInitializer.onStartup(null, servletContext);
        }
    }

    /**
     * 初始化Servlet
     *
     * @param deploymentInfo 部署信息
     */
    private void initServlet(DeploymentInfo deploymentInfo) throws Exception {
        List<ServletInfo> servletInfoList = new ArrayList<>(deploymentInfo.getServlets().values());
        servletInfoList.sort(Comparator.comparingInt(ServletInfo::getLoadOnStartup));

        for (ServletInfo servletInfo : servletInfoList) {
            Servlet servlet;
            ServletConfig servletConfig = new ServletConfigImpl(servletInfo, servletContext);
            if (servletInfo.isDynamic()) {
                servlet = servletInfo.getServlet();
            } else {
                servlet = (Servlet) Thread.currentThread().getContextClassLoader().loadClass(servletInfo.getServletClass()).newInstance();
                servletInfo.setServlet(servlet);
            }
            servlet.init(servletConfig);
        }
    }

    /**
     * 初始化Filter
     *
     * @param deploymentInfo 部署信息
     */
    private void initFilter(DeploymentInfo deploymentInfo) throws Exception {
        for (FilterInfo filterInfo : deploymentInfo.getFilters().values()) {
            FilterConfig filterConfig = new FilterConfigImpl(filterInfo, servletContext);
            Filter filter;
            if (filterInfo.isDynamic()) {
                filter = filterInfo.getFilter();
            } else {
                filter = (Filter) Thread.currentThread().getContextClassLoader().loadClass(filterInfo.getFilterClass()).newInstance();
                filterInfo.setFilter(filter);
            }
            filter.init(filterConfig);
        }
    }

    public void stop() {
        plugins.forEach(plugin -> {
            plugin.willStopContainer(this);
        });
        servletContext.getDeploymentInfo().getServlets().values().forEach(servletInfo -> servletInfo.getServlet().destroy());
        servletContext.getDeploymentInfo().getServletContextListeners().forEach(servletContextListener -> {
            ServletContextEvent event = new ServletContextEvent(servletContext);
            servletContextListener.contextDestroyed(event);
        });

        plugins.forEach(plugin -> {
            plugin.onContainerStopped(this);
        });
    }

    public DispatcherProvider getDispatcherProvider() {
        return dispatcherProvider;
    }

    public void setDispatcherProvider(DispatcherProvider dispatcherProvider) {
        this.dispatcherProvider = dispatcherProvider;
    }

    public SessionProvider getSessionProvider() {
        return sessionProvider;
    }

    public void setSessionProvider(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public MemoryPoolProvider getMemoryPoolProvider() {
        return memoryPoolProvider;
    }

    public void setMemoryPoolProvider(MemoryPoolProvider memoryPoolProvider) {
        this.memoryPoolProvider = memoryPoolProvider;
    }

    public String getContextPath() {
        return contextPath;
    }

    public ServletContextImpl getServletContext() {
        return servletContext;
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public boolean isStarted() {
        return started;
    }
}
