/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.conf;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.Servlet;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.UnavailableException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tech.smartboot.feat.core.common.HttpStatus;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.impl.ServletConfigImpl;
import tech.smartboot.servlet.impl.ServletContextImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class ServletInfo {
    private static final byte MASK_INITIALIZED = 0x01;
    private static final byte MASK_ASYNC_SUPPORT = 0x02;
    private static final byte MASK_DYNAMIC = 0x04;
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletInfo.class);
    private static final MultipartConfigElement DEFAULT_MULTIPART_CONFIG = new MultipartConfigElement("", -1, -1, -1);
    public static final String DEFAULT_SERVLET_NAME = "default";
    private final Map<String, String> initParams = new HashMap<>();
    private final Map<String, String> securityRoles = new HashMap<>();
    private String servletClass;
    private String servletName;
    private int loadOnStartup;
    private Servlet servlet;

    private String jspFile;

    private MultipartConfigElement multipartConfig = DEFAULT_MULTIPART_CONFIG;

    private byte mask;
    private final List<SecurityConstraint> securityConstraints = new ArrayList<>();

    private final List<ServletMappingInfo> servletMappings = new ArrayList<>();

    public ServletInfo() {
        this(false);
    }

    public ServletInfo(boolean dynamic) {
        if (dynamic) {
            addMask(MASK_DYNAMIC);
        }
    }

    public synchronized void init(ServletContextImpl servletContext) {
        if (initialized()) {
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
//                    req.setAttribute(RequestDispatcher.ERROR_EXCEPTION, e);
//                    req.setAttribute(RequestDispatcher.ERROR_MESSAGE, e.getMessage());
//                    req.setAttribute(RequestDispatcher.ERROR_STATUS_CODE, HttpStatus.INTERNAL_SERVER_ERROR.value());
                    if (finalLocation != null) {
                        servletContext.getRuntime().getDispatcherProvider().error(servletContext, finalLocation, req, resp, e, servletName, e.getMessage());
//                        req.getRequestDispatcher(finalLocation).forward(req, resp);
                    } else {
                        LOGGER.error("error location is null");
                        e.printStackTrace(resp.getWriter());
                    }
                }
            };
        } finally {
            addMask(MASK_INITIALIZED);
        }
    }

    private void addMask(byte v) {
        mask |= v;
    }

    private boolean masked(byte v) {
        return (mask & v) == v;
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

    public ServletInfo addInitParam(final String name, final String value) {
        initParams.put(name, value);
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
        return masked(MASK_DYNAMIC);
    }

    public MultipartConfigElement getMultipartConfig() {
        return multipartConfig;
    }

    public void setMultipartConfig(MultipartConfigElement multipartConfig) {
        this.multipartConfig = multipartConfig;
    }

    public boolean isAsyncSupported() {
        return masked(MASK_ASYNC_SUPPORT);
    }

    public void setAsyncSupported(boolean asyncSupported) {
        if (asyncSupported) {
            addMask(MASK_ASYNC_SUPPORT);
        } else {
            mask &= ~MASK_ASYNC_SUPPORT;
        }
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

    public List<SecurityConstraint> getSecurityConstraints() {
        return securityConstraints;
    }

    /**
     * 是否已经完成初始化
     *
     * @return
     */
    public boolean initialized() {
        return masked(MASK_INITIALIZED);
    }

    public List<ServletMappingInfo> getServletMappings() {
        return servletMappings;
    }

    public void addServletMapping(String urlPattern, ServletContextRuntime runtime) {
        if (servletMappings.stream().anyMatch(servletMapping -> servletMapping.getUrlPattern().equals(urlPattern))) {
            return;
        }
        ServletMappingInfo servletMappingInfo = new ServletMappingInfo(this, urlPattern);
        if (servletMappingInfo.getMappingMatch() != null) {
            servletMappings.add(servletMappingInfo);
            runtime.getMappingProvider().addMapping(servletMappingInfo);
        } else {
            LOGGER.error("invalid mapping : " + urlPattern);
        }

    }
}
