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
import tech.smartboot.feat.core.common.FeatUtils;
import tech.smartboot.feat.core.common.HeaderName;
import tech.smartboot.feat.core.common.HttpStatus;
import tech.smartboot.servlet.SmartHttpServletRequest;
import tech.smartboot.servlet.conf.DeploymentInfo;
import tech.smartboot.servlet.conf.LoginConfig;
import tech.smartboot.servlet.conf.SecurityConstraint;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.UrlPattern;
import tech.smartboot.servlet.impl.HttpServletRequestImpl;
import tech.smartboot.servlet.provider.SecurityProvider;
import tech.smartboot.servlet.util.PathMatcherUtil;

import java.io.IOException;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
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
    private final DeploymentInfo deploymentInfo;

    public SecurityProviderImpl(DeploymentInfo deploymentInfo) {
        this.deploymentInfo = deploymentInfo;
    }

    @Override
    public void addUser(String username, String password, Set<String> roles) {
        headerSecurities.put(username, new SecurityAccount(username, password, null, roles));
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
        if (deploymentInfo.getSecurityConstraints().isEmpty()) {
            return true;
        }
        return check(request, response, deploymentInfo.getSecurityConstraints().stream().filter(securityConstraint -> {
            for (UrlPattern urlPattern : securityConstraint.getUrlPatterns()) {
                if (PathMatcherUtil.matches(request, urlPattern)) {
                    return true;
                }
            }
            return false;
        }).toList());
    }

    private LoginAccount login(SmartHttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
        LoginConfig loginConfig = deploymentInfo.getLoginConfig();
        if (loginConfig != null) {
//            if ("FORM".equals(loginConfig.getAuthMethod())) {
//                return users.stream().filter(user -> user.getUsername().equals(request.getParameter("j_username")) && user.getPassword().equals(request.getParameter("j_password"))).findFirst().orElse(null);
//            }
        }
        if (loginConfig != null && "CLIENT-CERT".equals(loginConfig.getAuthMethod())) {
            List<X509Certificate> certificates = new ArrayList<>();
            for (Certificate certificate : request.getSslEngine().getSession().getPeerCertificates()) {
                if (certificate instanceof X509Certificate) {
                    certificates.add((X509Certificate) certificate);
                }
            }
            X509Certificate certificate = certificates.get(0);
            String name = certificate.getIssuerX500Principal().getName();
            LoginAccount account = new LoginAccount(certificate.getIssuerX500Principal().getName(), null, deploymentInfo.getSecurityRoleMapping().get(name), HttpServletRequest.CLIENT_CERT_AUTH);
            request.setLoginAccount(account);
            request.setAttribute("jakarta.servlet.request.X509Certificate", certificates.toArray(new X509Certificate[certificates.size()]));
            request.setAttribute("jakarta.servlet.request.cipher_suite", request.getSslEngine().getSession().getCipherSuite());
            request.setAttribute("jakarta.servlet.request.key_size", calculateKeySize(request.getSslEngine().getSession().getCipherSuite()));
            byte[] sessionId = request.getSslEngine().getSession().getId();
            request.setAttribute("jakarta.servlet.request.ssl_session_id", sessionId != null ? convertToHexString(sessionId) : null);
            return account;
        } else {

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
        } else if (FeatUtils.equals(loginConfig.getAuthMethod(), SecurityAccount.AUTH_TYPE_BASIC)) {
            response.setHeader(HeaderName.WWW_AUTHENTICATE.getName(), "Basic realm=\"" + loginConfig.getRealmName() + "\"");
            response.sendError(HttpStatus.UNAUTHORIZED.value());
        } else if (authorization == null && FeatUtils.isNotBlank(loginConfig.getLoginPage())) {
            request.getSession().setAttribute(SecurityProvider.LOGIN_REDIRECT_URI, request.getRequestURI().substring(request.getContextPath().length()));
            request.getSession().setAttribute(SecurityProvider.LOGIN_REDIRECT_METHOD, request.getMethod());
            request.getRequestDispatcher(loginConfig.getLoginPage()).forward(request, response);
        } else if (authorization != null && FeatUtils.isNotBlank(loginConfig.getErrorPage())) {
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

        if (constraints.stream().anyMatch(securityConstraint -> (securityConstraint.getHttpMethods().isEmpty() || securityConstraint.getHttpMethods().contains(request.getMethod())) && FeatUtils.isEmpty(securityConstraint.getRoleNames()) && securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.DENY)) {
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
        constraints = constraints.stream().filter(securityConstraint -> FeatUtils.isEmpty(securityConstraint.getHttpMethods()) || securityConstraint.getHttpMethods().contains(request.getMethod())).toList();
        if (constraints.isEmpty()) {
            response.sendError(HttpStatus.FORBIDDEN.value());
            return false;
        }

        //role为空且为DENY，或者不包含有效method
        if (constraints.stream().anyMatch(securityConstraint -> FeatUtils.isEmpty(securityConstraint.getRoleNames()) && securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.DENY)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value());
            return false;
        }

        constraints = constraints.stream().filter(securityConstraint -> FeatUtils.isNotEmpty(securityConstraint.getRoleNames())).toList();
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
            if (securityConstraint.getEmptyRoleSemantic() == ServletSecurity.EmptyRoleSemantic.PERMIT && FeatUtils.isEmpty(securityConstraint.getRoleNames())) {
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

    static int calculateKeySize(String cipherSuite) {
        // Roughly ordered from most common to least common.
        if (cipherSuite == null) {
            return 0;
            //  TLS 1.3: https://wiki.openssl.org/index.php/TLS1.3
        } else if (cipherSuite.equals("TLS_AES_256_GCM_SHA384")) {
            return 256;
        } else if (cipherSuite.equals("TLS_CHACHA20_POLY1305_SHA256")) {
            return 256;
        } else if (cipherSuite.startsWith("TLS_AES_128_")) {
            return 128;
            //  TLS <1.3
        } else if (cipherSuite.contains("WITH_AES_128_")) {
            return 128;
        } else if (cipherSuite.contains("WITH_AES_256_")) {
            return 256;
        } else if (cipherSuite.contains("WITH_3DES_EDE_CBC_")) {
            return 168;
        } else if (cipherSuite.contains("WITH_RC4_128_")) {
            return 128;
        } else if (cipherSuite.contains("WITH_DES_CBC_")) {
            return 56;
        } else if (cipherSuite.contains("WITH_DES40_CBC_")) {
            return 40;
        } else if (cipherSuite.contains("WITH_RC4_40_")) {
            return 40;
        } else if (cipherSuite.contains("WITH_IDEA_CBC_")) {
            return 128;
        } else if (cipherSuite.contains("WITH_RC2_CBC_40_")) {
            return 40;
        } else {
            return 0;
        }
    }

    private static final char[] HEX_CHARS = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final byte[] HEX_BYTES = new byte[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    public static String convertToHexString(byte[] toBeConverted) {

        char[] converted = new char[toBeConverted.length * 2];
        for (int i = 0; i < toBeConverted.length; i++) {
            byte b = toBeConverted[i];
            converted[i * 2] = HEX_CHARS[b >> 4 & 0x0F];
            converted[i * 2 + 1] = HEX_CHARS[b & 0x0F];
        }

        return String.valueOf(converted);
    }
}
