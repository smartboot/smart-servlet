/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.springboot.starter;

import org.springframework.boot.web.server.WebServer;
import org.springframework.boot.web.server.WebServerException;
import org.springframework.boot.web.servlet.server.AbstractServletWebServerFactory;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ServletContextRuntime;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/12
 */
public class SmartServletServer implements WebServer {
    private final Object monitor = new Object();
    private final Container container;
    private volatile boolean started = false;
    private final AbstractServletWebServerFactory factory;

    public SmartServletServer(ServletContextRuntime runtime, AbstractServletWebServerFactory factory) throws Throwable {
        this.factory = factory;
        container = new Container();
        container.addRuntime(runtime);
        container.initialize();
    }

    public Container getContainer() {
        return container;
    }

    @Override
    public void start() throws WebServerException {
        synchronized (this.monitor) {
            if (this.started) {
                return;
            }
            try {
                container.start();
                this.started = true;
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void stop() throws WebServerException {
        synchronized (this.monitor) {
            if (!this.started) {
                return;
            }
            this.started = false;
            container.stop();
        }
    }

    @Override
    public int getPort() {
        return factory.getPort();
    }
}
