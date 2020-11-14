/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DefaultServlet.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet;


import org.smartboot.http.enums.HttpMethodEnum;
import org.smartboot.http.enums.HttpStatus;
import org.smartboot.http.logging.RunLogger;
import org.smartboot.http.utils.HttpHeaderConstant;
import org.smartboot.http.utils.Mimetypes;
import org.smartboot.http.utils.StringUtils;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Locale;
import java.util.logging.Level;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/11
 */
public class DefaultServlet extends HttpServlet {
    private static final int READ_BUFFER = 1024 * 1024;
    private static final String URL_404 =
            "<html>" +
                    "<head>" +
                    "<title>smart-http 404</title>" +
                    "</head>" +
                    "<body><h1>smart-http 找不到你所请求的地址资源，404</h1></body>" +
                    "</html>";
    private ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
        }
    };

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        Enumeration<String> enumeration = config.getInitParameterNames();
        while (enumeration.hasMoreElements()) {
            String name = enumeration.nextElement();
            RunLogger.getLogger().log(Level.INFO, "servlet parameter name:" + name + " ,value:" + config.getInitParameter(name));
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String fileName = request.getRequestURI();
        String method = request.getMethod();
        if (StringUtils.endsWith(fileName, "/")) {
            fileName += "index.html";
        }
        RunLogger.getLogger().log(Level.FINEST, "请求URL:" + fileName);
        URL url = request.getServletContext().getResource(fileName);
        File file = null;
        try {
            file = new File(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        //404
        if (file == null || !file.isFile()) {
            RunLogger.getLogger().log(Level.WARNING, "file:" + request.getRequestURI() + " not found!");
            response.sendError(HttpStatus.NOT_FOUND.value(), HttpStatus.NOT_FOUND.getReasonPhrase());
            response.setHeader(HttpHeaderConstant.Names.CONTENT_TYPE, "text/html; charset=utf-8");

            if (!HttpMethodEnum.HEAD.getMethod().equals(method)) {
                response.getOutputStream().write(URL_404.getBytes());
            }
            return;
        }
        //304
        Date lastModifyDate = new Date(file.lastModified());
        try {
            String requestModified = request.getHeader(HttpHeaderConstant.Names.IF_MODIFIED_SINCE);
            if (StringUtils.isNotBlank(requestModified) && lastModifyDate.getTime() <= sdf.get().parse(requestModified).getTime()) {
                response.sendError(HttpStatus.NOT_MODIFIED.value(), HttpStatus.NOT_MODIFIED.getReasonPhrase());
                return;
            }
        } catch (Exception e) {
            RunLogger.getLogger().log(Level.SEVERE, "exception", e);
        }
        response.setHeader(HttpHeaderConstant.Names.LAST_MODIFIED, sdf.get().format(lastModifyDate));


        String contentType = Mimetypes.getInstance().getMimetype(file);
        response.setHeader(HttpHeaderConstant.Names.CONTENT_TYPE, contentType + "; charset=utf-8");
        //HEAD不输出内容
        if (HttpMethodEnum.HEAD.getMethod().equals(method)) {
            return;
        }

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
