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

package tech.smartboot.servlet.plugins.security;

import java.util.Collections;
import java.util.Set;

/**
 * 当前容器配置的认证信息
 *
 * @author 三刀
 */
public class SecurityAccount {
    public static final String AUTH_TYPE_BASIC = "BASIC";
    public static final String FORM = "FORM";
    public static final String DIGEST = "DIGEST";
    public static final String CLIENT_CERT = "CLIENT_CERT";
    public static final String NONE = "NONE";
    private final String username;
    private final String password;
    private final String authType;
    private final Set<String> roles;

    public SecurityAccount(String username, String password, String authType, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.authType = authType;
        this.roles = Collections.unmodifiableSet(roles);
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
