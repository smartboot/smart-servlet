/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: EventListenerInfo.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

import java.util.EventListener;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class EventListenerInfo {
    private final String listenerClass;
    private EventListener listener;

    public EventListenerInfo(String listenerClass) {
        this.listenerClass = listenerClass;
    }

    public EventListener getListener() {
        return listener;
    }

    public void setListener(EventListener listener) {
        this.listener = listener;
    }

    public String getListenerClass() {
        return listenerClass;
    }
}
