package org.smartboot.servlet.spring;

import org.springframework.context.Lifecycle;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.context.ServletContextAware;
import org.springframework.web.socket.server.HandshakeFailureException;
import org.springframework.web.socket.server.standard.AbstractStandardUpgradeStrategy;

import javax.servlet.ServletContext;
import javax.websocket.Endpoint;
import javax.websocket.Extension;
import java.util.List;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2021/3/27
 */
@Configuration
public class SmartServletRequestUpgradeStrategy extends AbstractStandardUpgradeStrategy implements ServletContextAware, Lifecycle {
    private ServletContext servletContext;

    @Override
    protected void upgradeInternal(ServerHttpRequest request, ServerHttpResponse response, String selectedProtocol, List<Extension> selectedExtensions, Endpoint endpoint) throws HandshakeFailureException {
        System.out.println("haha"+servletContext);
    }

    @Override
    public String[] getSupportedVersions() {
        return new String[0];
    }

    @Override
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public boolean isRunning() {
        return false;
    }
}
