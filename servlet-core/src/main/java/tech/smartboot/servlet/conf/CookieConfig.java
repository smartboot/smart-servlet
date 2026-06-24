/*
 * Copyright (c) 2017-2026, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: CookieConfig.java
 * Date: 2026-06-24
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package tech.smartboot.servlet.conf;

import java.util.HashMap;
import java.util.Map;

public class CookieConfig {
    private String name;
    private String domain;
    private String path;
    private boolean secure;
    private boolean httpOnly;
    private int maxAge;
    private Map<String, String> attributes = new HashMap<>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isSecure() {
        return secure;
    }

    public void setSecure(boolean secure) {
        this.secure = secure;
    }

    public boolean isHttpOnly() {
        return httpOnly;
    }

    public void setHttpOnly(boolean httpOnly) {
        this.httpOnly = httpOnly;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }

    public Map<String, String> getAttributes() {
        return attributes;
    }

}
