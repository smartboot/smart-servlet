/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.sandbox;

import jakarta.servlet.AsyncContext;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.websocket.ClientEndpointConfig;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.Extension;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpointConfig;
import tech.smartboot.feat.core.common.HttpMethod;
import tech.smartboot.feat.core.common.enums.HeaderNameEnum;
import tech.smartboot.feat.core.common.enums.HttpStatus;
import tech.smartboot.feat.core.common.utils.StringUtils;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.WebSocketServerContainer;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.impl.ServletContextImpl;
import tech.smartboot.servlet.plugins.PluginException;
import tech.smartboot.servlet.provider.AsyncContextProvider;
import tech.smartboot.servlet.provider.DispatcherProvider;
import tech.smartboot.servlet.provider.FaviconProvider;
import tech.smartboot.servlet.provider.VendorProvider;
import tech.smartboot.servlet.provider.WebsocketProvider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

final class MockProvider implements VendorProvider, WebsocketProvider, FaviconProvider, DispatcherProvider, AsyncContextProvider {
    public static final MockProvider INSTANCE = new MockProvider();

    @Override
    public void signature(HttpServletResponse response) {
        response.addHeader("X-Powered-By", "smartboot");
        response.addHeader("X-Version", Container.VERSION);
        response.addHeader("X-System", getBasicInfo());
        response.addHeader("X-Open-Source", "https://gitee.com/smartboot/smart-servlet");
        response.addHeader("X-Tip", "The current version is not authorized.");
    }

    private String getBasicInfo() {
        return "Java_" + System.getProperty("java.version") + "/" + System.getProperty("os.name") + "_" + System.getProperty("os.arch") + "_" + System.getProperty("os.version") + "/" + Runtime.getRuntime().availableProcessors() + "C" + (int) (Runtime.getRuntime().maxMemory() * 1.0 / 1024 / 1024 / 1024 + 0.5) + "G";
    }

    @Override
    public WebSocketServerContainer getWebSocketServerContainer() {
        return new WebSocketServerContainer() {

            @Override
            public void addEndpoint(Class<?> endpointClass) throws DeploymentException {

            }

            @Override
            public void addEndpoint(ServerEndpointConfig serverConfig) throws DeploymentException {

            }

            @Override
            public void upgradeHttpToWebSocket(Object o, Object o1, ServerEndpointConfig serverEndpointConfig, Map<String, String> map) throws IOException, DeploymentException {
                throw new PluginException(SandBox.UPGRADE_MESSAGE_ZH);
            }

            @Override
            public long getDefaultAsyncSendTimeout() {
                return 0;
            }

            @Override
            public void setAsyncSendTimeout(long timeoutmillis) {

            }

            @Override
            public Session connectToServer(Object annotatedEndpointInstance, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Class<?> annotatedEndpointClass, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Endpoint endpointInstance, ClientEndpointConfig cec, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public Session connectToServer(Class<? extends Endpoint> endpointClass, ClientEndpointConfig cec, URI path) throws DeploymentException, IOException {
                return null;
            }

            @Override
            public long getDefaultMaxSessionIdleTimeout() {
                return 0;
            }

            @Override
            public void setDefaultMaxSessionIdleTimeout(long timeout) {

            }

            @Override
            public int getDefaultMaxBinaryMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxBinaryMessageBufferSize(int max) {

            }

            @Override
            public int getDefaultMaxTextMessageBufferSize() {
                return 0;
            }

            @Override
            public void setDefaultMaxTextMessageBufferSize(int max) {

            }

            @Override
            public Set<Extension> getInstalledExtensions() {
                return Collections.emptySet();
            }
        };
    }


    @Override
    public void resister(ServletContextRuntime runtime) {
        ServletRegistration.Dynamic dynamic = runtime.getServletContext().addServlet("aa", new FaviconServlet());
        dynamic.addMapping("/favicon.ico");
    }

    static class FaviconServlet extends HttpServlet {
        private byte[] faviconBytes = null;
        private long faviconModifyTime;
        private final ThreadLocal<SimpleDateFormat> sdf = ThreadLocal.withInitial(() -> new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH));

        @Override
        public void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            if (faviconBytes != null) {
                String requestModified = req.getHeader(HeaderNameEnum.IF_MODIFIED_SINCE.getName());
                try {
                    if (StringUtils.isNotBlank(requestModified) && faviconModifyTime <= sdf.get().parse(requestModified).getTime()) {
                        resp.sendError(HttpStatus.NOT_MODIFIED.value(), HttpStatus.NOT_MODIFIED.getReasonPhrase());
                        return;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                loadDefaultFavicon();
            }

            resp.setHeader(HeaderNameEnum.LAST_MODIFIED.getName(), sdf.get().format(new Date(faviconModifyTime)));
            resp.setContentType("image/x-icon");
            //HEAD不输出内容
            if (HttpMethod.HEAD.equals(req.getMethod())) {
                return;
            }
            resp.setContentLength(faviconBytes.length);
            resp.getOutputStream().write(faviconBytes);
        }

        private void loadDefaultFavicon() {
            if (faviconBytes != null) {
                return;
            }
            InputStream inputStream = null;
            try {
                inputStream = MockProvider.class.getClassLoader().getResourceAsStream("favicon.ico");
                if (inputStream != null) {
                    faviconBytes = new byte[inputStream.available()];
                    inputStream.read(faviconBytes);
                    faviconModifyTime = System.currentTimeMillis() / 1000 * 1000;
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public RequestDispatcher getRequestDispatcher(ServletContextImpl servletContext, String path) {
        throw new PluginException("Please install the [dispatcher] plugin to enable the [getRequestDispatcher] function");
    }

    @Override
    public RequestDispatcher getNamedDispatcher(ServletContextImpl servletContext, String name) {
        throw new PluginException("Please install the [dispatcher] plugin to enable the [getNamedDispatcher] function");
    }

    @Override
    public RequestDispatcher getRequestDispatcher(HttpServletRequestImpl servletContext, String path) {
        throw new PluginException("Please install the [dispatcher] plugin to enable the [getRequestDispatcher] function");
    }

    @Override
    public AsyncContext startAsync(HttpServletRequestImpl request, ServletRequest servletRequest, ServletResponse servletResponse, AsyncContext asyncContext) throws IllegalStateException {
        throw new PluginException(SandBox.UPGRADE_MESSAGE_ZH);
    }
}
