/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.conf;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.servlet.impl.ServletConfigImpl;
import org.smartboot.servlet.impl.ServletContextImpl;
import org.smartboot.servlet.util.PathMatcherUtil;

import javax.servlet.MultipartConfigElement;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletInfo {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletInfo.class);
    public static final String DEFAULT_SERVLET_NAME = "default";
    private final List<ServletMappingInfo> mappings = new ArrayList<>();
    private final Map<String, String> initParams = new HashMap<>();
    private final Map<String, String> securityRoles = new HashMap<>();
    private String servletClass;
    private String servletName;
    private int loadOnStartup;
    private Servlet servlet;

    private String jspFile;

    private boolean dynamic;
    private MultipartConfigElement multipartConfig;

    private boolean asyncSupported;
    private boolean init = false;

    public synchronized void init(ServletContextImpl servletContext) {
        if (init) {
            return;
        }
        ServletConfig servletConfig = new ServletConfigImpl(this, servletContext);
        try {
            servlet.init(servletConfig);
        } catch (UnavailableException e) {
            e.printStackTrace();
            //占用该Servlet的URL mappings
            servlet = new HttpServlet() {
                @Override
                protected void service(HttpServletRequest req, HttpServletResponse resp) {
                    resp.setStatus(HttpStatus.NOT_FOUND.value());
                }
            };
        } catch (ServletException e) {
            e.printStackTrace();
            String location = servletContext.getDeploymentInfo().getErrorPageLocation(e);
            if (location == null) {
                location = servletContext.getDeploymentInfo().getErrorPageLocation(HttpStatus.INTERNAL_SERVER_ERROR.value());
            }
            String finalLocation = location;
            servlet = new HttpServlet() {
                @Override
                protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    resp.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
                    req.setAttribute(RequestDispatcher.ERROR_EXCEPTION, e);
                    req.setAttribute(RequestDispatcher.ERROR_MESSAGE, e.getMessage());
                    req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                    if (finalLocation != null) {
                        servletContext.getRuntime().getDispatcherProvider().error(servletContext,finalLocation,req,resp);
//                        req.getRequestDispatcher(finalLocation).forward(req, resp);
                    } else {
                        LOGGER.error("error location is null");
                        e.printStackTrace(resp.getWriter());
                    }
                }
            };
        } finally {
            init = true;
        }
    }

    public int getLoadOnStartup() {
        return loadOnStartup;
    }

    public void setLoadOnStartup(int loadOnStartup) {
        this.loadOnStartup = loadOnStartup;
    }

    public Servlet getServlet() {
        return servlet;
    }

    public void setServlet(Servlet servlet) {
        this.servlet = servlet;
    }

    public List<ServletMappingInfo> getMappings() {
        return Collections.unmodifiableList(mappings);
    }

    public ServletInfo addInitParam(final String name, final String value) {
        initParams.put(name, value);
        return this;
    }

    /**
     * 《Servlet 3.1规范中文版》 12.2 映射规范
     * 在 web 应用部署􏰁述符中，以下语法用于定义映射:
     * - 以‘/’字符开始、以‘/*’后缀结尾的字符串用于路径匹配。
     * - 以前缀‘*.’开始的字符串用于扩展名映射。
     * - 空字符串“”是一个特殊的 URL 模式，其精确映射到应用的上下文根，即，http://host:port/<context-root>/ 请求形式。在这种情况下，路径信息是‘/’且 servlet 路径和上下文路径是空字符串(“”)。
     * - 只包含“/”字符的字符串表示应用的“默认的”servlet。在这种情况下，servlet 路径是请求 URL 减去 上下文路径且路径信息是 null。
     * - 所以其他字符串仅用于精确匹配。
     *
     * @param mapping
     * @return
     */
    public ServletInfo addMapping(final String mapping) {
        ServletMappingInfo servletMappingInfo = PathMatcherUtil.addMapping(mapping);
        mappings.add(servletMappingInfo);
        return this;
    }

    public String getServletName() {
        return servletName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public Map<String, String> getInitParams() {
        return initParams;
    }

    public String getServletClass() {
        return servletClass;
    }

    public void setServletClass(String servletClass) {
        this.servletClass = servletClass;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public void setDynamic(boolean dynamic) {
        this.dynamic = dynamic;
    }

    public MultipartConfigElement getMultipartConfig() {
        return multipartConfig;
    }

    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        this.multipartConfig = multipartConfig;
    }

    public boolean isAsyncSupported() {
        return asyncSupported;
    }

    public void setAsyncSupported(boolean asyncSupported) {
        this.asyncSupported = asyncSupported;
    }

    public String getJspFile() {
        return jspFile;
    }

    public void setJspFile(String jspFile) {
        this.jspFile = jspFile;
    }

    public ServletInfo addSecurityRole(String name, String link) {
        this.securityRoles.put(name, link);
        return this;
    }

    public Map<String, String> getSecurityRoles() {
        return securityRoles;
    }

    /**
     * 是否已经完成初始化
     * @return
     */
    public boolean initialized() {
        return init;
    }
}
