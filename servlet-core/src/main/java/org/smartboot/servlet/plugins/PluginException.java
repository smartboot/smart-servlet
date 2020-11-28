/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: PluginException.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.plugins;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/27
 */
public class PluginException extends RuntimeException {
    public PluginException(String message) {
        super(message);
    }
}
