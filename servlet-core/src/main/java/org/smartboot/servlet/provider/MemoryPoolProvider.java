/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MemoryPoolProvider.java
 * Date: 2020-12-08
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.provider;

import org.smartboot.socket.buffer.BufferPage;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/8
 */
public interface MemoryPoolProvider {
    /**
     * 获取内存页
     *
     * @return
     */
    BufferPage getBufferPage();
}
