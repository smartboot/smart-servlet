/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletResponse;
import tech.smartboot.feat.core.common.exception.HttpException;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;
import tech.smartboot.feat.core.common.utils.StringUtils;
import tech.smartboot.feat.core.server.HttpRequest;
import tech.smartboot.feat.core.server.HttpResponse;
import tech.smartboot.feat.core.server.WebSocketRequest;
import tech.smartboot.feat.core.server.WebSocketResponse;
import tech.smartboot.servlet.conf.DeploymentInfo;
import tech.smartboot.servlet.conf.FilterInfo;
import tech.smartboot.servlet.conf.OrderMeta;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.conf.WebAppInfo;
import tech.smartboot.servlet.conf.WebFragmentInfo;
import tech.smartboot.servlet.exception.WrappedRuntimeException;
import tech.smartboot.servlet.handler.FilterMatchHandler;
import tech.smartboot.servlet.handler.HandlerContext;
import tech.smartboot.servlet.handler.HandlerPipeline;
import tech.smartboot.servlet.handler.SecurityHandler;
import tech.smartboot.servlet.handler.ServletRequestListenerHandler;
import tech.smartboot.servlet.handler.ServletServiceHandler;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.impl.HttpServletResponseImpl;
import tech.smartboot.servlet.impl.ServletContextImpl;
import tech.smartboot.servlet.plugins.Plugin;
import tech.smartboot.servlet.util.CommonUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
//    private static final String BANNER = """
//                                           _                                 _           _  \s
//                                          ( )_                              (_ )        ( )_\s
//              ___   ___ ___     _ _  _ __ | ,_)     ___    __   _ __  _   _  | |    __  | ,_)
//            /',__)/' _ ` _ `\\ /'_` )( '__)| |     /',__) /'__`\\( '__)( ) ( ) | |  /'__`\\| | \s
//            \\__, \\| ( ) ( ) |( (_| || |   | |_    \\__, \\(  ___/| |   | \\_/ | | | (  ___/| |_\s
//            (____/(_) (_) (_)`\\__,_)(_)   `\\__)   (____/`\\____)(_)   `\\___/'(___)`\\____)`\\__)
//            """;
    public static final String VERSION = "v2.6";
    public static final String CONFIGURATION_FILE = "smart-servlet.properties";
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
    private final ContainerConfig configuration = new ContainerConfig();

    public void initialize() throws Throwable {
        if (started) {
            return;
        }
        started = true;


        //设置默认
        if (runtimes.stream().noneMatch(runtime -> "/".equals(runtime.getContextPath()))) {
            ServletContextRuntime defaultRuntime = new ServletContextRuntime(null, Thread.currentThread().getContextClassLoader(), "/");
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
            addRuntime(defaultRuntime);
        }
        //扫描插件
        loadAndInstallPlugins();

        //启动运行环境
        for (ServletContextRuntime runtime : runtimes) {
            runtime.start();
        }
    }

    public void start() {
        for (int i = 0; i < 100; i++) {
            System.out.print('=');
        }
        System.out.println();

        System.out.println(ConsoleColors.GREEN + CommonUtil.getResourceAsString("smart-servlet/banner.txt") + ConsoleColors.RESET + "\r\n:: smart-servlet :: (" + VERSION + ")");
        for (int i = 0; i < 26; i++) {
            System.out.print('~');
        }
        System.out.println();
        plugins.forEach(plugin -> plugin.onContainerInitialized(this));
        for (int i = 0; i < 100; i++) {
            System.out.print('=');
        }
        System.out.println();
    }

    /**
     * 加载并安装插件
     */
    private void loadAndInstallPlugins() {
        plugins.add(new Plugin() {
            @Override
            public void onServletContextStopped(ServletContextRuntime containerRuntime) {
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
        pipeline.next(new SecurityHandler() {
            @Override
            public void handleRequest(HandlerContext handlerContext) throws ServletException, IOException {
                if (handlerContext.getServletInfo() == null) {
                    throw new ServletException("servlet is null");
                }
                if (!handlerContext.getServletInfo().initialized()) {
                    handlerContext.getServletInfo().init(handlerContext.getServletContext());
                }
                doNext(handlerContext);
            }
        }).next(new ServletRequestListenerHandler()).next(new FilterMatchHandler()).next(new SecurityHandler()).next(new ServletServiceHandler());
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
//            if(true){
//                throw new HttpException(HttpStatus.FORBIDDEN);
//            }
            ServletContextImpl servletContext = runtime.getServletContext();
            Thread.currentThread().setContextClassLoader(servletContext.getClassLoader());

            //封装上下文对象
            HttpServletRequestImpl servletRequest = new HttpServletRequestImpl(request, runtime, completableFuture);
            HttpServletResponseImpl servletResponse = new HttpServletResponseImpl(servletRequest, response);
            servletRequest.setHttpServletResponse(servletResponse);
            HandlerContext handlerContext = new HandlerContext(servletRequest, servletResponse, runtime.getServletContext(), false);
            ServletMappingInfo servletMappingInfo = runtime.getMappingProvider().matchServlet(servletRequest.getRequestURI());
            handlerContext.setServletInfo(runtime.getDeploymentInfo().getServlets().get(servletMappingInfo.getServletName()));
            servletRequest.setServletMappingInfo(servletMappingInfo);
            runtime.getVendorProvider().signature(servletResponse);
            // just do it
            runtime.getSessionProvider().pauseAccessTime(servletRequest);
            servletContext.getPipeline().handleRequest(handlerContext);
            runtime.getSessionProvider().updateAccessTime(servletRequest);
            //输出buffer中的数据
            asyncContext = servletRequest.getInternalAsyncContext();
            if (asyncContext == null) {
                servletResponse.flushServletBuffer();
                //为了适配tck 的测试, 需要关闭连接
                if (response.getOutputStream().isChunkedSupport()) {
                    response.close();
                }
            }
        } catch (HttpException e) {
            try {
                ByteArrayOutputStream stack = new ByteArrayOutputStream();
                PrintWriter printWriter = new PrintWriter(stack);
                e.printStackTrace(printWriter);
                printWriter.close();
                response.setHttpStatus(e.getHttpStatus());
                OutputStream outputStream = response.getOutputStream();
                String resp = CommonUtil.getResourceAsString("smart-servlet/error.html");
                StringBuilder sb = new StringBuilder(resp);
                int index = sb.indexOf("{{statusCode}}");
                if (index != -1) {
                    sb.replace(index, index + "{{statusCode}}".length(), String.valueOf(e.getHttpStatus().value()));
                }
                index = sb.indexOf("{{statusDesc}}");
                if (index != -1) {
                    sb.replace(index, index + "{{statusDesc}}".length(), e.getHttpStatus().getReasonPhrase());
                }
                index = sb.indexOf("{{stackTrace}}");
                if (index != -1) {
                    sb.replace(index, index + "{{stackTrace}}".length(), stack.toString().replaceAll("\n", "<br/>").replaceAll(" ", "&nbsp;"));
                }
                index = sb.indexOf("{{version}}");
                if (index != -1) {
                    sb.replace(index, index + "{{version}}".length(), VERSION);
                }
                outputStream.write(sb.toString().getBytes());
            } catch (IOException ignore) {
                LOGGER.warn("HttpError response exception", e);
            } finally {
                response.close();
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
        plugins.forEach(plugin -> plugin.uninstall(this));
    }

    public boolean isStarted() {
        return started;
    }

    public ServletContextRuntime matchRuntime(String requestUri) {
        for (ServletContextRuntime matchRuntime : runtimes) {
            String contextPath = matchRuntime.getContextPath();
            if (requestUri.startsWith(contextPath)) {
                return matchRuntime;
            }
        }
        throw new IllegalArgumentException();
    }


    private ServletContextRuntime getServletRuntime(String localPath, String contextPath, ClassLoader parentClassLoader) throws Exception {
        URLClassLoader urlClassLoader = getClassLoader(localPath, parentClassLoader);
        File contextFile = new File(localPath);
        ServletContextRuntime servletRuntime = new ServletContextRuntime(localPath, urlClassLoader, StringUtils.isBlank(contextPath) ? "/" + contextFile.getName() : contextPath);

        WebAppInfo webAppInfo = new WebAppInfo();
        WebXmlParseEngine engine = new WebXmlParseEngine();
        //加载内置的web.xml
        try (InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("smart_web.xml")) {
            engine.load(webAppInfo, stream);
        }

        //load web.xml file
        File webXmlFile = new File(contextFile, "WEB-INF" + File.separatorChar + "web.xml");
        if (webXmlFile.isFile()) {
//            LOGGER.info("web.xml info:" + IOUtils.toString(webXmlFile.toURI()));
            try (InputStream inputStream = Files.newInputStream(webXmlFile.toPath())) {
                engine.load(webAppInfo, inputStream);
            }
        }

        File sunWebXmlFile = new File(contextFile, "WEB-INF" + File.separatorChar + "sun-web.xml");
        if (sunWebXmlFile.isFile()) {
//            LOGGER.info("web.xml info:" + IOUtils.toString(webXmlFile.toURI()));
            try (InputStream inputStream = Files.newInputStream(sunWebXmlFile.toPath())) {
                engine.load(webAppInfo, inputStream);
            }
        }

        //加载web-fragment.xml
        if (webAppInfo.getAbsoluteOrdering() != null) {
            Map<String, URL> fragmentMap = new HashMap<>();
            Enumeration<URL> fragments = urlClassLoader.getResources("META-INF/web-fragment.xml");
            while (fragments.hasMoreElements()) {
                URL url = fragments.nextElement();
                try (InputStream inputStream = url.openStream()) {
                    String name = engine.parseFragmentName(inputStream);
                    fragmentMap.put(name, url);
                }
            }
            for (var fragment : webAppInfo.getAbsoluteOrdering()) {
                URL url = fragmentMap.get(fragment);
                if (url == null) {
                    continue;
                }
                try (InputStream inputStream = url.openStream()) {
                    engine.loadFragment(webAppInfo, inputStream);
                }
            }
        } else {
            Enumeration<URL> fragments = urlClassLoader.getResources("META-INF/web-fragment.xml");
            Map<String, WebFragmentInfo> fragmentInfos = new HashMap<>();
            List<OrderMeta> list = new ArrayList<>();
            while (fragments.hasMoreElements()) {
                URL url = fragments.nextElement();
                try (InputStream inputStream = url.openStream()) {
                    OrderMeta orderMeta = engine.parseFragmentRelativeOrdering(inputStream);
                    orderMeta.setUrl(url);
                    list.add(orderMeta);
                }
            }
            list.sort((o1, o2) -> {
                if (o1.getBefore() != null && o1.getBefore().contains(o2.getName())) {
                    return -1;
                }
                if (o1.isBeforeOthers()) {
                    return -1;
                }
                if (o1.isAfterOthers()) {
                    return 1;
                }
                if (o1.getAfter() != null && o1.getAfter().contains(o2.getName())) {
                    return 1;
                }
                return 0;
            });
            for (var order : list) {
                try (InputStream inputStream = order.getUrl().openStream()) {
                    engine.loadFragment(webAppInfo, inputStream);
                }
            }
        }


        //new runtime object
        servletRuntime.setDisplayName(webAppInfo.getDisplayName());
        servletRuntime.setDescription(webAppInfo.getDescription());
        DeploymentInfo deploymentInfo = servletRuntime.getDeploymentInfo();
        //set session timeout
        deploymentInfo.setSessionTimeout(webAppInfo.getSessionTimeout());
        deploymentInfo.setLoginConfig(webAppInfo.getLoginConfig());
        if (StringUtils.isNotBlank(webAppInfo.getVersion())) {
            String[] array = webAppInfo.getVersion().split("\\.");
            if (array.length == 2) {
                deploymentInfo.setEffectiveMajorVersion(Integer.parseInt(array[0]));
                deploymentInfo.setEffectiveMinorVersion(Integer.parseInt(array[1]));
            }
        }
//        if (webAppInfo.getLoginConfig() != null && webAppInfo.getLoginConfig().getLoginPage().endsWith(".jsp")) {
//            servletRuntime.getServletContext().addJspFile("aaaaaa", webAppInfo.getLoginConfig().getLoginPage());
//        }
        //register Servlet into deploymentInfo
        webAppInfo.getServletMappings().forEach(deploymentInfo::addServletMapping);
        webAppInfo.getServlets().values().forEach(deploymentInfo::addServlet);

        webAppInfo.getErrorPages().forEach(deploymentInfo::addErrorPage);

        //register Filter
        webAppInfo.getFilterMappingInfos().forEach(deploymentInfo::addFilterMapping);
        for (FilterInfo filterInfo : webAppInfo.getFilters()) {
            deploymentInfo.addFilter(filterInfo);
        }
        //register servletContext into deploymentInfo
        webAppInfo.getContextParams().forEach(deploymentInfo::addInitParameter);

        //register ServletContextListener into deploymentInfo
        for (String listener : webAppInfo.getListeners()) {
            Class<? extends EventListener> clazz = (Class<? extends EventListener>) servletRuntime.getServletContext().getClassLoader().loadClass(listener);
            servletRuntime.getServletContext().addListener0(clazz.newInstance(), false);
        }

        webAppInfo.getLocaleEncodingMappings().forEach(deploymentInfo::addLocaleEncodingMapping);

        webAppInfo.getMimeMappings().forEach((key, value) -> servletRuntime.getServletContext().putMimeTypes(key, value));

        webAppInfo.getSecurityConstraints().forEach(deploymentInfo::addSecurityConstraint);
        deploymentInfo.getSecurityRoleMapping().putAll(webAppInfo.getSecurityRoleMapping());

        deploymentInfo.setContextUrl(contextFile.toURI().toURL());

        //如果 web.xml 描述符中的 metadata-complete 元素设置为 true，
        // 将不会处理在 class 文件和绑定在 jar 包中的 web-fragments 中的注解
        if (!webAppInfo.isMetadataComplete() && webAppInfo.getAbsoluteOrdering() == null) {
            deploymentInfo.setHandlesTypesLoader(new AnnotationsLoader(deploymentInfo.getClassLoader()));
        }

        for (ServletContainerInitializer containerInitializer : ServiceLoader.load(ServletContainerInitializer.class, deploymentInfo.getClassLoader())) {
            LOGGER.info("load ServletContainerInitializer:" + containerInitializer.getClass().getName());
            deploymentInfo.addServletContainerInitializer(containerInitializer);
        }
//        deploymentInfo.setDynamicListenerState(true);
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
        if (webAppInfo.getWelcomeFileList() == null || webAppInfo.getWelcomeFileList().isEmpty()) {
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

    public ContainerConfig getConfiguration() {
        return configuration;
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
