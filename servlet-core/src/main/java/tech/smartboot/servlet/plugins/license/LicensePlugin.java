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
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2022/9/30
 */
public class LicensePlugin extends Plugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(LicensePlugin.class);
    private static final LicenseTO INVALID_LICENSE = new LicenseTO();
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
                if ((licenseTO != null && licenseTO != INVALID_LICENSE) || "/favicon.ico".equals(request.getRequestURI())) {
                    baseHandler.handle(request, response, completableFuture);
                } else {
                    try {
                        response.setHttpStatus(HttpStatus.SERVICE_UNAVAILABLE);
                        OutputStream outputStream = response.getOutputStream();
                        outputStream.write("<center>".getBytes());
                        outputStream.write(("<h1>" + HttpStatus.SERVICE_UNAVAILABLE.value() + " " + HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase() + "</h1>").getBytes());
                        outputStream.write(expireMessage.getBytes());
                        outputStream.write(("<hr/><a target='_blank' href='https://smartboot.tech/smart-servlet'>smart-servlet</a>/" + Container.VERSION + "&nbsp;|&nbsp; <a target='_blank' href='https://gitee.com/smartboot/smart-servlet'>Gitee</a>").getBytes());
                        outputStream.write("</center>".getBytes());
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
    public void onContainerInitialized(Container container) {
        System.out.println("\033[1mLicense Plugin:\033[0m");
        if (licenseTO == null) {
            System.out.println("\t" + ConsoleColors.RED + "ERROR：License not found, please check the license file：[ " + (isSpringBoot() ? "src/main/resources/smart-servlet/License.shield" : "${SERVLET_HOME}/conf/License.shield") + " ]." + ConsoleColors.RESET);
            return;
        }
        if (licenseTO == INVALID_LICENSE) {
            System.out.println("\t" + ConsoleColors.RED + "ERROR：License is invalid, please check the license file：[ " + (isSpringBoot() ? "src/main/resources/smart-servlet/License.shield" : "${SERVLET_HOME}/conf/License.shield") + " ]." + ConsoleColors.RESET);
            return;
        }
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        System.out.println("\t:: Licensed to " + ConsoleColors.BOLD + ConsoleColors.ANSI_UNDERLINE_ON + ConsoleColors.BLUE + licenseTO.getApplicant() + ConsoleColors.ANSI_RESET + " until " + ConsoleColors.BOLD + ConsoleColors.ANSI_UNDERLINE_ON + ConsoleColors.BLUE + sdf.format(new Date(licenseTO.getExpireTime())) + ConsoleColors.ANSI_RESET);
        System.out.println("\t:: License ID: " + ConsoleColors.BOLD + ConsoleColors.ANSI_UNDERLINE_ON + licenseTO.getSn() + ConsoleColors.RESET);
        System.out.println("\t:: Copyright© " + licenseTO.getVendor() + " ,E-mail: " + licenseTO.getContact());
        if (licenseTO.getTrialDuration() > 0) {
            System.out.println(ConsoleColors.RED + "\t:: Trial: " + licenseTO.getTrialDuration() + " minutes" + ConsoleColors.RESET);
        }
    }

    @Override
    public void addServletContext(ServletContextRuntime runtime) {
        if (licenseTO != null) {
            runtime.setVendorProvider(response -> {
            });
        }
    }

    @Override
    public void willStartServletContext(ServletContextRuntime containerRuntime) {
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
            if (fileInputStream == null) {
                return;
            }
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            LicenseEntity entity = license.loadLicense(outputStream.toByteArray());
            licenseTO = loadLicense(entity);
        } catch (Exception e) {
            LOGGER.error("License load exception", e.getMessage());
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

        if (!isVersionSupported(Container.VERSION.substring(1), properties.getProperty("supportVersion"))) {
            return INVALID_LICENSE;
        }
        return licenseTO;
    }

    public static boolean isVersionSupported(String containerVersion, String supportVersion) {
        if (StringUtils.isBlank(supportVersion)) {
            return false;
        }
        // 解析支持版本范围
        String[] versionRange = supportVersion.split("~");
        String startVersion = versionRange[0];
        String endVersion = versionRange.length == 2 ? versionRange[1] : "99.99.99";

        // 将版本号转换为整数数组
        int[] containerVersionArray = parseVersion(containerVersion);
        int[] startVersionArray = parseVersion(startVersion);
        int[] endVersionArray = parseVersion(endVersion);

        // 比较版本号
        return compareVersions(containerVersionArray, startVersionArray) >= 0 && compareVersions(containerVersionArray, endVersionArray) <= 0;
    }

    private static int[] parseVersion(String version) {
        String[] parts = version.split("\\.");
        int[] versionArray = new int[parts.length];
        for (int i = 0; i < parts.length; i++) {
            versionArray[i] = Integer.parseInt(parts[i]);
        }
        return versionArray;
    }

    private static int compareVersions(int[] version1, int[] version2) {
        int length = Math.max(version1.length, version2.length);
        for (int i = 0; i < length; i++) {
            int v1 = i < version1.length ? version1[i] : 0;
            int v2 = i < version2.length ? version2[i] : 0;
            if (v1 != v2) {
                return v1 - v2;
            }
        }
        return 0;
    }


}
