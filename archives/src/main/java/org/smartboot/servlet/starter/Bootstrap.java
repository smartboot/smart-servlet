package org.smartboot.servlet.starter;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.server.HttpBootstrap;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandle;
import org.smartboot.http.server.WebSocketHandle;
import org.smartboot.http.server.WebSocketRequest;
import org.smartboot.http.server.WebSocketResponse;
import org.smartboot.servlet.ContainerRuntime;

import java.io.File;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class Bootstrap {
    private static final Logger LOGGER = LoggerFactory.getLogger(Bootstrap.class);

    public static void main(String[] args) throws Exception {
        String webapps = System.getProperty("webapps.dir");
        long start = System.currentTimeMillis();
        if (webapps == null) {
            webapps = new File("archives/webapps").getAbsolutePath();
        }
        ContainerRuntime containerRuntime = new ContainerRuntime();
        File file = new File(webapps);
        if (file.isDirectory()) {
            for (File path : file.listFiles()) {
                LOGGER.info("start load: " + path.getAbsolutePath());
                containerRuntime.addRuntime(path.getAbsolutePath(), "/" + path.getName());
                LOGGER.info("load servlet container: /" + path.getName() + " success!");
            }
        }
        containerRuntime.start();
        final HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(new HttpServerHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) {
                containerRuntime.doHandle(request, response);
            }
        });
        bootstrap.wsPipeline().next(new WebSocketHandle() {
            @Override
            public void doHandle(WebSocketRequest request, WebSocketResponse response) throws IOException {
                containerRuntime.doHandle(request, response);
            }
        });
        bootstrap.setPort(8080).configuration().bannerEnabled(false);
        bootstrap.start();
        System.out.println("启动成功,耗时：" + (System.currentTimeMillis() - start) + "ms");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            containerRuntime.stop();
            bootstrap.shutdown();
        }));
    }
}
