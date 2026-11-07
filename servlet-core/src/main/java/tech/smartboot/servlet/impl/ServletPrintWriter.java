/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tech.smartboot.servlet.impl;

import tech.smartboot.feat.core.common.logging.Logger;
import tech.smartboot.feat.core.common.logging.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.CoderResult;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.UnsupportedCharsetException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/10
 */
public class ServletPrintWriter extends Writer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServletPrintWriter.class);
    private final ServletOutputStreamImpl servletOutputStream;
    private final CharsetEncoder charsetEncoder;

    public ServletPrintWriter(ServletOutputStreamImpl servletOutputStream, String charset) throws UnsupportedEncodingException {
        super(servletOutputStream);
        this.servletOutputStream = servletOutputStream;
        try {
            this.charsetEncoder = Charset.forName(charset).newEncoder();
            charsetEncoder.onUnmappableCharacter(CodingErrorAction.REPLACE);
            charsetEncoder.onMalformedInput(CodingErrorAction.REPLACE);
        } catch (UnsupportedCharsetException e) {
            throw new UnsupportedEncodingException(e.getMessage());
        }
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
