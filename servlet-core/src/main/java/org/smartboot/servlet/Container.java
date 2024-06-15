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
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerConfiguration;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.conf.DeploymentInfo;
import org.smartboot.servlet.conf.WebAppInfo;
import org.smartboot.servlet.exception.WrappedRuntimeException;
import org.smartboot.servlet.handler.FilterMatchHandler;
import org.smartboot.servlet.handler.HandlerContext;
import org.smartboot.servlet.handler.HandlerPipeline;
import org.smartboot.servlet.handler.ServletMatchHandler;
import org.smartboot.servlet.handler.ServletRequestListenerHandler;
import org.smartboot.servlet.handler.ServletServiceHandler;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.impl.HttpServletResponseImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.plugins.Plugin;

import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Servlet容器运行环境
 *
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2020/12/31
 */
public class Container {
    private static final Logger LOGGER = LoggerFactory.getLogger(Container.class);
    /**
     * http://patorjk.com/software/taag/
     * Font Name: Puffy
     */
    private static final String BANNER = "                               _                                 _           _   \n" + "                              ( )_                              (_ )        ( )_ \n" + "  ___   ___ ___     _ _  _ __ | ,_)     ___    __   _ __  _   _  | |    __  | ,_)\n" + "/',__)/' _ ` _ `\\ /'_` )( '__)| |     /',__) /'__`\\( '__)( ) ( ) | |  /'__`\\| |  \n" + "\\__, \\| ( ) ( ) |( (_| || |   | |_    \\__, \\(  ___/| |   | \\_/ | | | (  ___/| |_ \n" + "(____/(_) (_) (_)`\\__,_)(_)   `\\__)   (____/`\\____)(_)   `\\___/'(___)`\\____)`\\__)";
    public static final String VERSION = "v1.5";
    /**
     * 注册在当前 Servlet 容器中的运行环境
     */
    private final List<ServletContextRuntime> runtimes = new CopyOnWriteArrayList<>();
    /**
     * 注册至当前容器中的插件集
     */
    private final List<Plugin> plugins = new ArrayList<>();
    /**
     * Servlet容器运行环境是否完成启动
     */
    private volatile boolean started = false;

    /**
     * Http服务相关配置
     */
    private HttpServerConfiguration configuration;

    public void start(HttpServerConfiguration configuration) throws Throwable {
        if (started) {
            return;
        }
        started = true;
        this.configuration = configuration;
        configuration.serverName("smart-servlet");
        System.out.println(ConsoleColors.GREEN + BANNER + ConsoleColors.RESET + "\r\n:: smart-servlet :: (" + VERSION + ")");
        HandlerPipeline pipeline = new HandlerPipeline();
        pipeline.next(new ServletServiceHandler() {
            final byte[] line = "欢迎使用 smart-servlet！".getBytes(StandardCharsets.UTF_8);

            @Override
            public void handleRequest(HandlerContext handlerContext) {
                try {
                    ServletResponse response = handlerContext.getResponse();
                    response.setContentLength(line.length);
                    response.getOutputStream().write(line);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        });
        defaultRuntime.getServletContext().setPipeline(pipeline);
        defaultRuntime.start();
        //扫描插件
        loadAndInstallPlugins();

        //启动运行环境
        for (ServletContextRuntime runtime : runtimes) {
            runtime.start();
        }
    }

    /**
     * 加载并安装插件
     */
    private void loadAndInstallPlugins() {
        plugins.add(new Plugin() {
            @Override
            public void onContainerStopped(ServletContextRuntime containerRuntime) {
                LOGGER.info("remove servletContextRuntime:{} from runtimes", containerRuntime.getContextPath());
                runtimes.remove(containerRuntime);
            }
        });
        for (Plugin plugin : ServiceLoader.load(Plugin.class, Container.class.getClassLoader())) {
            LOGGER.debug("load plugin: " + plugin.pluginName());
            plugins.add(plugin);
        }
        //安装插件
        plugins.forEach(plugin -> {
            LOGGER.debug("install plugin: " + plugin.pluginName());
            plugin.install(this);
        });
        runtimes.forEach(runtime -> plugins.forEach(plugin -> plugin.addServletContext(runtime)));
    }

    /**
     * 注册 Servlet 子容器
     *
     * @param runtime Servlet 子容器
     */
    public void addRuntime(ServletContextRuntime runtime) {
        ServletContextRuntime existRuntime = runtimes.stream().filter(containerRuntime -> containerRuntime.getContextPath().equals(runtime.getContextPath())).findFirst().orElse(null);
        if (existRuntime != null) {
            //自定义ROOT Context优先级高于rootRuntime
            throw new IllegalArgumentException("contextPath: " + runtime.getContextPath() + " is already exists!");
        }
        HandlerPipeline pipeline = new HandlerPipeline();
        pipeline.next(new ServletRequestListenerHandler()).next(new ServletMatchHandler()).next(new FilterMatchHandler()).next(new ServletServiceHandler());
        runtime.getServletContext().setPipeline(pipeline);
        runtime.setPlugins(plugins);
        runtime.setContainerRuntime(this);
        runtimes.add(runtime);
        //按contextPath长度倒序,防止被"/"优先匹配
        runtimes.sort((o1, o2) -> o2.getContextPath().length() - o1.getContextPath().length());
        plugins.forEach(plugin -> plugin.addServletContext(runtime));
    }

    /**
     * 注册 Servlet 子容器
     *
     * @param localPath   本地目录
     * @param contextPath 注册的 Context 路径
     * @throws Exception
     */
    public ServletContextRuntime addRuntime(String localPath, String contextPath) throws Exception {
        return addRuntime(localPath, contextPath, Thread.currentThread().getContextClassLoader());
    }

    /**
     * 注册 Servlet 子容器
     *
     * @param localPath         本地目录
     * @param contextPath       注册的 Context 路径
     * @param parentClassLoader 父类加载
     * @throws Exception
     */
    public ServletContextRuntime addRuntime(String localPath, String contextPath, ClassLoader parentClassLoader) throws Exception {
        ServletContextRuntime contextRuntime = getServletRuntime(localPath, contextPath, parentClassLoader);
        addRuntime(contextRuntime);
        return contextRuntime;
    }

    public void doHandle(WebSocketRequest request, WebSocketResponse response) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ServletContextRuntime runtime = matchRuntime(request.getRequestURI());
            if (!runtime.isStarted()) {
                throw new IllegalStateException("container is not started");
            }
            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());
            runtime.getWebsocketProvider().doHandle(request, response);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
    }

    /**
     * 执行 Http 请求
     *
     * @param request
     * @param response
     */
    public void doHandle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        AsyncContext asyncContext = null;
        try {
            //识别请求对应的运行时环境,必然不能为null，要求存在contextPath为"/"的container
            ServletContextRuntime runtime = matchRuntime(request.getRequestURI());
//            if (!runtime.isStarted()) {
//                throw new IllegalStateException("container is not started");
//            }
            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());

            //封装上下文对象
            HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request, runtime, DispatcherType.REQUEST, completableFuture);
            HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(servletRequest, response);
            servletRequest.setHttpServletResponse(servletResponse);
            HandlerContext handlerContext = new HandlerContext(servletRequest, servletResponse, runtime.getServletContext(), false);
            runtime.getVendorProvider().signature(servletResponse);
            // just do it
            servletContext.getPipeline().handleRequest(handlerContext);
            runtime.getSessionProvider().updateAccessTime(servletRequest);
            //输出buffer中的数据
            asyncContext = servletRequest.getInternalAsyncContext();
            if (asyncContext == null) {
                servletResponse.flushServletBuffer();
            }
        } catch (Exception e) {
            throw new WrappedRuntimeException(e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
            if (asyncContext != null) {
                asyncContext.complete();
            } else {
                completableFuture.complete(null);
            }
        }

    }

    public void stop() {
        runtimes.forEach(ServletContextRuntime::stop);
        //卸载插件
        plugins.forEach(Plugin::uninstall);
    }

    public boolean isStarted() {
        return started;
    }

    public HttpServerConfiguration getConfiguration() {
        return configuration;
    }

    private final ServletContextRuntime defaultRuntime = new ServletContextRuntime(null, Thread.currentThread().getContextClassLoader(), "/");

    public ServletContextRuntime matchRuntime(String requestUri) {
        for (ServletContextRuntime matchRuntime : runtimes) {
            String contextPath = matchRuntime.getContextPath();
            if (requestUri.startsWith(contextPath)) {
                return matchRuntime;
            }
        }
        return defaultRuntime;
    }


    private ServletContextRuntime getServletRuntime(String localPath, String contextPath, ClassLoader parentClassLoader) throws Exception {
        WebAppInfo webAppInfo = new WebAppInfo();
        WebXmlParseEngine engine = new WebXmlParseEngine();
        //加载内置的web.xml
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("smart_web.xml")) {
            engine.load(webAppInfo, stream);
        }

        //load web.xml file
        File contextFile = new File(localPath);
        File webXmlFile = new File(contextFile, "WEB-INF" + File.separatorChar + "web.xml");
        if (webXmlFile.isFile()) {
//            LOGGER.info("web.xml info:" + IOUtils.toString(webXmlFile.toURI()));
            try (InputStream inputStream = Files.newInputStream(webXmlFile.toPath())) {
                engine.load(webAppInfo, inputStream);
            }
        }


        URLClassLoader urlClassLoader = getClassLoader(localPath, parentClassLoader);

        //加载web-fragment.xml
        Enumeration<URL> fragments = urlClassLoader.getResources("META-INF/web-fragment.xml");
        while (fragments.hasMoreElements()) {
            try (InputStream inputStream = fragments.nextElement().openStream()) {
                engine.load(webAppInfo, inputStream);
            }
        }
        //new runtime object
        ServletContextRuntime servletRuntime = new ServletContextRuntime(localPath, urlClassLoader, StringUtils.isBlank(contextPath) ? "/" + contextFile.getName() : contextPath);
        servletRuntime.setDisplayName(webAppInfo.getDisplayName());
        servletRuntime.setDescription(webAppInfo.getDescription());
        DeploymentInfo deploymentInfo = servletRuntime.getDeploymentInfo();
        //set session timeout
        deploymentInfo.setSessionTimeout(webAppInfo.getSessionTimeout());
        //register Servlet into deploymentInfo
        webAppInfo.getServlets().values().forEach(deploymentInfo::addServlet);

        webAppInfo.getErrorPages().forEach(deploymentInfo::addErrorPage);

        //register Filter
        webAppInfo.getFilters().values().forEach(deploymentInfo::addFilter);
        //register servletContext into deploymentInfo
        webAppInfo.getContextParams().forEach(deploymentInfo::addInitParameter);

        //register ServletContextListener into deploymentInfo
        webAppInfo.getListeners().forEach(listener -> servletRuntime.getServletContext().addListener(listener));
        deploymentInfo.setDynamicListenerState(true);

        //register filterMapping into deploymentInfo
        webAppInfo.getFilterMappings().forEach(deploymentInfo::addFilterMapping);

        webAppInfo.getLocaleEncodingMappings().forEach(deploymentInfo::addLocaleEncodingMapping);

        webAppInfo.getMimeMappings().forEach((key, value) -> servletRuntime.getServletContext().putMimeTypes(key, value));

        deploymentInfo.setContextUrl(contextFile.toURI().toURL());

        deploymentInfo.setHandlesTypesLoader(new AnnotationsLoader(deploymentInfo.getClassLoader()));
        for (ServletContainerInitializer containerInitializer : ServiceLoader.load(ServletContainerInitializer.class, deploymentInfo.getClassLoader())) {
            LOGGER.info("load ServletContainerInitializer:" + containerInitializer.getClass().getName());
            deploymentInfo.addServletContainerInitializer(containerInitializer);
        }
        // ServletContainerInitializer 可能注解 handlesTypes
//        if (CollectionUtils.isNotEmpty(deploymentInfo.getServletContainerInitializers())) {
//            deploymentInfo.setHandlesTypesLoader(new HandlesTypesLoader(deploymentInfo.getClassLoader()));
//        }

        //默认页面
        //《Servlet3.1规范中文版》10.10 欢迎文件
        // 欢迎文件列表是一个没有尾随或前导/的局部 URL 有序列表
//            for (String welcomeFile : webAppInfo.getWelcomeFileList()) {
//                if (welcomeFile.startsWith("/")) {
//                    throw new IllegalArgumentException("invalid welcome file " + welcomeFile + " is startWith /");
//                } else if (welcomeFile.endsWith("/")) {
//                    throw new IllegalArgumentException("invalid welcome file " + welcomeFile + " is endWith /");
//                }
//            }
        if (webAppInfo.getWelcomeFileList() == null || webAppInfo.getWelcomeFileList().size() == 0) {
            deploymentInfo.setWelcomeFiles(Arrays.asList("index.html", "index.jsp"));
        } else {
            //实际使用中存在"/"开头的情况，将其矫正过来
            List<String> welcomeFiles = new ArrayList<>(webAppInfo.getWelcomeFileList().size());
            webAppInfo.getWelcomeFileList().forEach(file -> {
                if (file.startsWith("/")) {
                    welcomeFiles.add(file.substring(1));
                } else {
                    welcomeFiles.add(file);
                }
            });
            deploymentInfo.setWelcomeFiles(welcomeFiles);
        }
        return servletRuntime;
    }

    private URLClassLoader getClassLoader(String localPath, ClassLoader parentClassLoader) throws MalformedURLException {
        List<URL> list = new ArrayList<>();
        File libDir = new File(localPath, "WEB-INF" + File.separator + "lib/");
        if (libDir.isDirectory()) {
            File[] files = libDir.listFiles();
            if (files != null && files.length > 0) {
                for (File file : files) {
                    list.add(file.toURI().toURL());
                }
            }
        }
        //list.sort((o1, o2) -> o2.toString().compareTo(o1.toString()));

        File classDir = new File(localPath, "WEB-INF" + File.separator + "classes/");
        if (classDir.isDirectory()) {
            list.add(classDir.toURI().toURL());
        }
        URL[] urls = new URL[list.size()];
        list.toArray(urls);
        return new URLClassLoader(urls, parentClassLoader);
    }

    static class ConsoleColors {
        /**
         * 重置颜色
         */
        public static final String RESET = "\033[0m";
        /**
         * 蓝色
         */
        public static final String BLUE = "\033[34;1m";

        /**
         * 红色
         */
        public static final String RED = "\033[31m";

        /**
         * 绿色
         */
        public static final String GREEN = "\033[32;1m";

    }
}
