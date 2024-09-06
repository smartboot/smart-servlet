package tech.smartboot.servlet.plugins.tls;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.ParamReflect;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.impl.Request;
import org.smartboot.socket.extension.plugins.SslPlugin;
import org.smartboot.socket.extension.ssl.factory.PemServerSSLContextFactory;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.plugins.Plugin;

import java.io.InputStream;

/**
 *
 */
public class TlsPlugin extends Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(TlsPlugin.class);
    private HttpBootstrap bootstrap;
    private SSLConfig sslConfig;

    @Override
    public void initPlugin(Container containerRuntime) {
        super.initPlugin(containerRuntime);


        try {
            sslConfig = new SSLConfig();
            try (InputStream fileInputStream = getResource("smart-servlet.properties")) {
                if (fileInputStream != null) {
                    ParamReflect.reflect(fileInputStream, sslConfig);
                }
            }
            if (!sslConfig.isEnable()) {
                LOGGER.debug("tls is disabled!");
                return;
            }
            SslPlugin<Request> sslPlugin;
            switch (sslConfig.getType()) {
                case "pem":
                    try (InputStream pemStream = getResource("smart-servlet.pem")) {
                        sslPlugin = new SslPlugin<>(new PemServerSSLContextFactory(pemStream));
                    }

                    break;
                case "jks":
//                    sslPlugin = new SslPlugin<>(new ServerSSLContextFactory(Files.newInputStream(new File(getServletHome(), "conf/smart-servlet.keystore").toPath()), "123456", "123456"), ClientAuth.NONE);
//                    break;
                default:
                    throw new UnsupportedOperationException("无效证书类型");
            }
//            sslPlugin.debug(true);
            bootstrap = new HttpBootstrap();
            bootstrap.httpHandler(containerRuntime.getConfiguration().getHttpServerHandler());
            bootstrap.setPort(sslConfig.getPort()).configuration().addPlugin(sslPlugin).group(containerRuntime.getConfiguration().group()).readBufferSize(sslConfig.getReadBufferSize()).bannerEnabled(false);
            bootstrap.start();

        } catch (Exception e) {
            sslConfig = null;
            bootstrap.shutdown();
            bootstrap = null;
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onContainerInitialized(Container container) {
        if (sslConfig != null) {
            System.out.println("\033[1mTLS Plugin:\033[0m");
            System.out.println("\tTLS enabled, port:" + sslConfig.getPort());
        }
    }

    @Override
    protected void destroyPlugin() {
        if (bootstrap != null) {
            bootstrap.shutdown();
        }
    }

    @Override
    public String pluginName() {
        return "tls";
    }
}
