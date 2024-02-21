/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package org.smartboot.servlet.sandbox;

import org.smartboot.http.common.logging.Logger;
import org.smartboot.http.common.logging.LoggerFactory;
import org.smartboot.servlet.impl.HttpServletRequestImpl;
import org.smartboot.servlet.provider.SecurityProvider;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class MockSecurityProvider implements SecurityProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(MockSecurityProvider.class);

    @Override
    public void login(String username, String password, HttpServletRequestImpl httpServletRequest) throws ServletException {
        LOGGER.warn("unSupport login");
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
        LOGGER.warn("unSupport logout");
    }

}
