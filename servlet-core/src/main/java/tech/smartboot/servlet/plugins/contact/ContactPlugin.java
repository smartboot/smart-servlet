/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.contact;

import tech.smartboot.servlet.ServletContextRuntime;
import tech.smartboot.servlet.plugins.Plugin;

/**
 * 联系方式插件
 */
public class ContactPlugin extends Plugin {

    @Override
    public void onServletContextStartSuccess(ServletContextRuntime servletContextRuntime) {
        System.out.println();
        System.out.println("\033[1mTechnical Support:\033[0m");
        System.out.println(" · Document: https://smartboot.tech]");
        System.out.println(" · Gitee: https://gitee.com/smartboot/smart-servlet");
        System.out.println(" · Github: https://github.com/smartboot/smart-servlet");
        System.out.println(" · E-mail: zhengjunweimail@163.com");
    }
}
