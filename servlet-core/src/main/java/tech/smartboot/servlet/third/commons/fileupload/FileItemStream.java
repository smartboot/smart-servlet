/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.servlet.third.commons.fileupload;

import java.io.IOException;
import java.io.InputStream;

/**
 * <p> This interface provides access to a file or form item that was
 * received within a <code>multipart/form-data</code> POST request.
 * The items contents are retrieved by calling {@link #openStream()}.</p>
 * <p>Instances of this class are created by accessing the
 * iterator, returned by
 * {@link FileUploadBase#getItemIterator(RequestContext)}.</p>
 * <p><em>Note</em>: There is an interaction between the iterator and
 * its associated instances of {@link FileItemStream}: By invoking
 * {@link java.util.Iterator#hasNext()} on the iterator, you discard all data,
 * which hasn't been read so far from the previous data.</p>
 */
public interface FileItemStream extends FileItemHeadersSupport {

    /**
     * This exception is thrown, if an attempt is made to read
     * data from the {@link InputStream}, which has been returned
     * by {@link FileItemStream#openStream()}, after
     * {@link java.util.Iterator#hasNext()} has been invoked on the
     * iterator, which created the {@link FileItemStream}.
     */
    public static class ItemSkippedException extends IOException {

        /**
         * The exceptions serial version UID, which is being used
         * when serializing an exception instance.
         */
        private static final long serialVersionUID = -7280778431581963740L;

    }

    /**
     * Creates an {@link InputStream}, which allows to read the
     * items contents.
     *
     * @return The input stream, from which the items data may
     *   be read.
     * @throws IllegalStateException The method was already invoked on
     * this item. It is not possible to recreate the data stream.
     * @throws IOException An I/O error occurred.
     * @see ItemSkippedException
     */
    InputStream openStream() throws IOException;

    /**
     * Returns the content type passed by the browser or <code>null</code> if
     * not defined.
     *
     * @return The content type passed by the browser or <code>null</code> if
     *         not defined.
     */
    String getContentType();

    /**
     * Returns the original filename in the client's filesystem, as provided by
     * the browser (or other client software). In most cases, this will be the
     * base file name, without path information. However, some clients, such as
     * the Opera browser, do include path information.
     *
     * @return The original filename in the client's filesystem.
     */
    String getName();

    /**
     * Returns the name of the field in the multipart form corresponding to
     * this file item.
     *
     * @return The name of the form field.
     */
    String getFieldName();

    /**
     * Determines whether or not a <code>FileItem</code> instance represents
     * a simple form field.
     *
     * @return <code>true</code> if the instance represents a simple form
     *         field; <code>false</code> if it represents an uploaded file.
     */
    boolean isFormField();

}
