/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: ServletPathMatcher.java
 * Date: 2020-11-28
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.util;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/14
 */
public class ServletPathMatcher {

    private final static ServletPathMatcher INSTANCE = new ServletPathMatcher();

    public static ServletPathMatcher getInstance() {
        return INSTANCE;
    }

    public static boolean isAbsoluteUrl(String location) {
        if (location != null && location.length() > 0 && location.contains(":")) {
            try {
                URI uri = new URI(location);
                return uri.getScheme() != null;
            } catch (URISyntaxException e) {
            }
        }
        return false;
    }

    /**
     * <p>
     * three type: endsWithMatch(eg. /xxx*=/xxx/xyz), startsWithMatch(eg.
     * *.xxx=abc.xxx), equals(eg. /xxx=/xxx).
     * </p>
     * <b>Notice</b>: *xxx* will match *xxxyyyy. endsWithMatch first.
     */
    public boolean matches(String pattern, String source) {
        if (pattern == null || source == null) {
            return false;
        }
        pattern = pattern.trim();
        source = source.trim();
        if (pattern.endsWith("*")) {
            // pattern: /druid* source:/druid/index.html
            int length = pattern.length() - 1;
            if (source.length() >= length) {
                if (pattern.substring(0, length).equals(
                        source.substring(0, length))) {
                    return true;
                }
            }
        } else if (pattern.startsWith("*")) {
            // pattern: *.html source:/xx/xx.html
            int length = pattern.length() - 1;
            if (source.length() >= length
                    && source.endsWith(pattern.substring(1))) {
                return true;
            }
        } else if (pattern.contains("*")) {
            // pattern:  /druid/*/index.html source:/druid/admin/index.html
            int start = pattern.indexOf("*");
            int end = pattern.lastIndexOf("*");
            if (source.startsWith(pattern.substring(0, start))
                    && source.endsWith(pattern.substring(end + 1))) {
                return true;
            }
        } else {
            // pattern: /druid/index.html source:/druid/index.html
            if (pattern.equals(source)) {
                return true;
            }
        }
        return false;
    }
}