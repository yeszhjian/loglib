package com.dzcx.core.log.utils;

import android.util.LruCache;

/**
 * user：yeszhjian on 2019/1/4 11:16
 * email：yeszhjian@163.com
 */

public class ClassNameCache extends LruCache<Class<?>, String> {

    public ClassNameCache(int maxSize) {
        super(maxSize);
    }

    @Override
    protected String create(Class<?> klass) {
        return klass.getCanonicalName();
    }

}
