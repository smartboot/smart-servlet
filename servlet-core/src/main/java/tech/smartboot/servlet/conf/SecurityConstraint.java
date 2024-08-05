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

import java.util.ArrayList;
import java.util.List;

public class SecurityConstraint {
    private final List<String> resourceNames = new ArrayList<>();
    private final List<String> urlPatterns = new ArrayList<>();
    private final List<String> httpMethods = new ArrayList<>();

    private final List<String> roleNames = new ArrayList<>();
    private final List<String> transportGuarantees = new ArrayList<>();

    public List<String> getUrlPatterns() {
        return urlPatterns;
    }

    public List<String> getHttpMethods() {
        return httpMethods;
    }

    public List<String> getResourceNames() {
        return resourceNames;
    }

    public List<String> getRoleNames() {
        return roleNames;
    }

    public List<String> getTransportGuarantees() {
        return transportGuarantees;
    }
}
