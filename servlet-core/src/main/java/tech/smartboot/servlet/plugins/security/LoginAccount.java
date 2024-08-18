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
    private final String username;
    private final String password;
    private final Set<String> roles;

    public LoginAccount(String username, String password, Set<String> roles) {
        this.username = username;
        this.password = password;
        this.roles = roles;
    }


    public String getPassword() {
        return password;
    }

    public Set<String> getRoles() {
        return roles;
    }

    @Override
    public String getName() {
        return username;
    }
}
