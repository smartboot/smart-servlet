/*
 *  Copyright (C) [2022] smartboot [zhengjunweimail@163.com]
 *
 *  企业用户未经smartboot组织特别许可，需遵循AGPL-3.0开源协议合理合法使用本项目。
 *
 *   Enterprise users are required to use this project reasonably
 *   and legally in accordance with the AGPL-3.0 open source agreement
 *  without special permission from the smartboot organization.
 */

package tech.smartboot.servlet.plugins.license;

import java.security.MessageDigest;

public final class Md5 {
    private Md5() {

    }

    public static String md5(byte[] data) {
        final int m = 2;
        final int n = 4;
        final int a = 0xf;
        char[] hexDigits = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f'};

        try {
            MessageDigest mdTemp = MessageDigest.getInstance("MD5");
            mdTemp.update(data);
            byte[] md = mdTemp.digest();
            int j = md.length;
            char[] str = new char[j * m];
            int k = 0;
            for (byte b : md) {
                str[k++] = hexDigits[b >>> n & a];
                str[k++] = hexDigits[b & a];
            }
            return new String(str);
        } catch (Exception e) {
            return null;
        }
    }
}