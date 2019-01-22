package com.dzcx.core.log.config;

/**
 * user：yeszhjian on 2019/1/4 11:23
 * email：yeszhjian@163.com
 */

public class Constants {

    public final static String PAGENAME = "pageName";
    public final static String VIEWPATH = "viewPath";
    public final static String VIEWPATHSUB = "subPath";
    public final static String EVENTTYPE = "eventType";
    public final static String DATAPATH = "dataPath";
    public final static String BUSINESSDATA = "businessData";
    public final static String START_THIS = "this";
    public final static String START_ITEM = "item";
    public final static String KEY_CONTEXT = "context";
    public final static String KEY_PARENT = "parent";

    public static final long MIN_SIZE_STORE = 1024 * 1024 * 40;
    //todo 这里的代码需要删除
//    public static final long MAX_SIZE_FILE=1024*300;
    public static final long MAX_SIZE_FILE = 1024 * 1024 * 40;
    public static final String REGEX_PATTERN = "^[0-9]{11,}_[0-9]{11,}\\.zip$";
    /**
     * 上传日志
     */
    public static final int TYPE_UPLOAD = 0;
    /**
     * 保存log日志到数据
     */
    public static final int TYPE_STORE_LOG = 1;
    /**
     * 检查数据库，删除过期数据
     */
    public static final int TYPE_DATABASE_CHECK = 2;
    /**
     * 保存设备信息
     */
    public static final int TYPE_STORE_DEVICE_INFO = 3;
    /**
     * 保存日志的时间间隔
     */
    public static final int STORE_LOG_DELAY_TIME = 20 * 1000;
    /**
     * 保存设备信息
     */
    public static final String ACTION_STORE_DEVICE_INFO = "action_store_device_info";
    /**
     * 需要使用service上传log的时候使用的action
     */
    public static final String ACTION_UPLOAD = "logservice.upload";
    /**
     * 检查数据库的数据
     */
    public static final String ACTION_CHECK = "logservice.check";
    /**
     * String 上传日志的时候使用的time
     */
    public static final String ACTION_PARAM_TIME = "param_time";
    /**
     * 上传文件的唯一id
     */
    public static final String ACTION_PARAM_ID = "param_push_id";
    /**
     * boolean 上传日志的时候使用的，表示是否是上传db文件
     */
    public static final String ACTION_PARAM_USE_DB = "use_db";

}
