/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.security;

import java.security.Principal;
import java.util.Set;

public class LoginAccount implements Principal {
    private final String name;
    private final String password;
    private final Set<String> roles;
    private final String authType;

    public LoginAccount(String name, String password, Set<String> roles, String authType) {
        this.name = name;
        this.password = password;
        this.roles = roles;
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
