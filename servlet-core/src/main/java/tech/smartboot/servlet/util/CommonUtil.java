/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.util;

import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommonUtil {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommonUtil.class);

    public static String getResourceAsString(String fileName) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try (InputStream inputStream = CommonUtil.class.getClassLoader().getResourceAsStream(fileName);) {
            if (inputStream == null) {
                LOGGER.error("resource {} not found", fileName);
                return null;
            }
            byte[] buffer = new byte[1024];
            int len;
            while ((len = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, len);
            }
            return outputStream.toString();
        } catch (IOException e) {
            LOGGER.error("read resource {} error", fileName, e);
            return null;
        }
    }

}
