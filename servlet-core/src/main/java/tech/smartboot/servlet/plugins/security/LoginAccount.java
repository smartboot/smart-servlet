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

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

public class LoginAccount implements Principal {
    private final String name;
    private final String password;
    private final Set<String> roles;
    private final String authType;

    public LoginAccount(String name, String password, Set<String> roles, String authType) {
        this.name = name;
        this.password = password;
        this.roles = roles == null ? Collections.emptySet() : roles;
        this.authType = authType;
    }


    public String getPassword() {
        return password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getName() {
        return name;
    }

    public String getAuthType() {
        return authType;
    }
}
