/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.jakarta.plugins.license;

/**
 * 软件运行时过期
 *
 * @author 三刀
 * @version V1.0 , 2020/4/13
 */
public interface RuntimeExpireStrategy {
    void expire(LicenseEntity entity);
}
