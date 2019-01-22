package com.dzcx.core.log;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dzcx.core.log.bean.MsgModel;
import com.dzcx.core.log.bean.NetInfoModel;
import com.dzcx.core.log.config.Constants;
import com.dzcx.core.log.config.EventType;
import com.dzcx.core.log.logdb.LogDBManager;
import com.dzcx.core.log.logdb.LogMsgManager;
import com.dzcx.core.log.loginterface.AppProxy;
import com.dzcx.core.log.protect.ProtectManager;
import com.dzcx.core.log.protect.ProtectModel;
import com.dzcx.core.log.utils.CrashHandler;
import com.dzcx.core.log.utils.HookUtils;
import com.dzcx.core.log.utils.IOUtil;
import com.dzcx.core.log.utils.SystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * user：yeszhjian on 2019/1/4 11:24
 * email：yeszhjian@163.com
 */
public class Tracker {
    private static final String CONFIG_FILE_NAME = "log_analysis";
    private static final String TAG = "TrackerData";
    private volatile static Tracker mTracker;
    private final Map<String, Object> mConfigureMap;
    private Context mContext;
    private AppProxy mAppProxy;
    private String tempData = "";

    /**
     * 初始化
     *
     * @param context
     * @param appProxy
     * @param protectList 受保护的服务或者activity
     */
    private Tracker(Context context, AppProxy appProxy, ArrayList<ProtectModel> protectList) {
        // TODO: 2019/1/4 该业务埋点配置本应由服务器下发，这里暂时写在本地
        mContext = context.getApplicationContext();
        // 解析业务埋点配置
        String json = null;
        try {
            InputStream inputStream = context.getAssets().open("configure.json");
            json = IOUtil.readInputStream(inputStream);
        } catch (IOException e) {

        }
        mConfigureMap = JSON.parseObject(json, Map.class);
        mAppProxy = appProxy;
        setState(mContext, true);
        initOtherService(mContext);
        if (mAppProxy != null && (mAppProxy.getStoreDeviceInfoJudgeUrl() == null || mAppProxy.getStoreDeviceInfoJudgeUrl().length == 0)) {
            storeDeviceInfo();
        }
        ProtectManager.init(mContext, protectList);
        HookUtils.hook();
        storeConfig(mContext, appProxy);
    }

    public static void init(Context context) {
        if (mTracker == null) {
            synchronized (Tracker.class) {
                if (mTracker == null) {
                    mTracker = new Tracker(context, null, null);
                }
            }
        }
    }

    public static void init(Context context, AppProxy appProxy) {
        if (mTracker == null) {
            synchronized (Tracker.class) {
                if (mTracker == null) {
                    mTracker = new Tracker(context.getApplicationContext(), appProxy, null);
                }
            }
        }
    }

    public void init(Context context, AppProxy appProxy, ArrayList<ProtectModel> protectList) {
        if (mTracker == null) {
            synchronized (Tracker.class) {
                if (mTracker == null) {
                    mTracker = new Tracker(context.getApplicationContext(), appProxy, protectList);
                }
            }
        }
    }

    public static synchronized Tracker instance() {
        if (mTracker == null) {
            Log.d(TAG, "Tracker is not enabled, please call init first");
        }
        return mTracker;
    }

    public Map<String, Object> getConfigureMap() {
        return mConfigureMap;
    }

    /**
     * 初始化其他的服务
     *
     * @param context
     */
    private void initOtherService(Context context) {
        //初始化数据库
        LogDBManager.getInstance().init(context);
        //设置全局的异常捕获
        CrashHandler.getInstance().init();
        //打开log的服务
        checkLog(context);
    }

    private void storeConfig(Context context, AppProxy proxy) {
        if (context != null && proxy != null) {
            SharedPreferences preferences = context.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor edit = preferences.edit();
            edit.putString("token", proxy.getUserToken());
            String[] storeDeviceInfoJudgeUrl = proxy.getStoreDeviceInfoJudgeUrl();
            if (storeDeviceInfoJudgeUrl != null && storeDeviceInfoJudgeUrl.length != 0) {
                StringBuilder builder = new StringBuilder();
                for (String item : storeDeviceInfoJudgeUrl) {
                    builder.append(item);
                    builder.append(",");
                }
                builder.deleteCharAt(builder.length() - 1);
                edit.putString("judge", builder.toString());
            }
            NetInfoModel netInfo = proxy.getNetInfo();
            if (netInfo != null) {
                edit.putString("start_url", netInfo.getStartUploadUrl());
                edit.putString("upload_url", netInfo.getUploadUrl());
            }
            edit.commit();
        }
    }

    private void checkLog(Context context) {
        Intent intent = new Intent(context, LogService.class);
        intent.setAction(Constants.ACTION_CHECK);
        context.startService(intent);
    }

    public AppProxy getAppProxy() {
        if (mAppProxy == null) {
            if (mContext != null) {
                SharedPreferences preferences = mContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
                final String token = preferences.getString("token", "");
                String judgeUrl = preferences.getString("judge", null);
                String[] judgeArray = null;
                if (!TextUtils.isEmpty(judgeUrl)) {
                    judgeArray = judgeUrl.split(",");
                }
                String startUrl = preferences.getString("start_url", "");
                String uploadUrl = preferences.getString("upload_url", "");
                final String[] urls = judgeArray;
                final NetInfoModel model = NetInfoModel.Builder.newInstance().startUploadUrl(startUrl).uploadUrl(uploadUrl).build();
                mAppProxy = new AppProxy() {
                    @Override
                    public String getUserToken() {
                        return token;
                    }

                    @Override
                    public String[] getStoreDeviceInfoJudgeUrl() {
                        return urls;
                    }

                    @Override
                    public NetInfoModel getNetInfo() {
                        return model;
                    }
                };
            }
        }
        return mAppProxy;
    }

    /**
     * 保存设备信息
     */
    public void storeDeviceInfo() {
        if (mContext == null) {
            return;
        }
        Intent intent = new Intent(mContext, LogService.class);
        intent.setAction(Constants.ACTION_STORE_DEVICE_INFO);
        mContext.startService(intent);
    }

    /**
     * 设置当前保活的状态
     *
     * @param isAlive
     */
    public void setState(Context context, boolean isAlive) {
        LogService.hasShowLowSize = false;
        Context applicationContext = context.getApplicationContext();
        SharedPreferences preferences = applicationContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = preferences.edit();
        edit.putBoolean("protect_service", isAlive);
        edit.apply();
    }

    public void stopService(Context context) {
        Context applicationContext = context.getApplicationContext();
        setState(context, false);
        ProtectManager.stop(applicationContext);
        //停止极光推送
        SystemUtils.stopPush(applicationContext);
    }

    /**
     * 判断保活的服务是否开启
     *
     * @param context
     * @return
     */
    public boolean isProtectEnable(Context context) {
        Context applicationContext = null;
        if (context != null) {
            applicationContext = context.getApplicationContext();
        } else if (mContext != null) {
            applicationContext = mContext;
        }
        if (applicationContext == null) {
            return true;
        } else {
            SharedPreferences preferences = applicationContext.getSharedPreferences(CONFIG_FILE_NAME, Context.MODE_PRIVATE);
            return preferences.getBoolean("protect_service", true);
        }
    }

    /**
     * 点击事件日志收集
     *
     * @param pageView
     * @param currViewPath
     * @param eventId
     * @param attributes
     */
    public void trackEvent(String pageView, String currViewPath, String eventId, Map<String, Object> attributes) {
        if (null != attributes) {
            //可服务端配置埋点处理
            Iterator<Map.Entry<String, Object>> iterator = attributes.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<String, Object> entry = iterator.next();
                Log.d(TAG, "attributes@" + entry.getKey() + " = " + entry.getValue());
            }
        } else {
            MsgModel model = new MsgModel();
            model.setType(EventType.TYPE_CLICK_INT);
            StringBuilder builder = new StringBuilder();
            builder.append("{\"type\":\"viewClick\",\"page\":\"");
            builder.append(pageView);
            builder.append("\",\"view\":\"");
            builder.append(currViewPath);
            builder.append("\"}");
            String s = builder.toString();
            model.setMsg(s);
            Log.d(TAG, "点击事件日志： " + builder.toString());
            LogMsgManager.getInstance().addLogMsg(model);
        }
    }

    /**
     * activity生命周期日志收集
     *
     * @param pageView
     * @param currMethod
     */
    public void trackLifecycle(String pageView, String currMethod) {
        MsgModel model = new MsgModel();
        model.setType(EventType.TYPE_LIFE_CYCLE);
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"lifecycle\",\"page\":\"");
        builder.append(pageView);
        builder.append("\",\"method\":\"");
        builder.append(currMethod);
        builder.append("\"}");
        String s = builder.toString();
        model.setMsg(s);
        Log.d(TAG, "生命周期日志： " + builder.toString());
        LogMsgManager.getInstance().addLogMsg(model);
    }

    /**
     * 网络数据日志收集
     *
     * @param model
     */
    public void trackHttpData(MsgModel model) {
//        Log.d(TAG, "网络日志： " + model.getMsg());
        LogMsgManager.getInstance().addLogMsg(model);
    }

    /**
     * Exception数据日志收集
     *
     * @param msg
     */
    public void trackApiExceptionData(String msg) {
//        Log.d(TAG, "网络链接错误日志： " + msg);
        MsgModel msgModel = new MsgModel();
        msgModel.setType(EventType.TYPE_NET_ERROR);
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"error\" ,\"message\": \"");
        builder.append(msg);
        builder.append("\"");
        String s = builder.toString();
        msgModel.setMsg(s);
        LogMsgManager.getInstance().addLogMsg(msgModel);
    }

    /**
     * 广播数据日志收集
     */
    public void trackBroadcastReceive(String msg) {
        MsgModel msgModel = new MsgModel();
        msgModel.setType(EventType.TYPE_BROADCAST);
        StringBuilder sb = new StringBuilder();
        sb.append("{\"type\":\"broadcast\", \"data\":{");
        sb.append(msg);
        sb.append("}}");
        msgModel.setMsg(sb.toString());
        if (!tempData.equals(sb.toString())) {
            Log.d(TAG, "广播日志： " + msg);
            tempData = sb.toString();
            LogMsgManager.getInstance().addLogMsg(msgModel);
        }
    }

    /**
     * 推送数据日志收集
     */
    public void trackPushData(String msg) {
        Log.d(TAG, "推送日志： " + msg);
        MsgModel model = new MsgModel();
        model.setType(EventType.TYPE_PUSH_INT);
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"push\",\"method\":\"");
        builder.append("onPushData()");
        builder.append("\",\"pushMsg\":\"");
        builder.append(msg);
        builder.append("\"}");
        String s = builder.toString();
        model.setMsg(s);
        LogMsgManager.getInstance().addLogMsg(model);
    }

    /**
     * 日志上传
     * @param jsonObject
     */
    public void trackPushUpload(JSONObject jsonObject) {
        Log.d(TAG, "开始上传文件：-bbbbbbbbbbbbbbbb");
        String  date  = jsonObject.getString("pushDate");
        boolean useDB = jsonObject.getBoolean("useDb");
        String  id    = jsonObject.getString("id");
        if (TextUtils.isEmpty(id)) id =  System.nanoTime() + "";
        String formatDate = "";
        //date > 0
        if (!TextUtils.isEmpty(date)) {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
            formatDate = format.format(new Date(date));
        }
        Log.i(TAG, "开始上传文件：-cccccccccccccccccccc");
        Intent intent = new Intent(mContext, LogService.class);
        intent.putExtra(Constants.ACTION_PARAM_USE_DB, useDB);
        intent.putExtra(Constants.ACTION_PARAM_TIME, formatDate);
        intent.putExtra(Constants.ACTION_PARAM_ID, id);
        intent.setAction(Constants.ACTION_UPLOAD);
        mContext.startService(intent);
    }
}
