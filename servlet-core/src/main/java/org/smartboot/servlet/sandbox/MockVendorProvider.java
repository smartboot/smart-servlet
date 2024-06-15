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

import org.smartboot.servlet.Container;
import org.smartboot.servlet.provider.VendorProvider;

import javax.servlet.http.HttpServletResponse;

public class MockVendorProvider implements VendorProvider {
    @Override
    public void signature(HttpServletResponse response) {
        response.addHeader("X-Powered-By", "smartboot");
        response.addHeader("X-Version", Container.VERSION);
        response.addHeader("X-System", getBasicInfo());
        response.addHeader("X-Open-Source", "https://gitee.com/smartboot/smart-servlet");
        response.addHeader("X-Tip", "The current version is not authorized.");
    }

    private String getBasicInfo() {
        return "Java_" + System.getProperty("java.version") + "/" + System.getProperty("os.name") + "_" + System.getProperty("os.arch") + "_" + System.getProperty("os.version") + "/" + Runtime.getRuntime().availableProcessors() + "C" + (int) (Runtime.getRuntime().maxMemory() * 1.0 / 1024 / 1024 / 1024 + 0.5) + "G";
    }
}
