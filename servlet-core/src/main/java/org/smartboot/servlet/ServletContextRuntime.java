/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ContainerRuntime.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.ServletContainerInitializerInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.FilterConfigImpl;
import org.smartboot.servlet.impl.ServletConfigImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.Plugin;
import org.smartboot.servlet.provider.DispatcherProvider;
import org.smartboot.servlet.provider.MemoryPoolProvider;
import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.servlet.provider.WebsocketProvider;
import org.smartboot.servlet.sandbox.SandBox;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EventListener;
import java.util.List;

/**
 * 应用级子容器的运行时环境
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletContextRuntime {
    private String displayName;
    private String description;
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
     * Websocket服务提供者
     */
    private WebsocketProvider websocketProvider = SandBox.INSTANCE.getWebsocketProvider();
    /**
     * 关联至本运行环境的插件集合
     */
    private List<Plugin> plugins = Collections.emptyList();
    /**
     * 子容器是否已启动
     */
    private boolean started = false;

    private final String localPath;

    public ServletContextRuntime(String contextPath) {
        this(null, Thread.currentThread().getContextClassLoader(), contextPath);
    }

    public ServletContextRuntime(String localPath, ClassLoader classLoader, String contextPath) {
        this.localPath = localPath;
        if (StringUtils.isBlank(contextPath)) {
            this.contextPath = "/";
        } else {
            this.contextPath = contextPath;
        }
        this.deploymentInfo.setClassLoader(classLoader);
    }

    /**
     * 当前Servlet应用的本地路径
     */
    public String getLocalPath() {
        return localPath;
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
    public void start() {
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            //有些场景下ServletContainerInitializer初始化依赖当前容器的类加载器
            Thread.currentThread().setContextClassLoader(deploymentInfo.getClassLoader());
            plugins.forEach(plugin -> plugin.willStartContainer(this));

            DeploymentInfo deploymentInfo = servletContext.getDeploymentInfo();

            //初始化容器
            initContainer(deploymentInfo);

            //实例化Servlet
            newServletsInstance(deploymentInfo);

            //启动Listener
            for (String eventListenerInfo : deploymentInfo.getEventListeners()) {
                EventListener listener = (EventListener) deploymentInfo.getClassLoader().loadClass(eventListenerInfo).newInstance();
                servletContext.addListener(listener);
            }

            //启动Servlet
            initServlet(deploymentInfo);

            //启动Filter
            initFilter(deploymentInfo);
            started = true;
            deploymentInfo.amazing();
            plugins.forEach(plugin -> plugin.onContainerStartSuccess(this));
        } catch (Exception e) {
            e.printStackTrace();
            plugins.forEach(plugin -> plugin.whenContainerStartError(this, e));
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private void newServletsInstance(DeploymentInfo deploymentInfo) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (ServletInfo servletInfo : deploymentInfo.getServlets().values()) {
            if (!servletInfo.isDynamic()) {
                Servlet servlet = (Servlet) deploymentInfo.getClassLoader().loadClass(servletInfo.getServletClass()).newInstance();
                servletInfo.setServlet(servlet);
            }
        }
        //绑定 default Servlet
        if (!deploymentInfo.getServlets().containsKey(ServletInfo.DEFAULT_SERVLET_NAME)) {
            ServletInfo servletInfo = new ServletInfo();
            servletInfo.setServletName(ServletInfo.DEFAULT_SERVLET_NAME);
            servletInfo.setServlet(new DefaultServlet(deploymentInfo.getWelcomeFiles()));
            deploymentInfo.addServlet(servletInfo);
        }
    }


    /**
     * 初始化容器
     *
     * @param deploymentInfo 部署信息
     */
    private void initContainer(DeploymentInfo deploymentInfo) throws ServletException {
        //注册临时目录
        servletContext.setAttribute(ServletContext.TEMPDIR, new File(System.getProperty("java.io.tmpdir")));
        //扫描 handleType
        if (deploymentInfo.getHandlesTypesLoader() != null) {
            long start = System.currentTimeMillis();
            deploymentInfo.getHandlesTypesLoader().scanHandleTypes()
                    .forEach((servletContainerInitializer, handlesTypes) -> {
                        ServletContainerInitializerInfo initializerInfo = new ServletContainerInitializerInfo(servletContainerInitializer, handlesTypes);
                        deploymentInfo.getServletContainerInitializers().add(initializerInfo);
                    });
            deploymentInfo.getHandlesTypesLoader().clear();
            deploymentInfo.setHandlesTypesLoader(null);
            System.out.println("scanHandleTypes use :" + (System.currentTimeMillis() - start));
        }
        for (ServletContainerInitializerInfo servletContainerInitializer : deploymentInfo.getServletContainerInitializers()) {
            servletContainerInitializer.getServletContainerInitializer().onStartup(servletContainerInitializer.getHandlesTypes(), servletContext);
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
            ServletConfig servletConfig = new ServletConfigImpl(servletInfo, servletContext);
            servletInfo.getServlet().init(servletConfig);
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
                filter = (Filter) deploymentInfo.getClassLoader().loadClass(filterInfo.getFilterClass()).newInstance();
                filterInfo.setFilter(filter);
            }
            filter.init(filterConfig);
        }
    }

    public void stop() {
        plugins.forEach(plugin -> plugin.willStopContainer(this));
        deploymentInfo.getServlets().values().forEach(servletInfo -> servletInfo.getServlet().destroy());
        ServletContextEvent event = deploymentInfo.getServletContextListeners().isEmpty() ? null : new ServletContextEvent(servletContext);
        deploymentInfo.getServletContextListeners().forEach(servletContextListener -> servletContextListener.contextDestroyed(event));

        plugins.forEach(plugin -> plugin.onContainerStopped(this));
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

    public WebsocketProvider getWebsocketProvider() {
        return websocketProvider;
    }

    public void setWebsocketProvider(WebsocketProvider websocketProvider) {
        this.websocketProvider = websocketProvider;
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

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
