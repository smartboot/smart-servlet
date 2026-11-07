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

import jakarta.servlet.annotation.ServletSecurity;

import java.util.ArrayList;
import java.util.List;

public class SecurityConstraint {
    //13.8 没有指定角色的授权约束表示在任何情况下都不允许访问受约束请求
    private ServletSecurity.EmptyRoleSemantic emptyRoleSemantic = ServletSecurity.EmptyRoleSemantic.DENY;
    private ServletSecurity.TransportGuarantee transportGuarantee = ServletSecurity.TransportGuarantee.NONE;
    //    private final List<String> resourceNames = new ArrayList<>();
    private final List<UrlPattern> urlPatterns = new ArrayList<>();
    private final List<String> httpMethods = new ArrayList<>();
    private final List<String> httpMethodOmissions = new ArrayList();

    private List<String> roleNames;

    public List<UrlPattern> getUrlPatterns() {
        return urlPatterns;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public List<String> getHttpMethodOmissions() {
        return httpMethodOmissions;
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
