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

import tech.smartboot.servlet.third.commons.fileupload.disk.DiskFileItem;

import java.io.File;

/**
 * <p> The default implementation of the
 * {@link FileItem FileItem} interface.
 *
 * <p> After retrieving an instance of this class from a {@link
 * DiskFileUpload DiskFileUpload} instance (see
 * {@link DiskFileUpload
 * #parseRequest(javax.servlet.http.HttpServletRequest)}), you may
 * either request all contents of file at once using {@link #get()} or
 * request an {@link java.io.InputStream InputStream} with
 * {@link #getInputStream()} and process the file without attempting to load
 * it into memory, which may come handy with large files.
 *
 * @deprecated 1.1 Use <code>DiskFileItem</code> instead.
 */
@Deprecated
public class DefaultFileItem
    extends DiskFileItem {

    // ----------------------------------------------------------- Constructors

    /**
     * Constructs a new <code>DefaultFileItem</code> instance.
     *
     * @param fieldName     The name of the form field.
     * @param contentType   The content type passed by the browser or
     *                      <code>null</code> if not specified.
     * @param isFormField   Whether or not this item is a plain form field, as
     *                      opposed to a file upload.
     * @param fileName      The original filename in the user's filesystem, or
     *                      <code>null</code> if not specified.
     * @param sizeThreshold The threshold, in bytes, below which items will be
     *                      retained in memory and above which they will be
     *                      stored as a file.
     * @param repository    The data repository, which is the directory in
     *                      which files will be created, should the item size
     *                      exceed the threshold.
     *
     * @deprecated 1.1 Use <code>DiskFileItem</code> instead.
     */
    @Deprecated
    public DefaultFileItem(String fieldName, String contentType,
            boolean isFormField, String fileName, int sizeThreshold,
            File repository) {
        super(fieldName, contentType, isFormField, fileName, sizeThreshold,
                repository);
    }

}
