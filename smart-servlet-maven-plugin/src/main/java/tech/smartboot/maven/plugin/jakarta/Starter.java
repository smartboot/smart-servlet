/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
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
