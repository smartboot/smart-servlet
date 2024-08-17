/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.conf;

import jakarta.servlet.annotation.ServletSecurity;

import java.util.ArrayList;
import java.util.List;

public class SecurityConstraint {
    private ServletSecurity.EmptyRoleSemantic emptyRoleSemantic = ServletSecurity.EmptyRoleSemantic.PERMIT;
    private ServletSecurity.TransportGuarantee transportGuarantee = ServletSecurity.TransportGuarantee.NONE;
    //    private final List<String> resourceNames = new ArrayList<>();
    private final List<UrlPattern> urlPatterns = new ArrayList<>();
    private final List<String> httpMethods = new ArrayList<>();

    private List<String> roleNames;

    public List<UrlPattern> getUrlPatterns() {
        return urlPatterns;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public void setRoleNames(List<String> roleNames) {
        this.roleNames = roleNames;
    }

    public ServletSecurity.EmptyRoleSemantic getEmptyRoleSemantic() {
        return emptyRoleSemantic;
    }

    public void setEmptyRoleSemantic(ServletSecurity.EmptyRoleSemantic emptyRoleSemantic) {
        this.emptyRoleSemantic = emptyRoleSemantic;
    }

    public ServletSecurity.TransportGuarantee getTransportGuarantee() {
        return transportGuarantee;
    }

    public void setTransportGuarantee(ServletSecurity.TransportGuarantee transportGuarantee) {
        this.transportGuarantee = transportGuarantee;
    }
}
