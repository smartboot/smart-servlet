/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletOutputStreamImpl.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.http.common.BufferOutputStream;
import org.smartboot.socket.buffer.VirtualBuffer;

import javax.servlet.ServletOutputStream;
import javax.servlet.WriteListener;
import java.io.IOException;

/**
 * @author 三刀
 * @version V1.0 , 2020/10/19
 */
public class ServletOutputStreamImpl extends ServletOutputStream {
    private final BufferOutputStream outputStream;
    private boolean committed = false;
    /**
     * buffer仅用于提供response.resetBuffer能力,commit之后即失效
     */
    private byte[] buffer;
    private int count;

    public ServletOutputStreamImpl(BufferOutputStream outputStream, byte[] buffer) {
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
    public void write(int b) {
        throw new UnsupportedOperationException();
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

    public void write(VirtualBuffer buffer) throws IOException {
        outputStream.write(buffer);
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

    public void setCount(int count) throws IOException {
        //此段逻辑必须只由ServletPrintWrite#write触发
        boolean commit = count == this.buffer.length || count == this.count;
        this.count = count;
        if (commit) {
            flush();
        } else if (count > this.buffer.length) {
            throw new IndexOutOfBoundsException("count:" + count + " ,limit:" + buffer.length);
        }
    }

    public byte[] getBuffer() {
        if (committed) {
            return null;
        }
        return buffer;
    }

}
