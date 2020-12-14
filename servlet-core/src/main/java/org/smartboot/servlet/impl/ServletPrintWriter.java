/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletPrintWriter.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.impl;

import org.smartboot.servlet.ContainerRuntime;
import org.smartboot.socket.buffer.VirtualBuffer;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/10
 */
public class ServletPrintWriter extends Writer {
    private final ServletOutputStreamImpl servletOutputStream;
    private final CharsetEncoder charsetEncoder;
    private final ContainerRuntime containerRuntime;
    private VirtualBuffer virtualBuffer;

    public ServletPrintWriter(ServletOutputStreamImpl servletOutputStream, String charset, ContainerRuntime containerRuntime) {
        super(servletOutputStream);
        this.containerRuntime = containerRuntime;
        this.servletOutputStream = servletOutputStream;
        this.charsetEncoder = Charset.forName(charset).newEncoder();
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        write(CharBuffer.wrap(cbuf, off, len));
    }

    @Override
    public void write(String str, int off, int len) throws IOException {
        if (len == 0) {
            return;
        }
        write(CharBuffer.wrap(str, off, len));
    }

    private void write(CharBuffer buffer) throws IOException {
        while (buffer.hasRemaining()) {
            VirtualBuffer virtualBuffer;
            //第一步：匹配VirtualBuffer
            boolean committed = servletOutputStream.isCommitted();
            if (committed) {
                //一个中文转成2个字节，预申请2倍空间
                virtualBuffer = containerRuntime.getMemoryPoolProvider().getBufferPage().allocate(buffer.remaining() << 1);
            } else {
                //未提交前写入暂存区
                if (this.virtualBuffer == null) {
                    this.virtualBuffer = VirtualBuffer.wrap(ByteBuffer.wrap(servletOutputStream.getBuffer()));
                }
                this.virtualBuffer.buffer().clear().position(servletOutputStream.getCount());
                virtualBuffer = this.virtualBuffer;
            }

            //第二步：编码
            charsetEncoder.encode(buffer, virtualBuffer.buffer(), true);

            //第三步：输出
            virtualBuffer.buffer().flip();
            if (committed) {
                servletOutputStream.write(virtualBuffer);
            } else {
                //更新缓冲区计数
                servletOutputStream.setCount(virtualBuffer.buffer().remaining());
                //释放内存
                if (servletOutputStream.isCommitted()) {
                    this.virtualBuffer = null;
                }
            }
            if (buffer.hasRemaining()) {
                System.out.println("aaa " + buffer.remaining());
            }
        }
    }

    @Override
    public void flush() throws IOException {
        servletOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        servletOutputStream.close();
    }
}
