package org.smartboot.servlet.starter;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.servlet.ServletHttpHandle;
import org.smartboot.servlet.war.WebContextRuntime;

import java.io.File;

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
        ServletHttpHandle httpHandle = new ServletHttpHandle();
        File file = new File(webapps);
        if (file.isDirectory()) {
            for (File path : file.listFiles()) {
                System.out.println("start load: " + path.getAbsolutePath());
                WebContextRuntime webContextRuntime = new WebContextRuntime(path.getAbsolutePath(), "examples".equals(path.getName()) ? "/" : "/" + path.getName());
                httpHandle.addRuntime(webContextRuntime.getServletRuntime());
                System.out.println("load " + path.getName() + " success!");
            }
        }
        httpHandle.start();
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(httpHandle);
        bootstrap.setBannerEnabled(false);
        bootstrap.setReadBufferSize(1024 * 1024).setPort(8080).start();
        System.out.println("启动成功,耗时：" + (System.currentTimeMillis() - start) + "ms");
    }
}
