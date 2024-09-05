/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.license;

import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.http.common.utils.StringUtils;
import org.smartboot.http.server.HttpRequest;
import org.smartboot.http.server.HttpResponse;
import org.smartboot.http.server.HttpServerHandler;
import tech.smartboot.servlet.Container;
import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.plugins.Plugin;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/9/30
 */
public class LicensePlugin extends Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicensePlugin.class);

    private LicenseTO licenseTO;
    private License license;

    private static String expireMessage = "The LICENSE has expired. Please renew it in time.";

    @Override
    public void initPlugin(Container containerRuntime) {
        loadLicense();

        HttpServerHandler baseHandler = containerRuntime.getConfiguration().getHttpServerHandler();
        containerRuntime.getConfiguration().setHttpServerHandler(new HttpServerHandler() {
            @Override
            public void handle(HttpRequest request, HttpResponse response, CompletableFuture<Object> completableFuture) throws Throwable {
                if (licenseTO != null || "/favicon.ico".equals(request.getRequestURI())) {
                    baseHandler.handle(request, response, completableFuture);
                } else {
                    try {
                        response.setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE);
                        OutputStream outputStream = response.getOutputStream();
                        Map<String, String> data = new HashMap<>();
                        List<String> macs = getMacAddresses();
                        if (macs.isEmpty()) {
                            outputStream.write("<center>".getBytes());
                            outputStream.write(("<h1>" + HttpStatus.SERVICE_UNAVAILABLE.value() + " " + HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase() + "</h1>").getBytes());
                            outputStream.write(expireMessage.getBytes());
                            outputStream.write(("<hr/><a target='_blank' href='https://smartboot.tech/smart-servlet'>smart-servlet</a>/" + Container.VERSION + "&nbsp;|&nbsp; <a target='_blank' href='https://gitee.com/smartboot/smart-servlet'>Gitee</a>").getBytes());
                            outputStream.write("</center>".getBytes());
                        } else {
                            data.put("mac", getMacAddresses().get(0));
                            outputStream.write(license.getExpireHtml(data).getBytes());
                        }
                    } catch (IOException e) {
                        LOGGER.warn("HttpError response exception", e);
                    } finally {
                        response.close();
                    }
                }
            }
        });
    }

    @Override
    public void addServletContext(ServletContextRuntime runtime) {
        if (licenseTO != null) {
            runtime.setVendorProvider(response -> {
            });
        }
    }

    @Override
    public void willStartContainer(ServletContextRuntime containerRuntime) {
        containerRuntime.setFaviconProvider(runtime -> {
        });
    }

    private void loadLicense() {
        license = new License(entity -> {
            System.err.println("License已过期");
            licenseTO = null;
        }, entity -> {
            if (entity == license.getEntity()) {
                System.err.println("The trial version License has expired.");
                licenseTO = null;
                expireMessage = "The trial period is over. Please purchase the authorized full version of LICENSE.";
            }
        }, 10000);

        try (InputStream fileInputStream = getResource("License.shield")) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            LicenseEntity entity = license.loadLicense(outputStream.toByteArray());
            licenseTO = loadLicense(entity);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            System.out.println(":: Licensed to " + ConsoleColors.BOLD + ConsoleColors.ANSI_UNDERLINE_ON + ConsoleColors.BLUE + licenseTO.getApplicant() + ConsoleColors.ANSI_RESET + " until " + ConsoleColors.BOLD + ConsoleColors.ANSI_UNDERLINE_ON + ConsoleColors.BLUE + sdf.format(new Date(licenseTO.getExpireTime())) + ConsoleColors.ANSI_RESET);
            System.out.println(":: SN: " + ConsoleColors.BOLD + ConsoleColors.ANSI_UNDERLINE_ON + licenseTO.getSn() + ConsoleColors.RESET);
            System.out.println(":: Copyright© " + licenseTO.getVendor() + " ,E-mail: " + licenseTO.getContact());
            if (licenseTO.getTrialDuration() > 0) {
                System.out.println(ConsoleColors.RED + ":: Trial: " + licenseTO.getTrialDuration() + " minutes" + ConsoleColors.RESET);
            } else if (!getMacAddresses().contains(licenseTO.getMac())) {
                licenseTO = null;
            }
        } catch (Exception e) {
            System.err.println("load license error");
        }
    }

    private LicenseTO loadLicense(LicenseEntity entity) throws IOException {
        Properties properties = new Properties();
        properties.load(new ByteArrayInputStream(entity.getData()));
        LicenseTO licenseTO = new LicenseTO();
        licenseTO.setApplicant(properties.getProperty("enterprise.license.user"));
        licenseTO.setSn(properties.getProperty("enterprise.license.number"));
        licenseTO.setExpireTime(entity.getExpireTime());
        licenseTO.setTrialDuration(entity.getTrialDuration());
        licenseTO.setContact(entity.getContact());
        licenseTO.setVendor(entity.getApplicant());
        licenseTO.setPlugins(Arrays.asList(StringUtils.split(properties.getProperty("plugins", ""), ",")));
        licenseTO.setMac(properties.getProperty("mac"));
        return licenseTO;
    }

    private List<String> getMacAddresses() {
        List<String> macs = new ArrayList<>();
//        try {
//            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
//            while (networkInterfaces.hasMoreElements()) {
//                NetworkInterface networkInterface = networkInterfaces.nextElement();
//                byte[] mac = networkInterface.getHardwareAddress();
//                if (mac != null) {
//                    StringBuilder sb = new StringBuilder();
//                    for (int i = 0; i < mac.length; i++) {
//                        sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
//                    }
//                    macs.add(sb.toString());
//                }
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        return macs;
    }


    static class ConsoleColors {
        /**
         * 重置颜色
         */
        public static final String RESET = "\033[0m";
        /**
         * 蓝色
         */
        public static final String BLUE = "\033[34m";

        /**
         * 红色
         */
        public static final String RED = "\033[31m";

        /**
         * 绿色
         */
        public static final String GREEN = "\033[32m";

        //加粗
        public static final String BOLD = "\033[1m";

        public static final String ANSI_UNDERLINE_ON = "\u001B[4m"; // 开启下划线
        public static final String ANSI_RESET = "\u001B[0m"; // 重置所有样式


    }
}
