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

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;

import java.io.IOException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/10
 */
public class ServletPrintWriter extends Writer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletPrintWriter.class);
    private final ServletOutputStreamImpl servletOutputStream;
    private final CharsetEncoder charsetEncoder;

    public ServletPrintWriter(ServletOutputStreamImpl servletOutputStream, String charset) {
        super(servletOutputStream);
        this.servletOutputStream = servletOutputStream;
        this.charsetEncoder = Charset.forName(charset).newEncoder();
        charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
        charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE);
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
            ByteBuffer virtualBuffer = ByteBuffer.allocate(buffer.remaining() < 32 ? 32 : buffer.remaining() << 1);

            //第二步：编码
            CoderResult result = charsetEncoder.encode(buffer, virtualBuffer, true);
            if (result.isError()) {
                LOGGER.info("encoding result:{} ,remaining", result);
            }

            //第三步：输出
            virtualBuffer.flip();
            servletOutputStream.write(virtualBuffer.array(), 0, virtualBuffer.remaining());
            if (buffer.hasRemaining()) {
                LOGGER.info("continue encoding ,remaining:" + buffer.remaining());
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
