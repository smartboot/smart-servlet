/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.fileupload.util;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * An input stream, which limits its data size. This stream is
 * used, if the content length is unknown.
 */
public abstract class LimitedInputStream extends FilterInputStream implements Closeable {

    /**
     * The maximum size of an item, in bytes.
     */
    private final long sizeMax;

    /**
     * The current number of bytes.
     */
    private long count;

    /**
     * Whether this stream is already closed.
     */
    private boolean closed;

    /**
     * Creates a new instance.
     *
     * @param inputStream The input stream, which shall be limited.
     * @param pSizeMax The limit; no more than this number of bytes
     *   shall be returned by the source stream.
     */
    public LimitedInputStream(InputStream inputStream, long pSizeMax) {
        super(inputStream);
        sizeMax = pSizeMax;
    }

    /**
     * Called to indicate, that the input streams limit has
     * been exceeded.
     *
     * @param pSizeMax The input streams limit, in bytes.
     * @param pCount The actual number of bytes.
     * @throws IOException The called method is expected
     *   to raise an IOException.
     */
    protected abstract void raiseError(long pSizeMax, long pCount)
            throws IOException;

    /**
     * Called to check, whether the input streams
     * limit is reached.
     *
     * @throws IOException The given limit is exceeded.
     */
    private void checkLimit() throws IOException {
        if (count > sizeMax) {
            raiseError(sizeMax, count);
        }
    }

    /**
     * Reads the next byte of data from this input stream. The value
     * byte is returned as an <code>int</code> in the range
     * <code>0</code> to <code>255</code>. If no byte is available
     * because the end of the stream has been reached, the value
     * <code>-1</code> is returned. This method blocks until input data
     * is available, the end of the stream is detected, or an exception
     * is thrown.
     * <p>
     * This method
     * simply performs <code>in.read()</code> and returns the result.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @throws  IOException  if an I/O error occurs.
     * @see        FilterInputStream#in
     */
    @Override
    public int read() throws IOException {
        int res = super.read();
        if (res != -1) {
            count++;
            checkLimit();
        }
        return res;
    }

    /**
     * Reads up to <code>len</code> bytes of data from this input stream
     * into an array of bytes. If <code>len</code> is not zero, the method
     * blocks until some input is available; otherwise, no
     * bytes are read and <code>0</code> is returned.
     * <p>
     * This method simply performs <code>in.read(b, off, len)</code>
     * and returns the result.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   The start offset in the destination array
     *                   <code>b</code>.
     * @param      len   the maximum number of bytes read.
     * @return     the total number of bytes read into the buffer, or
     *             <code>-1</code> if there is no more data because the end of
     *             the stream has been reached.
     * @throws  NullPointerException If <code>b</code> is <code>null</code>.
     * @throws  IndexOutOfBoundsException If <code>off</code> is negative,
     * <code>len</code> is negative, or <code>len</code> is greater than
     * <code>b.length - off</code>
     * @throws  IOException  if an I/O error occurs.
     * @see        FilterInputStream#in
     */
    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int res = super.read(b, off, len);
        if (res > 0) {
            count += res;
            checkLimit();
        }
        return res;
    }

    /**
     * Returns, whether this stream is already closed.
     *
     * @return True, if the stream is closed, otherwise false.
     * @throws IOException An I/O error occurred.
     */
    @Override
    public boolean isClosed() throws IOException {
        return closed;
    }

    /**
     * Closes this input stream and releases any system resources
     * associated with the stream.
     * This
     * method simply performs <code>in.close()</code>.
     *
     * @throws  IOException  if an I/O error occurs.
     * @see        FilterInputStream#in
     */
    @Override
    public void close() throws IOException {
        closed = true;
        super.close();
    }

}
