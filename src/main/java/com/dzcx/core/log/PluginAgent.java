package com.dzcx.core.log;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.RadioGroup;
import android.widget.RatingBar;
import android.widget.SeekBar;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dzcx.core.log.bean.MsgModel;
import com.dzcx.core.log.logdb.LogMsgManager;
import com.dzcx.core.log.config.Constants;
import com.dzcx.core.log.utils.HttpLogUtils;
import com.dzcx.core.log.utils.PathUtil;
import com.dzcx.core.log.utils.StringEncrypt;


import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 插桩回调数据采集类
 * user：yeszhjian on 2019/1/4 11:25
 * email：yeszhjian@163.com
 */
public class PluginAgent {

    public static HashMap<Integer, Pair<Integer, String>> sAliveFragMap = new HashMap<>();
    private static long requestId = 0;

    private Activity getActivity(View view) {
        if (null != view) {
            Context context = view.getContext();
            while (context instanceof ContextWrapper) {
                if (context instanceof Activity) {
                    return (Activity) context;
                }
                context = ((ContextWrapper) context).getBaseContext();
            }
        }

        return null;
    }

    public static void onActivityCreate(Object obj1, Bundle obj2) {
        Tracker.instance().trackLifecycle(obj1.toString(), "onCreate()");
    }

    public static void onActivityStart(Object obj) {
        Tracker.instance().trackLifecycle(obj.toString(), "onStart()");
    }

    public static void onActivityResume(Object obj) {
        Tracker.instance().trackLifecycle(obj.toString(), "onResume()");
    }

    public static void onActivityPause(Object obj) {
        Tracker.instance().trackLifecycle(obj.toString(), "onPause()");
    }

    public static void onActivityStop(Object obj) {
        Tracker.instance().trackLifecycle(obj.toString(), "onStop()");
    }

    public static void onActivityDestroy(Object obj) {
        Tracker.instance().trackLifecycle(obj.toString(), "onDestory()");
    }

    public static void onIntercept(Request request) {
        MsgModel model = HttpLogUtils.createRequestMsg(request);
        requestId = model.getId();
    }

    public static void onIntercept(Response response) {
        try {
            HttpLogUtils.createAndAddResponceMsg(response, requestId);
        } catch (Exception e) {
        }
    }

    public static void onApiErrorInfo(String errorInfo) {
        Tracker.instance().trackApiExceptionData(errorInfo);
    }

    public static void onReceive(Context context, Intent intent) {
        if (context == null || intent == null || intent.getAction() == null) {
            return;
        }
        String str = "\"action\":\"" + intent.getAction() + "\",\"extras\":{";
        String data = "";
        try{
            if(intent != null && intent.getExtras() != null){
                Set<String> set = intent.getExtras().keySet();
                for (String key : set) {
                    data = data + "\"" + key + "\":\"" + intent.getExtras().get(key) + "\",";
                }
            }
        }catch (Exception e){

        }
        str = str + data + "}";
        Tracker.instance().trackBroadcastReceive(str);
    }

    public static void onPushData(String msg) {
        JSONObject jsonObject = JSONObject.parseObject(msg);
        String type = jsonObject.getString("type");
        if (!TextUtils.isEmpty(type) && type.equals("uploadCacheFile")) {
            Tracker.instance().trackPushUpload(jsonObject);
        } else {
            Tracker.instance().trackPushData(msg);
        }
    }

    public static void onClick(View view) {
        boolean hasBusiness = false;
        // 解析dataPath
        Context context = view.getContext();
        if (context instanceof Activity) {
            String pageName = context.getClass().getSimpleName();
            String currViewPath = PathUtil.getViewPath(view);
            String eventId = StringEncrypt.Encrypt(pageName + currViewPath, StringEncrypt.DEFAULT);
            Map<String, Object> configureMap = Tracker.instance().getConfigureMap();
//            Log.d("LazierTracker", "pageName = " + pageName);
//            Log.d("LazierTracker", "currViewPath = " + currViewPath);
//            Log.d("LazierTracker", "eventId = " + eventId);
            if (null != configureMap) {
                JSONArray nodesArr = (JSONArray) configureMap.get(pageName);
                if (null != nodesArr && nodesArr.size() > 0) {
                    for (int i = 0; i < nodesArr.size(); i++) {
                        JSONObject nodeObj = nodesArr.getJSONObject(i);
                        String viewPath = nodeObj.getString(Constants.VIEWPATH);
                        String dataPath = nodeObj.getString(Constants.DATAPATH);
//                        Log.d("LazierTracker", "viewPath = " + viewPath);
//                        Log.d("LazierTracker", "dataPath = " + dataPath);
                        if (currViewPath.equals(viewPath) || PathUtil.match(currViewPath, viewPath)) {
                            // 按照路径dataPath搜集数据
                            Object businessData = PathUtil.getDataObj(view, dataPath);
                            Map<String, Object> attributes = new HashMap<>();
                            attributes.put(Constants.PAGENAME, pageName);
                            attributes.put(Constants.VIEWPATH, currViewPath);
                            JSONArray subPaths = nodeObj.getJSONArray(Constants.VIEWPATHSUB);
                            if (null == subPaths || subPaths.size() == 0) {
                                attributes.put(Constants.BUSINESSDATA, businessData);
                            } else {
                                for (int j = 0; j < subPaths.size(); j++) {
                                    String subPath = subPaths.getString(j);
                                    Object obj = PathUtil.getDataObj(businessData, subPath);
                                    attributes.put(subPath, obj);
                                }
                            }
                            Tracker.instance().trackEvent(pageName, currViewPath, eventId, attributes);
                            hasBusiness = true;
                            break;
                        }
                    }
                }
            }
            if (!hasBusiness) {
                Tracker.instance().trackEvent(pageName, currViewPath, eventId, null);
            }
        }
    }

    public static void onClick(Object object, DialogInterface dialogInterface, int which) {

    }

    public static void onItemClick(Object object, AdapterView parent, View view, int position, long id) {
//        Context context = view.getContext();
//        String pageName = context.getClass().getSimpleName();
//        String currViewPath = PathUtil.getViewPath(view);
    }

    public static void onItemSelected(Object object, AdapterView parent, View view, int position, long id) {
        onItemClick(object, parent, view, position, id);
    }

    public static void onGroupClick(Object thisObject, ExpandableListView parent, View v, int groupPosition, long id) {

    }

    public static void onChildClick(Object thisObject, ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {

    }

    public static void onStopTrackingTouch(Object thisObj, SeekBar seekBar) {

    }

    public static void onRatingChanged(Object thisObj, RatingBar ratingBar, float rating, boolean fromUser) {

    }

    public static void onCheckedChanged(Object object, RadioGroup radioGroup, int checkedId) {

    }

    public static void onCheckedChanged(Object object, CompoundButton button, boolean isChecked) {

    }

    public static void onFragmentResume(Object obj) {
        addAliveFragment(obj);
    }

    public static void onFragmentPause(Object obj) {
        removeAliveFragment(obj);
    }

    private static boolean checkFragment(android.support.v4.app.Fragment paramFragment) {
        return true;
    }

    private static boolean checkFragment(android.app.Fragment paramFragment) {
        return true;
    }

    public static void setFragmentUserVisibleHint(Object obj, boolean isUserVisibleHint) {
        if (isUserVisibleHint) {
            addAliveFragment(obj);
        } else {
            removeAliveFragment(obj);
        }
    }

    public static void onFragmentHiddenChanged(Object fragment, boolean hidden) {
        setFragmentUserVisibleHint(fragment, !hidden);
    }

    private static void addAliveFragment(Object obj) {
        View view = null;
        if (obj instanceof Fragment) {
            view = ((Fragment) obj).getView();
        } else if (obj instanceof android.support.v4.app.Fragment) {
            view = ((android.support.v4.app.Fragment) obj).getView();
        }
        if (null != view) {
            int viewCode = view.hashCode();
            sAliveFragMap.put(obj.hashCode(), new Pair<>(viewCode, obj.getClass().getSimpleName()));
        }
    }

    private static void removeAliveFragment(Object obj) {
        if (null != obj) {
            sAliveFragMap.remove(obj.hashCode());
        }
    }

}
