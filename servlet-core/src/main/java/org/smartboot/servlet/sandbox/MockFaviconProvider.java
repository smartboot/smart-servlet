/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpMethodEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.servlet.ServletContextRuntime;
import org.smartboot.servlet.provider.FaviconProvider;

import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MockFaviconProvider implements FaviconProvider {

    @Override
    public void resister(ServletContextRuntime runtime) {
        ServletRegistration.Dynamic dynamic = runtime.getServletContext().addServlet("aa", new FaviconServlet());
        dynamic.addMapping("/favicon.ico");
    }

    static class FaviconServlet extends HttpServlet {
        private byte[] faviconBytes = null;
        private long faviconModifyTime;
        private final ThreadLocal<SimpleDateFormat> sdf = new ThreadLocal<SimpleDateFormat>() {
            @Override
            protected SimpleDateFormat initialValue() {
                return new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss z", Locale.ENGLISH);
            }
        };

        @Override
        public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            if (faviconBytes != null) {
                String requestModified = req.getHeader(HeaderNameEnum.IF_MODIFIED_SINCE.getName());
                try {
                    if (StringUtils.isNotBlank(requestModified) && faviconModifyTime <= sdf.get().parse(requestModified).getTime()) {
                        resp.sendError(HttpStatus.NOT_MODIFIED.value(), HttpStatus.NOT_MODIFIED.getReasonPhrase());
                        return;
                    }
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
            } else {
                loadDefaultFavicon();
            }

            resp.setHeader(HeaderNameEnum.LAST_MODIFIED.getName(), sdf.get().format(new Date(faviconModifyTime)));
            resp.setContentType("image/x-icon");
            //HEAD不输出内容
            if (HttpMethodEnum.HEAD.getMethod().equals(req.getMethod())) {
                return;
            }
            resp.setContentLength(faviconBytes.length);
            resp.getOutputStream().write(faviconBytes);
        }

        private void loadDefaultFavicon() {
            if (faviconBytes != null) {
                return;
            }
            InputStream inputStream = null;
            try {
                inputStream = MockFaviconProvider.class.getClassLoader().getResourceAsStream("favicon.ico");
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
    }
}
