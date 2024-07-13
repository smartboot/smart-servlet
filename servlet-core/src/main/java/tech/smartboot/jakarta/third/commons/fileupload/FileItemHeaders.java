/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */
package tech.smartboot.jakarta.third.commons.fileupload;

import java.util.Iterator;

/**
 * <p> This class provides support for accessing the headers for a file or form
 * item that was received within a <code>multipart/form-data</code> POST
 * request.</p>
 *
 * @since 1.2.1
 */
public interface FileItemHeaders {

    /**
     * Returns the value of the specified part header as a <code>String</code>.
     *
     * If the part did not include a header of the specified name, this method
     * return <code>null</code>.  If there are multiple headers with the same
     * name, this method returns the first header in the item.  The header
     * name is case insensitive.
     *
     * @param name a <code>String</code> specifying the header name
     * @return a <code>String</code> containing the value of the requested
     *         header, or <code>null</code> if the item does not have a header
     *         of that name
     */
    String getHeader(String name);

    /**
     * <p>
     * Returns all the values of the specified item header as an
     * <code>Iterator</code> of <code>String</code> objects.
     * </p>
     * <p>
     * If the item did not include any headers of the specified name, this
     * method returns an empty <code>Iterator</code>. The header name is
     * case insensitive.
     * </p>
     *
     * @param name a <code>String</code> specifying the header name
     * @return an <code>Iterator</code> containing the values of the
     *         requested header. If the item does not have any headers of
     *         that name, return an empty <code>Iterator</code>
     */
    Iterator<String> getHeaders(String name);

    /**
     * <p>
     * Returns an <code>Iterator</code> of all the header names.
     * </p>
     *
     * @return an <code>Iterator</code> containing all of the names of
     *         headers provided with this file item. If the item does not have
     *         any headers return an empty <code>Iterator</code>
     */
    Iterator<String> getHeaderNames();

}
