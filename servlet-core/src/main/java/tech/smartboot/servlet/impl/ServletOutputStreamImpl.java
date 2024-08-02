/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.smartboot.http.common.BufferOutputStream;

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
    private int written;
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
            written += len;
            return;
        }

        if (len < buffer.length - written - 1) {
            System.arraycopy(b, off, buffer, written, len);
            written += len;
        } else {
            flushServletBuffer();
            outputStream.write(b, off, len);
            written += len;
        }
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
        if (buffer != null) {
            outputStream.write(buffer, 0, written);
            buffer = null;
        }
    }

    public boolean isCommitted() {
        return committed;
    }

    public void resetBuffer() {
        written = 0;
    }

    public int getWritten() {
        return written;
    }
}
