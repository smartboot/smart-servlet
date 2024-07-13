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

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * Exception for errors encountered while processing the request.
 */
public class FileUploadException extends Exception {

    /**
     * Serial version UID, being used, if the exception
     * is serialized.
     */
    private static final long serialVersionUID = 8881893724388807504L;

    /**
     * The exceptions cause. We overwrite the cause of
     * the super class, which isn't available in Java 1.3.
     */
    private final Throwable cause;

    /**
     * Constructs a new <code>FileUploadException</code> without message.
     */
    public FileUploadException() {
        this(null, null);
    }

    /**
     * Constructs a new <code>FileUploadException</code> with specified detail
     * message.
     *
     * @param msg the error message.
     */
    public FileUploadException(final String msg) {
        this(msg, null);
    }

    /**
     * Creates a new <code>FileUploadException</code> with the given
     * detail message and cause.
     *
     * @param msg The exceptions detail message.
     * @param cause The exceptions cause.
     */
    public FileUploadException(String msg, Throwable cause) {
        super(msg);
        this.cause = cause;
    }

    /**
     * Prints this throwable and its backtrace to the specified print stream.
     *
     * @param stream <code>PrintStream</code> to use for output
     */
    @Override
    public void printStackTrace(PrintStream stream) {
        super.printStackTrace(stream);
        if (cause != null) {
            stream.println("Caused by:");
            cause.printStackTrace(stream);
        }
    }

    /**
     * Prints this throwable and its backtrace to the specified
     * print writer.
     *
     * @param writer <code>PrintWriter</code> to use for output
     */
    @Override
    public void printStackTrace(PrintWriter writer) {
        super.printStackTrace(writer);
        if (cause != null) {
            writer.println("Caused by:");
            cause.printStackTrace(writer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Throwable getCause() {
        return cause;
    }

}
