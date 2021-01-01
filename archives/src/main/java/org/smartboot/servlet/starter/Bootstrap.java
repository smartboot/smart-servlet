package org.smartboot.servlet.starter;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.HttpRequest;
import org.smartboot.http.HttpResponse;
import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.server.handle.HttpHandle;
import org.smartboot.servlet.ContainerRuntime;

import java.io.File;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class Bootstrap {
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
                RunLogger.getLogger().log(Level.FINE, "start load: " + path.getAbsolutePath());
                containerRuntime.addRuntime(path.getAbsolutePath(), "/" + path.getName());
                RunLogger.getLogger().log(Level.FINE, "load servlet container: /" + path.getName() + " success!");
            }
        }
        containerRuntime.start();
        final HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(new HttpHandle() {
            @Override
            public void doHandle(HttpRequest request, HttpResponse response) {
                containerRuntime.doHandle(request, response);
            }
        });
        bootstrap.setBannerEnabled(false);
        bootstrap.setBufferPool(1024 * 1024 * 10, Runtime.getRuntime().availableProcessors(), 1024 * 4);
        bootstrap.setReadBufferSize(1024 * 4).setPort(8080).start();
        System.out.println("启动成功,耗时：" + (System.currentTimeMillis() - start) + "ms");
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            containerRuntime.stop();
            bootstrap.shutdown();
        }));
    }
}
