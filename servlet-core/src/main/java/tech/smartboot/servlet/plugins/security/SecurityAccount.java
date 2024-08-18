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
