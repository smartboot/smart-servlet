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

import tech.smartboot.servlet.third.commons.io.IOUtils;
import tech.smartboot.servlet.third.commons.fileupload.FileItemStream;
import tech.smartboot.servlet.third.commons.fileupload.InvalidFileNameException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Utility class for working with streams.
 */
public final class Streams {

    /**
     * Private constructor, to prevent instantiation.
     * This class has only static methods.
     */
    private Streams() {
        // Does nothing
    }

    /**
     * Default buffer size for use in
     * {@link #copy(InputStream, OutputStream, boolean)}.
     */
    private static final int DEFAULT_BUFFER_SIZE = 8192;

    /**
     * Copies the contents of the given {@link InputStream}
     * to the given {@link OutputStream}. Shortcut for
     * <pre>
     *   copy(pInputStream, pOutputStream, new byte[8192]);
     * </pre>
     *
     * @param inputStream The input stream, which is being read.
     * It is guaranteed, that {@link InputStream#close()} is called
     * on the stream.
     * @param outputStream The output stream, to which data should
     * be written. May be null, in which case the input streams
     * contents are simply discarded.
     * @param closeOutputStream True guarantees, that {@link OutputStream#close()}
     * is called on the stream. False indicates, that only
     * {@link OutputStream#flush()} should be called finally.
     *
     * @return Number of bytes, which have been copied.
     * @throws IOException An I/O error occurred.
     */
    public static long copy(InputStream inputStream, OutputStream outputStream, boolean closeOutputStream)
            throws IOException {
        return copy(inputStream, outputStream, closeOutputStream, new byte[DEFAULT_BUFFER_SIZE]);
    }

    /**
     * Copies the contents of the given {@link InputStream}
     * to the given {@link OutputStream}.
     *
     * @param inputStream The input stream, which is being read.
     *   It is guaranteed, that {@link InputStream#close()} is called
     *   on the stream.
     * @param outputStream The output stream, to which data should
     *   be written. May be null, in which case the input streams
     *   contents are simply discarded.
     * @param closeOutputStream True guarantees, that {@link OutputStream#close()}
     *   is called on the stream. False indicates, that only
     *   {@link OutputStream#flush()} should be called finally.
     * @param buffer Temporary buffer, which is to be used for
     *   copying data.
     * @return Number of bytes, which have been copied.
     * @throws IOException An I/O error occurred.
     */
    public static long copy(InputStream inputStream,
            OutputStream outputStream, boolean closeOutputStream,
            byte[] buffer)
    throws IOException {
        OutputStream out = outputStream;
        InputStream in = inputStream;
        try {
            long total = 0;
            for (;;) {
                int res = in.read(buffer);
                if (res == -1) {
                    break;
                }
                if (res > 0) {
                    total += res;
                    if (out != null) {
                        out.write(buffer, 0, res);
                    }
                }
            }
            if (out != null) {
                if (closeOutputStream) {
                    out.close();
                } else {
                    out.flush();
                }
                out = null;
            }
            in.close();
            in = null;
            return total;
        } finally {
            IOUtils.closeQuietly(in);
            if (closeOutputStream) {
                IOUtils.closeQuietly(out);
            }
        }
    }

    /**
     * This convenience method allows to read a
     * {@link FileItemStream}'s
     * content into a string. The platform's default character encoding
     * is used for converting bytes into characters.
     *
     * @param inputStream The input stream to read.
     * @see #asString(InputStream, String)
     * @return The streams contents, as a string.
     * @throws IOException An I/O error occurred.
     */
    public static String asString(InputStream inputStream) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(inputStream, baos, true);
        return baos.toString();
    }

    /**
     * This convenience method allows to read a
     * {@link FileItemStream}'s
     * content into a string, using the given character encoding.
     *
     * @param inputStream The input stream to read.
     * @param encoding The character encoding, typically "UTF-8".
     * @see #asString(InputStream)
     * @return The streams contents, as a string.
     * @throws IOException An I/O error occurred.
     */
    public static String asString(InputStream inputStream, String encoding) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        copy(inputStream, baos, true);
        return baos.toString(encoding);
    }

    /**
     * Checks, whether the given file name is valid in the sense,
     * that it doesn't contain any NUL characters. If the file name
     * is valid, it will be returned without any modifications. Otherwise,
     * an {@link InvalidFileNameException} is raised.
     *
     * @param fileName The file name to check
     * @return Unmodified file name, if valid.
     * @throws InvalidFileNameException The file name was found to be invalid.
     */
    public static String checkFileName(String fileName) {
        if (fileName != null  &&  fileName.indexOf('\u0000') != -1) {
            // pFileName.replace("\u0000", "\\0")
            final StringBuilder sb = new StringBuilder();
            for (int i = 0;  i < fileName.length();  i++) {
                char c = fileName.charAt(i);
                switch (c) {
                    case 0:
                        sb.append("\\0");
                        break;
                    default:
                        sb.append(c);
                        break;
                }
            }
            throw new InvalidFileNameException(fileName,
                    "Invalid file name: " + sb);
        }
        return fileName;
    }

}
