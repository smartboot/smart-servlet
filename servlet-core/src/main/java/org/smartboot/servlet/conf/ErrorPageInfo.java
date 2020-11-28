/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ErrorPageInfo.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.conf;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class ErrorPageInfo {
    private String location;
    private Integer errorCode;
    private String exceptionType;

    public ErrorPageInfo(String location, Integer errorCode, String exceptionType) {
        this.location = location;
        this.errorCode = errorCode;
        this.exceptionType = exceptionType;
    }

    public String getLocation() {
        return location;
    }

    public Integer getErrorCode() {
        return errorCode;
    }

    public String getExceptionType() {
        return exceptionType;
    }
}
