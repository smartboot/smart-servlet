/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet;

import jakarta.servlet.http.HttpServletRequest;
import org.smartboot.socket.util.Attachment;
import tech.smartboot.servlet.conf.ServletInfo;
import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.plugins.security.LoginAccount;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/22
 */
public interface SmartHttpServletRequest extends HttpServletRequest {
    void setRequestUri(String requestUri);

    void setServletInfo(ServletInfo servletInfo);

    /**
     * 获取附件对象
     *
     * @return 附件
     */
    Attachment getAttachment();

    /**
     * 存放附件，支持任意类型
     *
     * @param attachment 附件对象
     */
    void setAttachment(Attachment attachment);

    void setServletMappingInfo(ServletMappingInfo servletMappingInfo);

    void setAsyncSupported(boolean supported);

    void setLoginAccount(LoginAccount loginAccount);

}
