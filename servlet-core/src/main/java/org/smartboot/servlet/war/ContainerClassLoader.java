/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ContainerClassLoader.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.war;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class ContainerClassLoader {
    private final String location;
    private ClassLoader classLoader;

    public ContainerClassLoader(String location) {
        this.location = location;
    }

    public ClassLoader getClassLoader() throws MalformedURLException {
        if (classLoader != null) {
            return classLoader;
        }
        List<URL> list = new ArrayList<>();
        File libDir = new File(location, "WEB-INF" + File.separator + "lib/");
        File[] files = libDir.listFiles();
        if (files != null && files.length > 0) {
            for (File file : files) {
                list.add(file.toURI().toURL());
            }
        }

        File classDir = new File(location, "WEB-INF" + File.separator + "classes/");
        list.add(classDir.toURI().toURL());
        URL[] urls = new URL[list.size()];
        list.toArray(urls);
        classLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
        return classLoader;
    }
}
