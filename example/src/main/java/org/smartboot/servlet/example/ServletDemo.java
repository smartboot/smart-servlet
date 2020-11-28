package org.smartboot.servlet.example;

import org.smartboot.http.HttpBootstrap;
import org.smartboot.servlet.ServletHttpHandle;

import java.net.MalformedURLException;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletDemo {
    public static void main(String[] args) throws MalformedURLException {
        ServletHttpHandle httpHandle = new ServletHttpHandle();
        httpHandle.start();
        HttpBootstrap bootstrap = new HttpBootstrap();
        bootstrap.pipeline().next(httpHandle);
        bootstrap.setReadBufferSize(1024 * 1024).setPort(8080).start();
        System.out.println("启动成功");
    }
}
