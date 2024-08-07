/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.impl;

import jakarta.servlet.descriptor.JspConfigDescriptor;
import jakarta.servlet.descriptor.JspPropertyGroupDescriptor;
import jakarta.servlet.descriptor.TaglibDescriptor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class JspConfigDescriptorImpl implements JspConfigDescriptor {
    private final List<TaglibDescriptor> tagLibs = new ArrayList<>();
    private final List<JspPropertyGroupDescriptor> jspGroups = new ArrayList<>();

    @Override
    public Collection<TaglibDescriptor> getTaglibs() {
        return tagLibs;
    }

    @Override
    public Collection<JspPropertyGroupDescriptor> getJspPropertyGroups() {
        return jspGroups;
    }
}
