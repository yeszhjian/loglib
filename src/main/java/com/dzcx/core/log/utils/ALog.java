package com.dzcx.core.log.utils;

import android.util.Log;

import com.dzcx.core.log.bean.MsgModel;
import com.dzcx.core.log.config.EventType;
import com.dzcx.core.log.logdb.LogMsgManager;

/**
 * Created by chen3 on 2017/11/6.
 */

public class ALog {

    public static void i(String tag, String msg) {
        Log.i(tag, msg);
        storeLog(tag, msg);
    }

    public static void i(String msg) {
        i("lulu", msg);
    }

    private static void storeLog(String tag, String msg) {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"log\",\"tag\":\"");
        builder.append(tag);
        builder.append("\",\"msg\":\"");
        builder.append(msg);
        builder.append("\",\"stackTrace\":[");
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        int index = getIndex(stackTrace);
        if (index > -1) {
            int length = index + 3;
            if (length > stackTrace.length) {
                length = stackTrace.length;
            }
            for (; index < length; ++index) {
                StackTraceElement element = stackTrace[index];
                builder.append("{\"className\":\"");
                builder.append(element.getClassName());
                builder.append("\",\"methodName\":\"");
                builder.append(element.getMethodName());
                builder.append("\",\"lineNumber\":");
                builder.append(element.getLineNumber());
                builder.append("},");
            }
            builder.deleteCharAt(builder.length() - 1);

        }
        builder.append("]}");
        MsgModel model = new MsgModel();
        model.setMsg(builder.toString());
        model.setType(EventType.TYPE_LOG);
        LogMsgManager.getInstance().addLogMsg(model);
    }

    private static int getIndex(StackTraceElement[] stackTrace) {
        int index = -1;
        for (int i = stackTrace.length - 1; i > 0; --i) {
            StackTraceElement element = stackTrace[i];
            if (element.getClassName().equals(ALog.class.getName())) {
                index = i + 1;
                break;
            }
        }
        return index;
    }

}
