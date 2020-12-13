package org.smartboot.servlet.starter;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.http.logging.RunLogger;
import org.smartboot.servlet.ServletHttpHandle;
import org.smartboot.servlet.war.WebContextRuntime;

import java.io.File;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class Bootstrap {
    public static void main(String[] args) throws Exception {
        String webapps = System.getProperty("config");
        long start = System.currentTimeMillis();
        if (webapps == null) {
            webapps = new File("archives/webapps").getAbsolutePath();
        }
        final ServletHttpHandle httpHandle = new ServletHttpHandle();
        File file = new File(webapps);
        if (file.isDirectory()) {
            for (File path : file.listFiles()) {
                RunLogger.getLogger().log(Level.FINE, "start load: " + path.getAbsolutePath());
                WebContextRuntime webContextRuntime = new WebContextRuntime(path.getAbsolutePath(), "/" + path.getName());
                httpHandle.addRuntime(webContextRuntime.getServletRuntime());
                RunLogger.getLogger().log(Level.FINE, "load servlet container: /" + path.getName() + " success!");
            }
        }
        httpHandle.start();
        final HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(httpHandle);
        bootstrap.setBannerEnabled(false);
        bootstrap.setBufferPool(1024 * 1024 * 10, Runtime.getRuntime().availableProcessors(), 1024 * 4);
        bootstrap.setReadBufferSize(1024 * 4).setPort(8081).start();
        System.out.println("启动成功,耗时：" + (System.currentTimeMillis() - start) + "ms");
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                httpHandle.stop();
                bootstrap.shutdown();
            }
        }));
    }
}
