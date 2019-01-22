package com.dzcx.core.log.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.os.StatFs;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by chen3 on 2017/10/17.
 */

public class SystemUtils {

    public static boolean isAppAlive(Context context, String packageName) {
        boolean isAppRunning = false;
        // 获取activity管理对象
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        // 获取所有正在运行的app
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        // 遍历，进程名即包名
        for (ActivityManager.RunningAppProcessInfo appInfo : appProcessInfoList) {
            if (packageName.equals(appInfo.processName)) {
                isAppRunning = true;
                break;
            }
        }
        return isAppRunning;
    }

    public synchronized static void checkService(Context context) {
//        Analysis.getInstance().check(context);
//        startPush(context);
//        ArrayList<ProtectModel> protectList = ProtectManager.getProtectList(context);
//        if (protectList == null || protectList.size() == 0) {
//            return;
//        }
//        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
//        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(60);
//        if (runningServices != null && runningServices.size() != 0) {
//            for (ProtectModel service : protectList) {
//                boolean isAlive = false;
//                for (ActivityManager.RunningServiceInfo info : runningServices) {
//                    if (service.getClassPath().equals(info.service.getClassName())) {
//                        isAlive = true;
//                        break;
//                    }
//                }
//                if (!isAlive) {
//                    service.restart(context);
//                }
//            }
//
//        }
    }

    private static void startPush(Context context) {
        try {
            Class clazz = Class.forName("cn.jpush.android.api.JPushInterface");
            Method isPushStopMethod = clazz.getDeclaredMethod("isPushStopped", Context.class);
            boolean isPushStop = (boolean) isPushStopMethod.invoke(null, context);
            if (isPushStop) {
                Method resumePushMerhod = clazz.getDeclaredMethod("resumePush", Context.class);
                resumePushMerhod.invoke(null, context);
            }
        } catch (Exception e) {
        }
    }

    public static void stopPush(Context context) {
        try {
            Class clazz = Class.forName("cn.jpush.android.api.JPushInterface");
            Method isPushStopMethod = clazz.getDeclaredMethod("isPushStopped", Context.class);
            boolean isPushStop = (boolean) isPushStopMethod.invoke(null, context);
            if (!isPushStop) {
                Method resumePushMerhod = clazz.getDeclaredMethod("stopPush", Context.class);
                resumePushMerhod.invoke(null, context);
            }
        } catch (Exception e) {
        }
    }

    /**
     * 判断当前是否是主线程
     *
     * @return
     */
    public static boolean isMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    public static long getRomAvailableSize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return blockSize * availableBlocks;
    }

}
