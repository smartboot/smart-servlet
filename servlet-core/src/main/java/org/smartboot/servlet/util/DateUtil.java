/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: DateUtil.java
 * Date: 2020-11-14
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/10
 */
public class DateUtil {
    private static final TimeZone GMT = TimeZone.getTimeZone("GMT");
    private static final String[] DATE_FORMATS = new String[]{
            "EEE, dd MMM yyyy HH:mm:ss zzz",
            "EEE, dd-MMM-yy HH:mm:ss zzz",
            "EEE MMM dd HH:mm:ss yyyy"
    };
    private static final String DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";

    public static long parseDateHeader(String name, String value) {
        for (String dateFormat : DATE_FORMATS) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(dateFormat, Locale.US);
            simpleDateFormat.setTimeZone(GMT);
            try {
                return simpleDateFormat.parse(value).getTime();
            } catch (ParseException ex) {
                // ignore
            }
        }
        throw new IllegalArgumentException("Cannot parse date value '" + value + "' for '" + name + "' header");
    }

    public static String formatDate(long time) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT, Locale.US);
        dateFormat.setTimeZone(GMT);
        return dateFormat.format(new Date(time));
    }
}
