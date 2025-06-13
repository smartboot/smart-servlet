/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet;


import jakarta.servlet.DispatcherType;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.MappingMatch;
import tech.smartboot.feat.core.common.FeatUtils;
import tech.smartboot.feat.core.common.HeaderName;
import tech.smartboot.feat.core.common.HttpMethod;
import tech.smartboot.feat.core.common.HttpStatus;
import tech.smartboot.feat.core.common.exception.FeatException;
import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;
import tech.smartboot.feat.core.common.utils.Mimetypes;
import tech.smartboot.servlet.conf.DeploymentInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.impl.WriterOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
class DefaultServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServlet.class);
    private static final int READ_BUFFER = 1024 * 1024;
    private static final String FAVICON_NAME = "favicon.ico";
    private static byte[] faviconBytes = null;
    private final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        }
    };
    /**
     * 默认页面
     */
    private long faviconModifyTime;
    private final ServletContextRuntime runtime;

    public DefaultServlet(ServletContextRuntime runtime) {
        this.runtime = runtime;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Enumeration<String> enumeration = config.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            LOGGER.info("servlet parameter name:" + name + " ,value:" + config.getInitParameter(name));
        }
        loadDefaultFavicon();
    }

    private void loadDefaultFavicon() {
        if (faviconBytes != null) {
            return;
        }
        InputStream inputStream = null;
        try {
            inputStream = DefaultServlet.class.getClassLoader().getResourceAsStream(FAVICON_NAME);
            if (inputStream != null) {
                faviconBytes = new byte[inputStream.available()];
                inputStream.read(faviconBytes);
                faviconModifyTime = System.currentTimeMillis() / 1000 * 1000;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = request.getDispatcherType() == DispatcherType.INCLUDE ? (String) request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) : request.getRequestURI();
        String method = request.getMethod();
        String resource = fileName.substring(request.getContextPath().length());
        if (FeatUtils.isBlank(resource)) {
            resource = "/";
        }
        URL url = request.getServletContext().getResource(resource);
        File file = null;
        boolean defaultFavicon = url == null && fileName.endsWith(FAVICON_NAME) && faviconBytes != null;

        try {
            if (url != null) {
                LOGGER.info(url.toURI().toString());
                file = new File(url.toURI());
            }
            //资源文件不存在，尝试跳转welcome文件
            boolean forwardWelcome = !defaultFavicon && (file == null || !file.isFile());
            if (forwardWelcome) {
                forwardWelcome(request, response, method);
                return;
            }
        } catch (URISyntaxException e) {
            throw new FeatException(e);
        }

        //304
        long lastModifiedTime = defaultFavicon ? faviconModifyTime : file.lastModified();
        try {
            String requestModified = request.getHeader(HeaderName.IF_MODIFIED_SINCE.getName());
            if (FeatUtils.isNotBlank(requestModified) && lastModifiedTime <= sdf.get().parse(requestModified).getTime()) {
                response.sendError(HttpStatus.NOT_MODIFIED.value(), HttpStatus.NOT_MODIFIED.getReasonPhrase());
                return;
            }
        } catch (Exception e) {
            LOGGER.info("exception", e);
        }
        response.setHeader(HeaderName.LAST_MODIFIED.getName(), sdf.get().format(new Date(lastModifiedTime)));

        if (defaultFavicon) {
            response.setContentType("image/x-icon");
        } else {
            String contentType = Mimetypes.getInstance().getMimetype(file);
            response.setHeader(HeaderName.CONTENT_TYPE.getName(), contentType + "; charset=utf-8");
        }
        //HEAD不输出内容
        if (HttpMethod.HEAD.equals(method)) {
            return;
        }

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
        } catch (IllegalStateException e) {
            outputStream = new WriterOutputStream(response.getWriter());
        }

        if (defaultFavicon) {
            response.setContentLength(faviconBytes.length);
            outputStream.write(faviconBytes);
            return;
        }
        LOGGER.info("load file:" + fileName);
        FileInputStream fis = new FileInputStream(file);
        FileChannel fileChannel = fis.getChannel();
        long fileSize = fileChannel.size();
        long readPos = 0;
        while (readPos < fileSize) {
            MappedByteBuffer mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, readPos, fileSize - readPos > READ_BUFFER ? READ_BUFFER : fileSize - readPos);
            readPos += mappedByteBuffer.remaining();
            byte[] data = new byte[mappedByteBuffer.remaining()];
            mappedByteBuffer.get(data);
            outputStream.write(data);
        }
        fis.close();
    }

    /**
     * 尝试跳转至welcome页面
     */
    private void forwardWelcome(HttpServletRequest request, HttpServletResponse response, String method) throws URISyntaxException, IOException, ServletException {
        if (matchForwardWelcome(request, response)) {
            //找到有效welcome file，执行服务端跳转
            return;
        }
        // 404
        LOGGER.info("file:" + request.getRequestURI() + " not found!");
        //《Servlet3.1规范中文版》9.3 include 方法
        //如果默认的 servlet 是 RequestDispatch.include()的目标 servlet，
        // 而且请求的资源不存在，那么默认的 servlet 必须抛出 FileNotFoundException 异常。
        // 如果这个异常没有被捕获和处理，以及响应还未􏰀交，则响应状态 码必须被设置为 500。
        if (request.getDispatcherType() == DispatcherType.INCLUDE) {
            throw new FileNotFoundException();
        }

        response.sendError(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
//        String location = deploymentInfo.getErrorPageLocation(HttpStatus.NOT_FOUND.value());
//        if (FeatUtils.isNotBlank(location)) {
//            request.getRequestDispatcher(location).forward(request, response);
//            return;
//        }
//        response.setHeader(HeaderNameEnum.CONTENT_TYPE.getName(), "text/html; charset=utf-8");
//
//        if (!HttpMethodEnum.HEAD.getMethod().equals(method)) {
//            response.getOutputStream().write(URL_404.getBytes());
//        }
    }

    private boolean matchForwardWelcome(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        String requestUri = request.getDispatcherType() == DispatcherType.INCLUDE ? (String) request.getAttribute(RequestDispatcher.INCLUDE_REQUEST_URI) : request.getRequestURI();
        ServletContext servletContext = request.getServletContext();
        DeploymentInfo deploymentInfo = runtime.getDeploymentInfo();
        if (requestUri.endsWith("/")) {
            for (String file : deploymentInfo.getWelcomeFiles()) {
                String uri = requestUri.substring(request.getContextPath().length());
                URL welcomeUrl = servletContext.getResource(uri + file);
                if (welcomeUrl != null) {
                    request.getRequestDispatcher(uri + file).forward(request, response);
                    return true;
                }
                //是否匹配 Servlet url-pattern
                ServletMappingInfo mappingInfo = runtime.getMappingProvider().matchWithoutContextPath("/" + file);
                if (mappingInfo != null && mappingInfo.getMappingMatch() == MappingMatch.EXACT) {
                    request.getRequestDispatcher(uri + file).forward(request, response);
                    return true;
                }
            }
            return false;
        }
        // 例如: /abc/d.html ,由于d.html不存在而走到该分支
        if (deploymentInfo.getWelcomeFiles().stream().anyMatch(requestUri::endsWith) || requestUri.indexOf(".") > 0) {
            return false;
        }
        //存在目录，则触发客户端跳转
        URL url = servletContext.getResource(requestUri.substring(request.getContextPath().length()) + "/");
        if (url != null) {
            response.sendRedirect(requestUri + "/");
//            request.getRequestDispatcher(requestUri.substring(request.getContextPath().length()) + "/").forward(request, response);
            return true;
        }
//        URL url = servletContext.getResource(requestUri.substring(request.getContextPath().length()) + "/");

        return false;
    }

}
