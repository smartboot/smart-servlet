/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.FilterInfo;
import org.smartboot.servlet.conf.ServletContainerInitializerInfo;
import org.smartboot.servlet.conf.ServletInfo;
import org.smartboot.servlet.impl.FilterConfigImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.impl.ServletContextWrapperListener;
import org.smartboot.servlet.plugins.Plugin;
import org.smartboot.servlet.provider.AsyncContextProvider;
import org.smartboot.servlet.provider.DispatcherProvider;
import org.smartboot.servlet.provider.FaviconProvider;
import org.smartboot.servlet.provider.SessionProvider;
import org.smartboot.servlet.provider.VendorProvider;
import org.smartboot.servlet.provider.WebsocketProvider;
import org.smartboot.servlet.sandbox.SandBox;

import javax.servlet.Filter;
import javax.servlet.FilterConfig;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * 应用级子容器的运行时环境
 *
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletContextRuntime {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletContextRuntime.class);
    private String displayName;
    private String description;
    /**
     * 容器部署信息
     */
    private final DeploymentInfo deploymentInfo;
    /**
     * 服务上下文
     */
    private final ServletContextImpl servletContext;
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
    private SessionProvider sessionProvider;

    /**
     * Websocket服务提供者
     */
    private WebsocketProvider websocketProvider = SandBox.INSTANCE.getWebsocketProvider();

    private VendorProvider vendorProvider = SandBox.INSTANCE.getVendorProvider();
    private AsyncContextProvider asyncContextProvider = SandBox.INSTANCE.getAsyncContextProvider();
    private FaviconProvider faviconProvider = SandBox.INSTANCE.getFaviconProvider();
    /**
     * 关联至本运行环境的插件集合
     */
    private List<Plugin> plugins = Collections.emptyList();
    /**
     * 子容器是否已启动
     */
    private boolean started = false;

    private final String localPath;
    private Container container;

//    public ServletContextRuntime(String contextPath) {
//        this(null, Thread.currentThread().getContextClassLoader(), contextPath);
//    }

    public ServletContextRuntime(String localPath, ClassLoader classLoader, String contextPath) {
        this.localPath = localPath;
        if (StringUtils.isBlank(contextPath)) {
            this.contextPath = "/";
        } else {
            this.contextPath = contextPath;
        }
        deploymentInfo = new DeploymentInfo(classLoader);
        servletContext = new ServletContextImpl(this);
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
    public void start() throws Throwable {
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
            for (ServletContextWrapperListener wrapperListener : deploymentInfo.getServletContextListeners()) {
                servletContext.setCurrentInitializeContext(wrapperListener);
                ServletContextEvent event = new ServletContextEvent(servletContext);
                wrapperListener.getListener().contextInitialized(event);
            }
            servletContext.setCurrentInitializeContext(null);

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
            throw e;
        } finally {
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    private void newServletsInstance(DeploymentInfo deploymentInfo) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
        for (ServletInfo servletInfo : deploymentInfo.getServlets().values()) {
            if (!servletInfo.isDynamic()) {
                if (servletInfo.getJspFile() != null) {
                    LOGGER.error("unSupport jsp");
                    servletInfo.setServlet(new DefaultServlet(deploymentInfo));
                    servletInfo.addInitParam("jspFile", servletInfo.getJspFile());
                    continue;
                }
                Servlet servlet = (Servlet) deploymentInfo.getClassLoader().loadClass(servletInfo.getServletClass()).newInstance();
                servletInfo.setServlet(servlet);
            }
        }
        //绑定 default Servlet
        if (!deploymentInfo.getServlets().containsKey(ServletInfo.DEFAULT_SERVLET_NAME)) {
            ServletInfo servletInfo = new ServletInfo();
            servletInfo.setServletName(ServletInfo.DEFAULT_SERVLET_NAME);
            servletInfo.setServlet(new DefaultServlet(deploymentInfo));
            servletInfo.setLoadOnStartup(1);
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
            deploymentInfo.getHandlesTypesLoader().scanAnnotations();
            deploymentInfo.getHandlesTypesLoader().getInitializerClassMap().forEach((servletContainerInitializer, handlesTypes) -> {
                ServletContainerInitializerInfo initializerInfo = new ServletContainerInitializerInfo(servletContainerInitializer, handlesTypes);
                deploymentInfo.getServletContainerInitializers().add(initializerInfo);
            });
            deploymentInfo.getHandlesTypesLoader().getAnnotations(WebListener.class).forEach(listener -> {
                System.out.println(listener);
                servletContext.addListener(listener);
            });
            deploymentInfo.getHandlesTypesLoader().getServlets().values().forEach(servletInfo -> {
                ServletInfo webXmlInfo = deploymentInfo.getServlets().get(servletInfo.getServletName());
                if (webXmlInfo != null) {
                    servletInfo.getInitParams().forEach(webXmlInfo::addInitParam);
                } else {
                    deploymentInfo.addServlet(servletInfo);
                }
            });
            deploymentInfo.getHandlesTypesLoader().clear();
            deploymentInfo.setHandlesTypesLoader(null);
//            System.out.println("scanHandleTypes use :" + (System.currentTimeMillis() - start));
        }
//        ServletInfo jspServletInfo = new ServletInfo();
//        jspServletInfo.setServletClass(JspServlet.class.getName());
//        jspServletInfo.setServletName("jsp");
//        jspServletInfo.addMapping("*.jsp");
//        deploymentInfo.addServlet(jspServletInfo);
        faviconProvider.resister(this);

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
            //load-on-startup 元素表示该 servlet 应该在 Web 应用程序启动时加载（实例化并调用它的 init()方法）。
            // 该元素的元素内容必须是一个整数，表示 servlet 应该被加载的顺序。
            // 如果该值是一个负整数，或不存在该元素，容器自由选择什么时候加载这个 servlet。
            // 如果该值是一个正整数或 0，当应用部署后容器必须加载和初始化这个 servlet。容器必须保证较小整数标记的 servlet 在较大整数标记的 servlet 之前加载。
            // 容器可以选择具有相同 load-on-startup值的 servlet 的加载顺序。
            //PS：对于loadOnStartup为负数的情况需要延迟初始化，不然spring项目在某些情况下会出问题
            if (servletInfo.getLoadOnStartup() > 0) {
                servletInfo.init(servletContext);
            }
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
        deploymentInfo.getServletContextListeners().forEach(servletContextListener -> servletContextListener.getListener().contextDestroyed(event));

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


    public WebsocketProvider getWebsocketProvider() {
        return websocketProvider;
    }

    public void setWebsocketProvider(WebsocketProvider websocketProvider) {
        this.websocketProvider = websocketProvider;
    }

    public VendorProvider getVendorProvider() {
        return vendorProvider;
    }

    public void setVendorProvider(VendorProvider vendorProvider) {
        this.vendorProvider = vendorProvider;
    }

    public AsyncContextProvider getAsyncContextProvider() {
        return asyncContextProvider;
    }

    public void setAsyncContextProvider(AsyncContextProvider asyncContextProvider) {
        this.asyncContextProvider = asyncContextProvider;
    }

    public FaviconProvider getFaviconProvider() {
        return faviconProvider;
    }

    public void setFaviconProvider(FaviconProvider faviconProvider) {
        this.faviconProvider = faviconProvider;
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

    public Container getContainerRuntime() {
        return container;
    }

    public void setContainerRuntime(Container container) {
        this.container = container;
    }
}
