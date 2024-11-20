/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.maven.plugin.jakarta;

import tech.smartboot.servlet.Container;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/4
 */
public class Starter {

    public Starter(String path, String contentPath, int port, ClassLoader classLoader) throws Throwable {
        System.out.println("path: " + path);
        System.out.println("contentPath: " + contentPath);
        Container container = new Container();
        container.addRuntime(path, contentPath, classLoader);
        container.initialize();
        container.getConfiguration().setPort(port);
        container.start();
        Runtime.getRuntime().addShutdownHook(new Thread(container::stop));
    }
}
