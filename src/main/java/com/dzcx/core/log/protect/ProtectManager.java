package com.dzcx.core.log.protect;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;


import com.dzcx.core.log.LogService;

import java.util.ArrayList;

/**
 * Created by chen3 on 2017/10/17.
 */

public class ProtectManager {

    public static final int PROTECT_SCAN_TIME = 20000;

    private static ProtectManager mManager;

    private ArrayList<ProtectModel> mProtectList;

    private ProtectManager() {

    }

    private synchronized static ProtectManager getInstance() {
        if (mManager == null) {
            mManager = new ProtectManager();
        }
        return mManager;
    }

    /**
     * 初始化
     *
     * @param context 上下问对象
     */
    public static void init(Context context) {
        ProtectManager.getInstance().protectInit(context, null);
    }

    public static void init(Context context, ArrayList<ProtectModel> protectList) {
        ProtectManager.getInstance().protectInit(context, protectList);
    }

    /**
     * 初始化保活管理器
     *
     * @param context
     */
    private void protectInit(Context context, ArrayList<ProtectModel> protectList) {
        mProtectList = protectList;
        if (mProtectList == null) {
            mProtectList = new ArrayList<>();
        }
        mProtectList.add(new ProtectModel(LogService.class));
        //对于Android 5.0 以上的系统可以使用JobScheduler服务来定时执行一个Job
        //扫描正在运行的服务，然后重启已经关闭的服务或者界面
        JobManager.getInstance().start(context);
        startProtectService(context);
        storeToCache(context);
    }

    private void startProtectService(Context context) {

    }

    private ArrayList<ProtectModel> getInnerProtectList(Context context) {
        if (mProtectList == null || mProtectList.size() == 0) {
            loadProtectListFromCache(context);
        }
        return mProtectList;
    }

    private void loadProtectListFromCache(Context context) {
        if (mProtectList == null) {
            mProtectList = new ArrayList<>();
        }
        if (context != null) {
            SharedPreferences preferences = context.getSharedPreferences("protect_list", Context.MODE_PRIVATE);
            String list = preferences.getString("list", null);
            if (!TextUtils.isEmpty(list)) {
                String[] split = list.split(",");
                for (String protectItem : split) {
                    mProtectList.add(new ProtectModel(protectItem));
                }
            }
        }
    }

    private void storeToCache(Context context) {
        if (mProtectList != null && mProtectList.size() != 0 && context != null) {
            StringBuilder builder = new StringBuilder();
            for (ProtectModel model : mProtectList) {
                builder.append(model.getClassPath());
                builder.append(",");
            }
            builder.deleteCharAt(builder.length() - 1);

            SharedPreferences preferences = context.getSharedPreferences("protect_list", Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("list", builder.toString());
            edit.commit();
        }
    }

    /**
     * 获取受保活的任务对象
     *
     * @return
     */
    public static ArrayList<ProtectModel> getProtectList(Context context) {
        return getInstance().getInnerProtectList(context);
    }

    public static void stop(Context context) {
        getInstance().stopInner(context);
    }

    private void stopInner(Context context) {
        //JobManager
        JobManager.getInstance().stop(context);
        //取消广播
    }
}
