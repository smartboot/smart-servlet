/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: MockMemoryPoolProvider.java
 * Date: 2020-12-08
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.servlet.provider.MemoryPoolProvider;
import org.smartboot.socket.buffer.BufferPage;
import org.smartboot.socket.buffer.BufferPagePool;

/**
 * @author 三刀
 * @version V1.0 , 2020/12/8
 */
public class MockMemoryPoolProvider implements MemoryPoolProvider {
    private final BufferPagePool bufferPage = new BufferPagePool(1024 * 1024, Runtime.getRuntime().availableProcessors(), true);

    @Override
    public BufferPage getBufferPage() {
        return bufferPage.allocateBufferPage();
    }
}
