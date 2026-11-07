/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package tech.smartboot.servlet.conf;

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
