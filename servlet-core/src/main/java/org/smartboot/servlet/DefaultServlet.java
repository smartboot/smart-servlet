/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DefaultServlet.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;


import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.HttpHeaderConstant;
import org.smartboot.http.common.utils.Mimetypes;
import org.smartboot.http.common.utils.StringUtils;

import javax.servlet.DispatcherType;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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
public class DefaultServlet extends HttpServlet {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServlet.class);
    private static final int READ_BUFFER = 1024 * 1024;
    private static final String FAVICON_NAME = "favicon.ico";
    private static final String URL_404 =
            "<html>" +
                    "<head>" +
                    "<title>smart-http 404</title>" +
                    "</head>" +
                    "<body><h1>smart-http 找不到你所请求的地址资源，404</h1></body>" +
                    "</html>";
    private static byte[] faviconBytes = null;
    private final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        }
    };
    private long faviconModifyTime;

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
                faviconModifyTime = System.currentTimeMillis();
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
        String fileName = request.getRequestURI();
        String method = request.getMethod();
//        RunLogger.getLogger().log(Level.FINEST, "请求URL:" + fileName);
        URL url = request.getServletContext().getResource(fileName.substring(request.getContextPath().length()));
        File file = null;
        boolean defaultFavicon = false;
        if (url == null && fileName.endsWith(FAVICON_NAME) && faviconBytes != null) {
            defaultFavicon = true;
        }

        try {
            if (url != null) {
                LOGGER.info(url.toURI().toString());
                file = new File(url.toURI());
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //404
        if (!defaultFavicon && (file == null || !file.isFile())) {
            LOGGER.info("file:" + request.getRequestURI() + " not found!");
            //《Servlet3.1规范中文版》9.3 include 方法
            //如果默认的 servlet 是 RequestDispatch.include()的目标 servlet，
            // 而且请求的资源不存在，那么默认的 servlet 必须抛出 FileNotFoundException 异常。
            // 如果这个异常没有被捕获和处理，以及响应还未􏰀交，则响应状态 码必须被设置为 500。
            if (request.getDispatcherType() == DispatcherType.INCLUDE) {
                response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value(), HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
                throw new FileNotFoundException();
            }
            response.sendError(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
            response.setHeader(HttpHeaderConstant.Names.CONTENT_TYPE, "text/html; charset=utf-8");

            if (!HttpMethodEnum.HEAD.getMethod().equals(method)) {
                response.getOutputStream().write(URL_404.getBytes());
            }
            return;
        }

        //304
        long lastModifiedTime = defaultFavicon ? faviconModifyTime : file.lastModified();
        try {
            String requestModified = request.getHeader(HttpHeaderConstant.Names.IF_MODIFIED_SINCE);
            if (StringUtils.isNotBlank(requestModified) && lastModifiedTime <= sdf.get().parse(requestModified).getTime()) {
                response.sendError(HttpStatus.NOT_MODIFIED.value(), HttpStatus.NOT_MODIFIED.getReasonPhrase());
                return;
            }
        } catch (Exception e) {
            LOGGER.info("exception", e);
        }
        response.setHeader(HttpHeaderConstant.Names.LAST_MODIFIED, sdf.get().format(new Date(lastModifiedTime)));

        if (defaultFavicon) {
            response.setContentType("image/x-icon");
        } else {
            String contentType = Mimetypes.getInstance().getMimetype(file);
            response.setHeader(HttpHeaderConstant.Names.CONTENT_TYPE, contentType + "; charset=utf-8");
        }
        //HEAD不输出内容
        if (HttpMethodEnum.HEAD.getMethod().equals(method)) {
            return;
        }
        if (defaultFavicon) {
            response.setContentLength(faviconBytes.length);
            response.getOutputStream().write(faviconBytes);
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
            response.getOutputStream().write(data);
        }
        fis.close();
    }
}
