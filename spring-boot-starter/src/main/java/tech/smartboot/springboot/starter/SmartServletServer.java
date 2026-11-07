/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
