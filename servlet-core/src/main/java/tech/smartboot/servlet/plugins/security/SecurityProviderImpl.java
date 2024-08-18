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
import java.util.Arrays;
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
    private final Map<String, SecurityAccount> headerSecurities = new HashMap<>();
    private List<SecurityAccount> users = Arrays.asList(new SecurityAccount("j2ee", "j2ee", null, Set.of("Administrator", "Employee")), new SecurityAccount("javajoe", "javajoe", null, Set.of("VP", "Manager")));

    @Override
    public void addUser(String username, String password, Set<String> roles) {
        headerSecurities.put(username, new SecurityAccount(username, password, null, roles));
    }

    @Override
    public void init(List<SecurityConstraint> constraints) {

    }

    @Override
    public SecurityAccount login(String username, String password) throws ServletException {
        return users.stream().filter(user -> user.getUsername().equals(username) && user.getPassword().equals(password)).findFirst().orElse(null);
    }

    @Override
    public boolean authenticate(HttpServletRequestImpl httpServletRequest, HttpServletResponse response) throws IOException, ServletException {
        return false;
    }

    @Override
    public boolean isUserInRole(String role, LoginAccount loginAccount, HttpServletRequestImpl httpServletRequest) {
        if (loginAccount == null) {
            return false;
        }
        if (role == null || role.equals("*")) {
            return false;
        }
        if (role.equals("**")) {
            Set<String> roles = httpServletRequest.getServletContext().getDeploymentInfo().getSecurityRoles();
            if (!roles.contains("**")) {
                return true;
            }
        }

        return false;
    }

    @Override
    public void logout(HttpServletRequestImpl httpServletRequest) throws ServletException {

    }

    @Override
    public SecurityAccount login(HttpServletRequest request) throws ServletException {
        String authorization = request.getHeader("Authorization");
        if (StringUtils.isBlank(authorization)) {
            return null;
        }
        if (authorization.startsWith("Basic ")) {
            String[] auth = new String(Base64.getDecoder().decode(authorization.substring(6))).split(":");
            return users.stream().filter(user -> user.getUsername().equals(auth[0]) && user.getPassword().equals(auth[1])).findFirst().orElse(null);
        }
        return null;
    }
}
