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

import java.net.URL;
import java.util.List;

public class OrderMeta {
    private String name;
    private List<String> before;
    private boolean beforeOthers;
    private List<String> after;
    private boolean afterOthers;
    private URL url;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getBefore() {
        return before;
    }

    public void setBefore(List<String> before) {
        this.before = before;
    }

    public List<String> getAfter() {
        return after;
    }

    public void setAfter(List<String> after) {
        this.after = after;
    }

    public URL getUrl() {
        return url;
    }

    public void setUrl(URL url) {
        this.url = url;
    }

    public boolean isBeforeOthers() {
        return beforeOthers;
    }

    public void setBeforeOthers(boolean beforeOthers) {
        this.beforeOthers = beforeOthers;
    }

    public boolean isAfterOthers() {
        return afterOthers;
    }

    public void setAfterOthers(boolean afterOthers) {
        this.afterOthers = afterOthers;
    }
}
