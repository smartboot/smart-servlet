/*
 * Copyright (c) 2017-2020, org.smartboot. All rights reserved.
 * project name: smart-servlet
 * file name: EnumerationImpl.java
 * Date: 2020-11-17
 * Author: sandao (zhengjunweimail@163.com)
 *
 */

package org.smartboot.servlet.util;

import java.util.Enumeration;
import java.util.Iterator;

/**
 * @author 三刀
 * @version V1.0 , 2020/11/17
 */
public class EnumerationImpl<E> implements Enumeration<E> {
    private final Iterator<E> iterator;

    public EnumerationImpl(Iterator<E> iterator) {
        this.iterator = iterator;
    }

    @Override
    public boolean hasMoreElements() {
        return iterator.hasNext();
    }

    @Override
    public E nextElement() {
        return iterator.next();
    }
}
