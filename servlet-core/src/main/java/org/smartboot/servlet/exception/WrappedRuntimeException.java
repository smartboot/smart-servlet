/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: WrappedRuntimeException.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.exception;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class WrappedRuntimeException extends RuntimeException {
    private final Throwable throwable;

    public WrappedRuntimeException(Throwable cause) {
        this.throwable = cause;
    }

    public Throwable getThrowable() {
        return throwable;
    }
}
