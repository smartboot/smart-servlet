/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.sandbox;

import tech.smartboot.servlet.conf.ServletMappingInfo;
import tech.smartboot.servlet.provider.MappingProvider;

public class MockMappingProvider implements MappingProvider {
    @Override
    public ServletMappingInfo match(String url) {
        return null;
    }
}
