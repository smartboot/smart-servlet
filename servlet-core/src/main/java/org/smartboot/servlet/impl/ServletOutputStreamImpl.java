/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.common.BufferOutputStream;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/19
 */
public class ServletOutputStreamImpl extends ServletOutputStream {
    private final OutputStream outputStream;
    private boolean committed = false;
    /**
     * buffer仅用于提供response.resetBuffer能力,commit之后即失效
     */
    private byte[] buffer;
    private int count;
    private byte[] cacheByte;

    public ServletOutputStreamImpl(BufferOutputStream outputStream, byte[] buffer) {
//        this.outputStream = new BufferedOutputStream(outputStream, 1024);
        this.outputStream = outputStream;
        this.buffer = buffer;
    }

    @Override
    public boolean isReady() {
        throw new UnsupportedOperationException();
//        return false;
    }

    @Override
    public void setWriteListener(WriteListener writeListener) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void write(int v) throws IOException {
        initCacheBytes();
        cacheByte[0] = (byte) v;
        write(cacheByte, 0, 1);
    }

    /**
     * 初始化8字节的缓存数值
     */
    private void initCacheBytes() {
        if (cacheByte == null) {
            cacheByte = new byte[8];
        }
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        if (committed) {
            outputStream.write(b, off, len);
            return;
        }
        //继续缓存数据
        if (buffer == null) {
            committed = true;
            outputStream.write(b, off, len);
            return;
        }
        if (len < buffer.length - count - 1) {
            System.arraycopy(b, off, buffer, count, len);
            count += len;
            return;
        }
        committed = true;
        //buffer中存在缓存数据，先输出
        if (count > 0) {
            outputStream.write(buffer, 0, count);
            count = 0;
        }
        buffer = null;
        outputStream.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        outputStream.close();
    }

    @Override
    public void flush() throws IOException {
        flushServletBuffer();
        outputStream.flush();
    }

    public void flushServletBuffer() throws IOException {
        committed = true;
        if (count > 0) {
            outputStream.write(buffer, 0, count);
            buffer = null;
            count = 0;
        }
    }

    public boolean isCommitted() {
        return committed;
    }

    public void resetBuffer() {
        count = 0;
    }

    public int getCount() {
        return count;
    }
}
