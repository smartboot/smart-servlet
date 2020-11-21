/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ContainerRuntime.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;

import org.smartboot.http.logging.RunLogger;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.FilterConfigImpl;
import org.smartboot.servlet.impl.ServletConfigImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.session.MemorySessionManager;
import org.smartboot.servlet.session.SessionManager;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequestListener;
import java.util.Comparator;
import java.util.EventListener;
import java.util.Map;
import java.util.logging.Level;

/**
 * 运行时环境
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ContainerRuntime {
    private final ServletContextImpl servletContext = new ServletContextImpl();
    private final SessionManager sessionManager = new MemorySessionManager();
    private volatile boolean started = false;

    public ServletContextImpl getServletContext() {
        return servletContext;
    }

    public SessionManager getSessionManager() {
        return sessionManager;
    }

    /**
     * 启动容器
     *
     * @throws Exception
     */
    public void start() throws Exception {
        DeploymentInfo deploymentInfo = servletContext.getDeploymentInfo();
        //设置ServletContext参数
        Map<String, String> params = deploymentInfo.getInitParameters();
        params.forEach(servletContext::setInitParameter);


        //初始化容器
        deploymentInfo.getServletContainerInitializers().forEach(servletContainerInitializer -> {
            try {
                servletContainerInitializer.onStartup(null, servletContext);
            } catch (ServletException e) {
                e.printStackTrace();
            }
        });

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
            } else {
                throw new RuntimeException(listener.toString());
            }
        }

        //启动Servlet
        deploymentInfo.getServlets().values().stream().sorted(Comparator.comparingInt(ServletInfo::getLoadOnStartup)).forEach(servletInfo -> {
            try {
                Servlet servlet;
                ServletConfig servletConfig = new ServletConfigImpl(servletInfo, servletContext);
                if (servletInfo.isDynamic()) {
                    servlet = servletInfo.getServlet();
                } else {
                    servlet = (Servlet) Thread.currentThread().getContextClassLoader().loadClass(servletInfo.getServletClass()).newInstance();
                    servletInfo.setServlet(servlet);
                }
//                LOGGER.info("init servlet:{}", servlet);
                servlet.init(servletConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        //启动Filter
        deploymentInfo.getFilters().values().forEach(filterInfo -> {
            try {
                FilterConfig filterConfig = new FilterConfigImpl(filterInfo, servletContext);
                Filter filter = null;
                if (filterInfo.isDynamic()) {
                    filter = filterInfo.getFilter();
                } else {
                    filter = (Filter) Thread.currentThread().getContextClassLoader().loadClass(filterInfo.getFilterClass()).newInstance();
                    filterInfo.setFilter(filter);
                }
                filter.init(filterConfig);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    public void stop() {
        servletContext.getDeploymentInfo().getServletContextListeners().forEach(servletContextListener -> {
            ServletContextEvent event = new ServletContextEvent(servletContext);
            servletContextListener.contextDestroyed(event);
        });
        servletContext.getDeploymentInfo().getServlets().values().forEach(servletInfo -> {
            servletInfo.getServlet().destroy();
        });
    }

    public boolean isStarted() {
        return started;
    }
}
