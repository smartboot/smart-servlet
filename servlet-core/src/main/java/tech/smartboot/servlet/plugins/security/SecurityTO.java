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

import jakarta.servlet.annotation.ServletSecurity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SecurityTO {
    private Set<String> roles;
    private final List<String> httpMethods = new ArrayList<>();
    private ServletSecurity.EmptyRoleSemantic emptyRoleSemantic;
    private ServletSecurity.TransportGuarantee transportGuarantee;
}
