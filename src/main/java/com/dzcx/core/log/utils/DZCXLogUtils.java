package com.dzcx.core.log.utils;

import android.util.Log;

/**
 * user：yeszhjian on 2019/1/24 18:03
 * email：yeszhjian@163.com
 */

public class DZCXLogUtils {

    private static DZCXLogUtils instance = new DZCXLogUtils();

    private boolean ISDEBUG = true;

    private DZCXLogUtils() {

    }

    public static DZCXLogUtils getInstance() {
        return instance;
    }

    public void setISDEBUG(boolean ISDEBUG) {
        this.ISDEBUG = ISDEBUG;
    }

    public void logInfo(String tag, String msg){
        if(ISDEBUG){
            Log.v(tag, msg);
        }
    }
}
