package com.dzcx.core.log.utils;

/**
 * user：yeszhjian on 2019/1/4 11:20
 * email：yeszhjian@163.com
 */

public interface ResourceIds {
    boolean knownIdName(String name);

    int idFromName(String name);

    String nameForId(int id);
}
