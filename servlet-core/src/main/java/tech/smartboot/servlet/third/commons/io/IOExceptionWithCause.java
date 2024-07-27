/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.third.commons.io;

import java.io.IOException;

/**
 * Subclasses IOException with the {@link Throwable} constructors missing before Java 6. If you are using Java 6,
 * consider this class deprecated and use {@link IOException}.
 * 
 * @version $Id: IOExceptionWithCause.java 1304052 2012-03-22 20:55:29Z ggregory $
 * @since 1.4
 */
public class IOExceptionWithCause extends IOException {

    /**
     * Defines the serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new instance with the given message and cause.
     * <p>
     * As specified in {@link Throwable}, the message in the given <code>cause</code> is not used in this instance's
     * message.
     * </p>
     * 
     * @param message
     *            the message (see {@link #getMessage()})
     * @param cause
     *            the cause (see {@link #getCause()}). A <code>null</code> value is allowed.
     */
    public IOExceptionWithCause(String message, Throwable cause) {
        super(message);
        this.initCause(cause);
    }

    /**
     * Constructs a new instance with the given cause.
     * <p>
     * The message is set to <code>cause==null ? null : cause.toString()</code>, which by default contains the class
     * and message of <code>cause</code>. This constructor is useful for call sites that just wrap another throwable.
     * </p>
     * 
     * @param cause
     *            the cause (see {@link #getCause()}). A <code>null</code> value is allowed.
     */
    public IOExceptionWithCause(Throwable cause) {
        super(cause == null ? null : cause.toString());
        this.initCause(cause);
    }

}
