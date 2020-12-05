/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletContextImpl.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.utils.Mimetypes;
import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.enums.ServletContextPathType;
import org.smartboot.servlet.handler.HandlePipeline;

import javax.servlet.Filter;
import javax.servlet.FilterRegistration;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextAttributeEvent;
import javax.servlet.ServletContextAttributeListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.SessionCookieConfig;
import javax.servlet.SessionTrackingMode;
import javax.servlet.descriptor.JspConfigDescriptor;
import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletContextImpl implements ServletContext {
    //    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextImpl.class);
    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();
    private final ContainerRuntime containerRuntime;
    private final DeploymentInfo deploymentInfo;
    private SessionCookieConfig sessionCookieConfig = new SessionCookieConfigImpl();
    private ServletContextPathType pathType = ServletContextPathType.PATH;
    /**
     * 请求执行管道
     */
    private HandlePipeline pipeline;

    public ServletContextImpl(ContainerRuntime containerRuntime) {
        this.containerRuntime = containerRuntime;
        this.deploymentInfo = containerRuntime.getDeploymentInfo();
    }

    @Override
    public String getContextPath() {
        String contextPath = containerRuntime.getContextPath();
        if ("/".equals(contextPath)) {
            return "";
        }
        return contextPath;
    }

    @Override
    public ServletContext getContext(String uripath) {
        //获取uri归属的 DeploymentRuntime
//        LOGGER.error("unSupport now");
        return null;
    }

    @Override
    public int getMajorVersion() {
        return 0;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 0;
    }

    @Override
    public int getEffectiveMinorVersion() {
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        return Mimetypes.getInstance().getMimetype(file);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        //todo 暂时 返回 null 使org.apache.jasper.servlet.JasperInitializer#onStartup 可以执行
        return null;
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        if (path == null || deploymentInfo.getContextUrl() == null) {
            return null;
        }
        if (path.length() == 0) {
            path = "/";
        } else if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        URL pathUrl = new URL(deploymentInfo.getContextUrl(), path.substring(1));
        //todo 判断文件是否存在
        URL url = null;
        try {
            if (new File(pathUrl.toURI()).exists()) {
                url = pathUrl;
            }
        } catch (URISyntaxException e) {
            RunLogger.getLogger().log(Level.SEVERE, "path:" + pathUrl + " ，URISyntaxException:" + e.getMessage());
        }
        RunLogger.getLogger().log(Level.SEVERE, "path" + ((url == null) ? "(404):" : ":") + pathUrl + " ，url:" + deploymentInfo.getContextUrl());
        return url;
    }

    @Override
    public InputStream getResourceAsStream(String path) {
        try {
            URL url = this.getResource(path);
            return url == null || !new File(url.toURI()).isFile() ? null : url.openStream();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * <pre>
     * 《Servlet3.1规范中文版》9.1获得一个RequestDispatcher
     * getRequestDispatcher 方法需要一个 String 类型的参数􏰁述在 ServletContext 作用域内的路径。
     * 这个路径必须 是相对于 ServletContext 的根路径，并且以‟/‟开头，或者为空。
     * 该方法根据这个路径使用 servlet 路径匹配规 则(见第 12 章，请求映射到 servlet)来查找 servlet，
     * 把它包装成 RequestDispatcher 对象并返回。
     * 如果基于 给定的路径没有找到相应的 servlet，那么􏰀供一个返回那个路径内容的 RequestDispatcher。
     * </pre>
     *
     * @param path
     * @return
     */
    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return containerRuntime.getDispatcherProvider().getRequestDispatcher(this, path);
    }

    /**
     * <pre>
     * 《Servlet3.1规范中文版》9.1获得一个RequestDispatcher
     * getNamedDispatcher 方法使用一个 ServletContext 知道的 servlet 名称作为参数。
     * 如果找到一个 servlet，则把 它包装成 RequestDispatcher 对象，并返回该对象。
     * 如果没有与给定名字相关的 servlet，该方法必须返回 null。
     * </pre>
     *
     * @param name
     * @return
     */
    @Override
    public RequestDispatcher getNamedDispatcher(String name) {
        return containerRuntime.getDispatcherProvider().getNamedDispatcher(this, name);
    }

    @Override
    public Servlet getServlet(String name) throws ServletException {
        ServletInfo servletInfo = deploymentInfo.getServlets().get(name);
        return servletInfo == null ? null : servletInfo.getServlet();
    }

    @Override
    public Enumeration<Servlet> getServlets() {
        return Collections.enumeration(deploymentInfo.getServlets().values().stream().map(ServletInfo::getServlet).collect(Collectors.toList()));
    }

    @Override
    public Enumeration<String> getServletNames() {
        return Collections.enumeration(deploymentInfo.getServlets().keySet());
    }

    @Override
    public void log(String msg) {
//        LOGGER.info(msg);
    }

    @Override
    public void log(Exception exception, String msg) {
//        LOGGER.error(msg, exception);
    }

    @Override
    public void log(String message, Throwable throwable) {
//        LOGGER.error(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        try {
            URL url = getResource(path);
            if (url != null) {
                return new File(url.toURI()).getAbsolutePath();
            }
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String getServerInfo() {
        return "smart-servlet";
    }

    @Override
    public String getInitParameter(String name) {
        return deploymentInfo.getInitParameters().get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(deploymentInfo.getInitParameters().keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (deploymentInfo.getInitParameters().containsKey(name)) {
            return false;
        }
        deploymentInfo.addInitParameter(name, value);
        return true;
    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public void setAttribute(String name, Object object) {
        if (object == null) {
            removeAttribute(name);
            return;
        }

        Object oldValue = attributes.put(name, object);

        List<ServletContextAttributeListener> listeners = deploymentInfo.getServletContextAttributeListeners();
        if (listeners.size() > 0) {
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(this, name, object);
            deploymentInfo.getServletContextAttributeListeners().forEach(listener -> {
                if (oldValue == null) {
                    listener.attributeAdded(event);
                } else {
                    listener.attributeReplaced(event);
                }
            });
        }
    }

    @Override
    public void removeAttribute(String name) {
        Object value = attributes.remove(name);
        List<ServletContextAttributeListener> listeners = deploymentInfo.getServletContextAttributeListeners();
        if (listeners.size() > 0) {
            ServletContextAttributeEvent event = new ServletContextAttributeEvent(this, name, value);
            listeners.forEach(listener -> listener.attributeRemoved(event));
        }
    }

    @Override
    public String getServletContextName() {
        return deploymentInfo.getDisplayName();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        ServletInfo servletInfo = new ServletInfo();
        servletInfo.setServletName(servletName);
        servletInfo.setServlet(servlet);
        servletInfo.setServletClass(servlet.getClass().getName());
        servletInfo.setDynamic(true);
        deploymentInfo.addServlet(servletInfo);
        return new ApplicationServletRegistration(servletInfo, deploymentInfo);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        FilterInfo filterInfo = new FilterInfo();
        filterInfo.setFilter(filter);
        filterInfo.setFilterName(filterName);
        filterInfo.setFilterClass(filter.getClass().getName());
        filterInfo.setDynamic(true);
        deploymentInfo.addFilter(filterInfo);
        return new ApplicationFilterRegistration(filterInfo, deploymentInfo);
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        return sessionCookieConfig;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(String className) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> void addListener(T t) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        throw new UnsupportedOperationException();
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        //todo 暂时 返回 null 使org.apache.jasper.servlet.JasperInitializer#onStartup 可以执行
        return null;
    }

    @Override
    public ClassLoader getClassLoader() {
        return deploymentInfo.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getVirtualServerName() {
        throw new UnsupportedOperationException();
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public HandlePipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(HandlePipeline pipeline) {
        this.pipeline = pipeline;
    }
}
