/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.conf;

/**
 * @author 三刀
 * @version V1.0 , 2019/12/13
 */
public class ErrorPageInfo {
    private final String location;
    private final Integer errorCode;
    private final String exceptionType;

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
