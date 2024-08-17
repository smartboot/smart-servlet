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

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smartboot.http.common.utils.StringUtils;
import tech.smartboot.servlet.conf.SecurityConstraint;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.provider.SecurityProvider;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SecurityProviderImpl implements SecurityProvider {
    private Map<String, SecurityTO> exactPathSecurities = new HashMap<>();
    private Map<String, SecurityTO> prefixPathSecurities = new HashMap<>();
    private Map<String, SecurityTO> extensionSecurities = new HashMap<>();
    private Map<String, SecurityTO> methodSecurities = new HashMap<>();
    private final Map<String, UserTO> headerSecurities = new HashMap<>();

    @Override
    public void addUser(String username, String password, Set<String> roles) {
        headerSecurities.put(username, new UserTO(username, password, roles));
    }

    @Override
    public void init(List<SecurityConstraint> constraints) {

    }

    @Override
    public void login(String username, String password, HttpServletRequestImpl httpServletRequest) throws ServletException {

    }

    @Override
    public boolean authenticate(HttpServletRequestImpl httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public boolean isUserInRole(String role, HttpServletRequestImpl httpServletRequest) {
        return false;
    }

    @Override
    public void logout(HttpServletRequestImpl httpServletRequest) throws ServletException {

    }

    @Override
    public UserTO getUser(HttpServletRequest request) {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)) {
            return null;
        }
        UserTO userTO = new UserTO("j2ee", "j2ee", Set.of("Administrator", "Employee"));
        if (authorization.startsWith("Basic ")) {
            System.out.println(new String(Base64.getDecoder().decode(authorization.substring(6))));

        }
        return userTO;
    }
}
