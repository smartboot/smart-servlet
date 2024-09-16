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
import jakarta.servlet.ServletResponse;
import jakarta.servlet.ServletResponseWrapper;
import jakarta.servlet.annotation.ServletSecurity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.smartboot.http.common.enums.HeaderNameEnum;
import org.smartboot.http.common.enums.HttpStatus;
import org.smartboot.http.common.utils.StringUtils;
import tech.smartboot.servlet.SmartHttpServletRequest;
import tech.smartboot.servlet.conf.DeploymentInfo;
import tech.smartboot.servlet.conf.LoginConfig;
import tech.smartboot.servlet.conf.SecurityConstraint;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.UrlPattern;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.provider.SecurityProvider;
import tech.smartboot.servlet.util.CollectionUtils;
import tech.smartboot.servlet.util.PathMatcherUtil;

import java.io.IOException;
import java.util.ArrayList;
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
    private LoginConfig loginConfig;
    private List<SecurityConstraint> constraints = new ArrayList<>();

    @Override
    public void addUser(String username, String password, Set<String> roles) {
        headerSecurities.put(username, new SecurityAccount(username, password, null, roles));
    }

    @Override
    public void init(DeploymentInfo deploymentInfo) {
        this.constraints = deploymentInfo.getSecurityConstraints();
        this.loginConfig = deploymentInfo.getLoginConfig();
    }

    @Override
    public SecurityAccount login(String username, String password) throws ServletException {
        if (username == null || password == null) {
            throw new ServletException();
        }
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
        String roleLink = httpServletRequest.getServletInfo().getSecurityRoles().get(role);
        if (roleLink == null) {
            return loginAccount.getRoles().contains(role);
        }
        System.out.println(roleLink);
        System.out.println(httpServletRequest.getServletInfo().getSecurityRoles());
        return loginAccount.getRoles().contains(roleLink) || loginAccount.getRoles().contains(role);
    }

    @Override
    public void logout(HttpServletRequestImpl httpServletRequest) throws ServletException {

    }

    @Override
    public boolean login(SmartHttpServletRequest request, ServletResponse resp, ServletInfo servletInfo) throws ServletException, IOException {
        while (resp instanceof ServletResponseWrapper) {
            resp = ((ServletResponseWrapper) resp).getResponse();
        }
        HttpServletResponse response = (HttpServletResponse) resp;
        // servlet 存在权限验证
//        if (!servletInfo.getSecurityRoles().isEmpty()) {
//            LoginAccount loginAccount = login(request, response);
//            if (loginAccount == null) {
//                return false;
//            }
//            boolean match = false;
//            for (String role : servletInfo.getSecurityRoles().keySet()) {
//                if (loginAccount.getRoles().contains(role) || loginAccount.getRoles().contains(servletInfo.getSecurityRoles().get(role))) {
//                    match = true;
//                    break;
//                }
//            }
//            if (!match) {
//                return false;
//            }
//        }
        boolean ok = check(request, response, servletInfo.getSecurityConstraints());
        if (!ok) {
            return ok;
        }
        return check(request, response, constraints.stream().filter(securityConstraint -> {
            for (UrlPattern urlPattern : securityConstraint.getUrlPatterns()) {
                if (PathMatcherUtil.matches(request, urlPattern)) {
                    return true;
                }
            }
            return false;
        }).toList());
    }

    private LoginAccount login(SmartHttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        if (loginConfig != null) {
//            if ("FORM".equals(loginConfig.getAuthMethod())) {
//                return users.stream().filter(user -> user.getUsername().equals(request.getParameter("j_username")) && user.getPassword().equals(request.getParameter("j_password"))).findFirst().orElse(null);
//            }
        }
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.startsWith("Basic ")) {
            String[] auth = new String(Base64.getDecoder().decode(authorization.substring(6))).split(":");
            SecurityAccount securityAccount = users.stream().filter(user -> user.getUsername().equals(auth[0]) && user.getPassword().equals(auth[1])).findFirst().orElse(null);
            if (securityAccount != null) {
                LoginAccount account = new LoginAccount(securityAccount.getUsername(), securityAccount.getPassword(), securityAccount.getRoles(), HttpServletRequest.BASIC_AUTH);
                request.setLoginAccount(account);
                return account;
            }
        }
        if (loginConfig == null) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
        } else if (StringUtils.equals(loginConfig.getAuthMethod(), SecurityAccount.AUTH_TYPE_BASIC)) {
            response.setHeader(HeaderNameEnum.WWW_AUTHENTICATE.getName(), "Basic realm=\"" + loginConfig.getRealmName() + "\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value());
        } else if (authorization == null && StringUtils.isNotBlank(loginConfig.getLoginPage())) {
            request.getSession().setAttribute(SecurityProvider.LOGIN_REDIRECT_URI, request.getRequestURI().substring(request.getContextPath().length()));
            request.getSession().setAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD, request.getMethod());
            request.getRequestDispatcher(loginConfig.getLoginPage()).forward(request, response);
        } else if (authorization != null && StringUtils.isNotBlank(loginConfig.getErrorPage())) {
            request.getSession().setAttribute(SecurityProvider.LOGIN_REDIRECT_URI, request.getRequestURI().substring(request.getContextPath().length()));
            request.getSession().setAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD, request.getMethod());
            request.getRequestDispatcher(loginConfig.getErrorPage()).forward(request, response);
        } else {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }

    private boolean check(SmartHttpServletRequest request, HttpServletResponse response, List<SecurityConstraint> constraints) throws IOException, ServletException {
        if (constraints.isEmpty()) {
            return true;
        }

        if (constraints.stream().anyMatch(securityConstraint -> CollectionUtils.isEmpty(securityConstraint.getRoleNames()) && securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.DENY)) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return false;
        }

        constraints = constraints.stream().filter(securityConstraint -> !securityConstraint.getHttpMethodOmissions().contains(request.getMethod())).toList();
        //不存在匹配的安全约束
        if (constraints.isEmpty()) {
//            response.sendError(HttpStatus.FORBIDDEN.value());
            return true;
        }
        //提取匹配HttpMethod的安全约束
        constraints = constraints.stream().filter(securityConstraint -> CollectionUtils.isEmpty(securityConstraint.getHttpMethods()) || securityConstraint.getHttpMethods().contains(request.getMethod())).toList();
        if (constraints.isEmpty()) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return false;
        }

        //role为空且为DENY，或者不包含有效method
        if (constraints.stream().anyMatch(securityConstraint -> CollectionUtils.isEmpty(securityConstraint.getRoleNames()) && securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.DENY)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        constraints = constraints.stream().filter(securityConstraint -> CollectionUtils.isNotEmpty(securityConstraint.getRoleNames())).toList();
        //全部constraints的role都为空，认证通过
        if (constraints.isEmpty()) {
            return true;
        }

        //角色校验
        LoginAccount account = (LoginAccount) request.getUserPrincipal();
        if (account == null) {
            account = login(request, response);
            if (account == null) {
                return false;
            }
        }

        LoginAccount finalAccount = account;
        long count = constraints.stream().filter(securityConstraint -> {
            if (securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.PERMIT && CollectionUtils.isEmpty(securityConstraint.getRoleNames())) {
                return true;
            }
            if (securityConstraint.getRoleNames().contains("*")) {
                return true;
            }
            for (String role : securityConstraint.getRoleNames()) {
                if (finalAccount.getRoles().contains(role)) {
                    return true;
                }
            }
            return false;
        }).count();
        if (count == 0) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }
}
