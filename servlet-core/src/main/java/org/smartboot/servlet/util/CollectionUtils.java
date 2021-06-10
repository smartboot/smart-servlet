/*******************************************************************************
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-http
 * file name: CollectionUtils.java
 * Date: 2020-12-12
 * Author: sandao (zhengjunweimail@163.com)
 ******************************************************************************/

package org.smartboot.servlet.util;


import java.util.Collection;

/**
 * @author 三刀（zhengjunweimail@163.com）
 * @version V1.0 , 2020/12/12
 */
public class CollectionUtils {
    public static boolean isEmpty(Collection<?> collection) {
        return (collection == null || collection.isEmpty());
    }

    public static boolean isNotEmpty(final Collection<?> collection) {
        return !isEmpty(collection);
    }
}
