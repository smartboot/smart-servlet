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
