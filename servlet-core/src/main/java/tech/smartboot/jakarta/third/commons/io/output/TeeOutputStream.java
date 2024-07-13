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
 * Classic splitter of OutputStream. Named after the unix 'tee' 
 * command. It allows a stream to be branched off so there 
 * are now two streams.
 *
 * @version $Id: TeeOutputStream.java 1302056 2012-03-18 03:03:38Z ggregory $
 */
public class TeeOutputStream extends ProxyOutputStream {

    /** the second OutputStream to write to */
    protected OutputStream branch;

    /**
     * Constructs a TeeOutputStream.
     * @param out the main OutputStream
     * @param branch the second OutputStream
     */
    public TeeOutputStream(OutputStream out, OutputStream branch) {
        super(out);
        this.branch = branch;
    }

    /**
     * Write the bytes to both streams.
     * @param b the bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void write(byte[] b) throws IOException {
        super.write(b);
        this.branch.write(b);
    }

    /**
     * Write the specified bytes to both streams.
     * @param b the bytes to write
     * @param off The start offset
     * @param len The number of bytes to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void write(byte[] b, int off, int len) throws IOException {
        super.write(b, off, len);
        this.branch.write(b, off, len);
    }

    /**
     * Write a byte to both streams.
     * @param b the byte to write
     * @throws IOException if an I/O error occurs
     */
    @Override
    public synchronized void write(int b) throws IOException {
        super.write(b);
        this.branch.write(b);
    }

    /**
     * Flushes both streams.
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void flush() throws IOException {
        super.flush();
        this.branch.flush();
    }

    /**
     * Closes both output streams.
     * 
     * If closing the main output stream throws an exception, attempt to close the branch output stream.
     * 
     * If closing the main and branch output streams both throw exceptions, which exceptions is thrown by this method is
     * currently unspecified and subject to change.
     * 
     * @throws IOException
     *             if an I/O error occurs
     */
    @Override
    public void close() throws IOException {
        try {
            super.close();
        } finally {
            this.branch.close();
        }
    }

}
