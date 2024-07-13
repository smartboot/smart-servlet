/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.commons.io.output;
 
import java.io.IOException;
import java.io.OutputStream;

/**
 * This OutputStream writes all data to the famous <b>/dev/null</b>.
 * <p>
 * This output stream has no destination (file/socket etc.) and all
 * bytes written to it are ignored and lost.
 * 
 * @version $Id: NullOutputStream.java 1302056 2012-03-18 03:03:38Z ggregory $
 */
public class NullOutputStream extends OutputStream {
    
    /**
     * A singleton.
     */
    public static final NullOutputStream NULL_OUTPUT_STREAM = new NullOutputStream();

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param b The bytes to write
     * @param off The start offset
     * @param len The number of bytes to write
     */
    @Override
    public void write(byte[] b, int off, int len) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param b The byte to write
     */
    @Override
    public void write(int b) {
        //to /dev/null
    }

    /**
     * Does nothing - output to <code>/dev/null</code>.
     * @param b The bytes to write
     * @throws IOException never
     */
    @Override
    public void write(byte[] b) throws IOException {
        //to /dev/null
    }

}
