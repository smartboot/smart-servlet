/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterRegistration;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextAttributeEvent;
import jakarta.servlet.ServletContextAttributeListener;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequestAttributeListener;
import jakarta.servlet.ServletRequestListener;
import jakarta.servlet.SessionCookieConfig;
import jakarta.servlet.SessionTrackingMode;
import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.http.HttpSessionAttributeListener;
import jakarta.servlet.http.HttpSessionIdListener;
import jakarta.servlet.http.HttpSessionListener;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.Mimetypes;
import org.smartboot.http.common.utils.StringUtils;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.conf.DeploymentInfo;
import tech.smartboot.servlet.conf.FilterInfo;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.enums.ServletContextPathType;
import tech.smartboot.servlet.exception.WrappedRuntimeException;
import tech.smartboot.servlet.handler.HandlerPipeline;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletContextImpl implements ServletContext {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextImpl.class);
    private final ConcurrentMap<String, Object> attributes = new ConcurrentHashMap<>();
    private final ServletContextRuntime runtime;
    private final DeploymentInfo deploymentInfo;
    private final SessionCookieConfig sessionCookieConfig;
    private ServletContextPathType pathType = ServletContextPathType.PATH;

    private JspConfigDescriptor jspConfigDescriptor;
    private final Set<SessionTrackingMode> defaultSessionTrackingModes = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(SessionTrackingMode.COOKIE, SessionTrackingMode.URL)));
    private Set<SessionTrackingMode> sessionTrackingModes = defaultSessionTrackingModes;
    private Map<String, String> mimetypes = new HashMap<>();
    /**
     * 请求执行管道
     */
    private HandlerPipeline pipeline;

    private String responseCharacterEncoding;

    private String requestCharacterEncoding;

    private ServletContextWrapperListener currentInitializeContext;

    public ServletContextImpl(ServletContextRuntime runtime) {
        this.runtime = runtime;
        this.deploymentInfo = runtime.getDeploymentInfo();
        sessionCookieConfig = new SessionCookieConfigImpl(runtime);
    }

    @Override
    public String getContextPath() {
        String contextPath = runtime.getContextPath();
        if ("/".equals(contextPath)) {
            return "";
        }
        return contextPath;
    }

    @Override
    public ServletContext getContext(String uripath) {
        //获取uri归属的 DeploymentRuntime
//        LOGGER.error("unSupport now");
        return runtime.getContainerRuntime().matchRuntime(uripath).getServletContext();
    }

    @Override
    public int getMajorVersion() {
        return 6;
    }

    @Override
    public int getMinorVersion() {
        return 1;
    }

    @Override
    public int getEffectiveMajorVersion() {
        return 6;
    }

    @Override
    public int getEffectiveMinorVersion() {
        checkContextInitializeState();
        return 0;
    }

    @Override
    public String getMimeType(String file) {
        String ext = Mimetypes.getFileExtension(file);
        String type = mimetypes.get(ext);
        if (type != null) {
            return type;
        }
        type = Mimetypes.getInstance().getMimetypeByExtension(ext);
        //Returns the MIME type of the specified file, or null if the MIME type is not known.
        return type == Mimetypes.DEFAULT_MIMETYPE ? null : type;
    }

    public void putMimeTypes(String file, String type) {
        mimetypes.put(file, type);
    }

    @Override
    public Set<String> getResourcePaths(String path) {
        try {
            URL url = getResource(path);
            if (url == null) {
                LOGGER.warn(path + " resource not exists");
                return Collections.emptySet();
            }
            File file = new File(url.toURI());
            if (file.isDirectory()) {
                Set<String> set = new HashSet<>();
                boolean endWith = path.endsWith("/");
                for (File subFile : Objects.requireNonNull(file.listFiles())) {
                    if (subFile.isDirectory()) {
                        if (endWith) {
                            set.add(path + "/" + subFile.getName() + "/");
                        } else {
                            set.add(path + subFile.getName() + "/");
                        }
                    } else {
                        if (endWith) {
                            set.add(path + subFile.getName());
                        } else {
                            set.add(path + "/" + subFile.getName());
                        }

                    }
                }
                return set;
            }
        } catch (Exception e) {
            LOGGER.error("getResourcePaths exception", e);
        }
        return Collections.emptySet();
    }

    @Override
    public URL getResource(String path) throws MalformedURLException {
        //The path must begin with a /
        if (!path.startsWith("/")) {
            throw new MalformedURLException();
        }
        URL pathUrl = null;
        if (deploymentInfo.getContextUrl() != null) {
            pathUrl = new URL(deploymentInfo.getContextUrl(), path.substring(1));
        }
        //todo 判断文件是否存在
        URL url = null;
        try {
            if (pathUrl != null && new File(pathUrl.toURI()).exists()) {
                url = pathUrl;
            } else {
                url = getClassLoader().getResource(path);
            }
        } catch (URISyntaxException e) {
            LOGGER.info("path:" + pathUrl + " ，URISyntaxException:" + e.getMessage());
        }
        LOGGER.info("path" + ((url == null) ? "(404):" : ":") + pathUrl + " ，url:" + deploymentInfo.getContextUrl());
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
        return runtime.getDispatcherProvider().getRequestDispatcher(this, path);
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
        return runtime.getDispatcherProvider().getNamedDispatcher(this, name);
    }


    @Override
    public void log(String msg) {
        LOGGER.info(msg);
    }


    @Override
    public void log(String message, Throwable throwable) {
        LOGGER.error(message, throwable);
    }

    @Override
    public String getRealPath(String path) {
        if (path == null) {
            return null;
        }
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
        if (name == null) {
            throw new NullPointerException();
        }
        return deploymentInfo.getInitParameters().get(name);
    }

    @Override
    public Enumeration<String> getInitParameterNames() {
        return Collections.enumeration(deploymentInfo.getInitParameters().keySet());
    }

    @Override
    public boolean setInitParameter(String name, String value) {
        if (name == null) {
            throw new NullPointerException();
        }
        checkContextInitializeState();
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
        if (!listeners.isEmpty()) {
            if (oldValue == null) {
                ServletContextAttributeEvent event = new ServletContextAttributeEvent(this, name, object);
                listeners.forEach(listener -> listener.attributeAdded(event));
            } else {
                ServletContextAttributeEvent event = new ServletContextAttributeEvent(this, name, oldValue);
                listeners.forEach(listener -> listener.attributeReplaced(event));
            }
        }
    }

    @Override
    public void removeAttribute(String name) {
        Object value = attributes.remove(name);
        List<ServletContextAttributeListener> listeners = deploymentInfo.getServletContextAttributeListeners();
        ServletContextAttributeEvent event = listeners.isEmpty() ? null : new ServletContextAttributeEvent(this, name, value);
        listeners.forEach(listener -> listener.attributeRemoved(event));
    }

    @Override
    public String getServletContextName() {
        return runtime.getDisplayName();
    }

    public ServletContextRuntime getRuntime() {
        return runtime;
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, String className) {
        try {
            return addServlet(servletName, (Class<? extends Servlet>) getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Servlet servlet) {
        checkServletContextState();
        checkContextInitializeState();
        if (StringUtils.isBlank(servletName)) {
            throw new IllegalArgumentException("servletName is null");
        }

        if (deploymentInfo.getServlets().containsKey(servletName)) {
            return null;
        }

        ServletInfo servletInfo = new ServletInfo();
        servletInfo.setServletName(servletName);
        servletInfo.setServlet(servlet);
        servletInfo.setDynamic(true);
        deploymentInfo.addServlet(servletInfo);
        return new ApplicationServletRegistration(deploymentInfo, servletInfo);
    }

    @Override
    public ServletRegistration.Dynamic addServlet(String servletName, Class<? extends Servlet> servletClass) {
        try {
            return addServlet(servletName, createServlet(servletClass));
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ServletRegistration.Dynamic addJspFile(String servletName, String jspFile) {
        ServletRegistration.Dynamic registration = addServlet(servletName, "org.apache.jasper.servlet.JspServlet");
        registration.setInitParameter("jspFile", jspFile);
        return registration;
    }

    @Override
    public <T extends Servlet> T createServlet(Class<T> clazz) throws ServletException {
        checkServletContextState();
        checkContextInitializeState();
        return newInstance(clazz);
    }

    @Override
    public ServletRegistration getServletRegistration(String servletName) {
        checkContextInitializeState();
        ServletInfo servletInfo = deploymentInfo.getServlets().get(servletName);
        return servletInfo == null ? null : new ApplicationServletRegistration(deploymentInfo, servletInfo);
    }

    @Override
    public Map<String, ? extends ServletRegistration> getServletRegistrations() {
        checkContextInitializeState();
        Map<String, ApplicationServletRegistration> map = new HashMap<>();
        deploymentInfo.getServlets().forEach((servletName, servletInfo) -> {
            map.put(servletName, new ApplicationServletRegistration(deploymentInfo, servletInfo));
        });
        return map;
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, String className) {
        checkServletContextState();
        try {
            return addFilter(filterName, (Class<? extends Filter>) getClassLoader().loadClass(className));
        } catch (ClassNotFoundException e) {
            throw new WrappedRuntimeException(e);
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Filter filter) {
        checkServletContextState();
        checkContextInitializeState();

        if (StringUtils.isBlank(filterName)) {
            throw new IllegalArgumentException("filterName is null or an empty String");
        }
        if (deploymentInfo.getFilters().stream().anyMatch(filterInfo -> filterName.equals(filterInfo.getFilterName()))) {
            return null;
        }
        FilterInfo filterInfo = new FilterInfo();
        filterInfo.setFilter(filter);
        filterInfo.setFilterName(filterName);
        filterInfo.setFilterClass(filter.getClass().getName());
        filterInfo.setDynamic(true);
        deploymentInfo.addFilter(filterInfo);
        return new ApplicationFilterRegistration(filterInfo);
    }

    private void checkServletContextState() {
        if (runtime.isStarted()) {
            throw new IllegalStateException("ServletContext has already been initialized");
        }
    }

    @Override
    public FilterRegistration.Dynamic addFilter(String filterName, Class<? extends Filter> filterClass) {
        try {
            return addFilter(filterName, createFilter(filterClass));
        } catch (ServletException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <T extends Filter> T createFilter(Class<T> clazz) throws ServletException {
        return newInstance(clazz);
    }

    @Override
    public FilterRegistration getFilterRegistration(String filterName) {
        checkContextInitializeState();
        Optional<FilterInfo> optional = deploymentInfo.getFilters().stream().filter(filter -> filterName.equals(filter.getFilterName())).findFirst();
        return optional.map(ApplicationFilterRegistration::new).orElse(null);
    }

    @Override
    public Map<String, ? extends FilterRegistration> getFilterRegistrations() {
        checkContextInitializeState();
        Map<String, ApplicationFilterRegistration> filterMap = new HashMap<>();
        deploymentInfo.getFilters().forEach(filterInfo -> filterMap.put(filterInfo.getFilterName(), new ApplicationFilterRegistration(filterInfo)));
        return filterMap;
    }

    @Override
    public SessionCookieConfig getSessionCookieConfig() {
        checkContextInitializeState();
        return sessionCookieConfig;
    }

    @Override
    public void setSessionTrackingModes(Set<SessionTrackingMode> sessionTrackingModes) {
        checkServletContextState();
        checkContextInitializeState();
        if (sessionTrackingModes.size() > 1 && sessionTrackingModes.contains(SessionTrackingMode.SSL)) {
            throw new IllegalArgumentException();
        }
        this.sessionTrackingModes = sessionTrackingModes;
    }

    @Override
    public Set<SessionTrackingMode> getDefaultSessionTrackingModes() {
        return defaultSessionTrackingModes;
    }

    private void checkContextInitializeState() {
        if (currentInitializeContext != null && currentInitializeContext.isDynamic()) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public Set<SessionTrackingMode> getEffectiveSessionTrackingModes() {
        checkContextInitializeState();
        //The returned set is not backed by the ServletContext object,
        // so changes in the returned set are not reflected in the ServletContext object, and vice-versa.
        return Collections.unmodifiableSet(sessionTrackingModes);
    }

    @Override
    public void addListener(String className) {
        try {
            Class<? extends EventListener> clazz = (Class<? extends EventListener>) getClassLoader().loadClass(className);
            addListener(clazz);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T extends EventListener> void addListener(T listener) {
        checkServletContextState();
        checkListenerClass(listener.getClass());

        if (currentInitializeContext != null) {
            if (ServletContextListener.class.isAssignableFrom(listener.getClass())) {
                if (currentInitializeContext.isDynamic()) {
                    throw new UnsupportedOperationException();
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        LOGGER.info(listener.getClass().getSimpleName() + " listener: " + listener);
        if (ServletContextListener.class.isAssignableFrom(listener.getClass())) {
            deploymentInfo.addServletContextListener((ServletContextListener) listener);
        }
        if (ServletRequestListener.class.isAssignableFrom(listener.getClass())) {
            deploymentInfo.addServletRequestListener((ServletRequestListener) listener);
        }
        if (ServletContextAttributeListener.class.isAssignableFrom(listener.getClass())) {
            deploymentInfo.addServletContextAttributeListener((ServletContextAttributeListener) listener);
        }
        if (HttpSessionListener.class.isAssignableFrom(listener.getClass())) {
            deploymentInfo.addHttpSessionListener((HttpSessionListener) listener);
        }
        if (HttpSessionIdListener.class.isAssignableFrom(listener.getClass())) {
            deploymentInfo.addHttpSessionIdListener((HttpSessionIdListener) listener);
        }
        if (HttpSessionAttributeListener.class.isAssignableFrom(listener.getClass())) {
            deploymentInfo.addSessionAttributeListener((HttpSessionAttributeListener) listener);
        }
        if (ServletRequestAttributeListener.class.isAssignableFrom(listener.getClass())) {
            deploymentInfo.addRequestAttributeListener((ServletRequestAttributeListener) listener);
        }
    }

    @Override
    public void addListener(Class<? extends EventListener> listenerClass) {
        try {
            addListener(createListener(listenerClass));
        } catch (ServletException e) {
            throw new IllegalArgumentException(e);
        }
    }

    @Override
    public <T extends EventListener> T createListener(Class<T> clazz) throws ServletException {
        checkContextInitializeState();
        checkListenerClass(clazz);
        return newInstance(clazz);
    }

    private <T extends EventListener> void checkListenerClass(Class<T> clazz) {
        if (ServletContextListener.class.isAssignableFrom(clazz) || ServletContextAttributeListener.class.isAssignableFrom(clazz) || ServletRequestListener.class.isAssignableFrom(clazz) || ServletRequestAttributeListener.class.isAssignableFrom(clazz) || HttpSessionAttributeListener.class.isAssignableFrom(clazz) || HttpSessionIdListener.class.isAssignableFrom(clazz) || HttpSessionListener.class.isAssignableFrom(clazz)) {
            return;
        }
        throw new IllegalArgumentException();
    }

    private <T> T newInstance(Class<T> clazz) throws ServletException {
        try {
            return clazz.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServletException(e);
        }
    }

    @Override
    public JspConfigDescriptor getJspConfigDescriptor() {
        //todo 暂时 返回 null 使org.apache.jasper.servlet.JasperInitializer#onStartup 可以执行
        return jspConfigDescriptor;
    }

    public void setJspConfigDescriptor(JspConfigDescriptor jspConfigDescriptor) {
        this.jspConfigDescriptor = jspConfigDescriptor;
    }

    @Override
    public ClassLoader getClassLoader() {
        return deploymentInfo.getClassLoader();
    }

    @Override
    public void declareRoles(String... roleNames) {
        checkServletContextState();
        if (roleNames != null) {
            for (String role : roleNames) {
                if (StringUtils.isBlank(role)) {
                    throw new IllegalArgumentException("roleName is null or an empty String");
                }
            }
            deploymentInfo.getSecurityRoles().addAll(Arrays.asList(roleNames));
        }
    }

    @Override
    public String getVirtualServerName() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getSessionTimeout() {
        return deploymentInfo.getSessionTimeout();
    }

    @Override
    public void setSessionTimeout(int sessionTimeout) {
        checkServletContextState();
        checkContextInitializeState();
        deploymentInfo.setSessionTimeout(sessionTimeout);
    }

    @Override
    public String getRequestCharacterEncoding() {
        return requestCharacterEncoding;
    }

    @Override
    public void setRequestCharacterEncoding(String encoding) {
        this.requestCharacterEncoding = encoding;
    }

    @Override
    public String getResponseCharacterEncoding() {
        return responseCharacterEncoding;
    }

    @Override
    public void setResponseCharacterEncoding(String encoding) {
        this.responseCharacterEncoding = encoding;
    }

    public DeploymentInfo getDeploymentInfo() {
        return deploymentInfo;
    }

    public HandlerPipeline getPipeline() {
        return pipeline;
    }

    public void setPipeline(HandlerPipeline pipeline) {
        this.pipeline = pipeline;
    }

    public void setCurrentInitializeContext(ServletContextWrapperListener currentInitializeContext) {
        this.currentInitializeContext = currentInitializeContext;
    }
}
