package com.dzcx.core.log.config;

/**
 * Created by chen3 on 2017/9/14.
 */
 public class EventType {
   /**
    * 设备信息
    */
    public static final int TYPE_DEVICE_INFO  = 0;
   /**
    * 请求
    */
    public static final int TYPE_REQUEST_INT  = 1;
   /**
    * 响应
    */
    public static final int TYPE_RESPONCE_INT = 2;
   /**
    * 点击事件
    */
    public static final int TYPE_CLICK_INT    = 3;
   /**
    * push事件
    */
    public static final int TYPE_PUSH_INT     = 4;
   /**
    * 页面生命周期
    */
    public static final int TYPE_LIFE_CYCLE   = 5;
   /**
    * 异常信息
    */
    public static final int TYPE_EXCEPTION    = 6;
    /**
     * 打印的日志信息
     */
    public static final int TYPE_LOG          = 7;
    /**
     * 打开Activity
     */
    public static final int TYPE_START_ACTIVITY = 8;
    /**
     * 因为存储空间不足删除数据库的数据
     */
    public static final int TYPE_DELETE     = 9;
    /**
     * 网络异常信息
     */
    public static final int TYPE_NET_ERROR  = 10;
    /**
     * 网络异常信息
     */
    public static final int TYPE_DEVICE_NET = 11;
    /**
     * 广播信息
     */
    public static final int TYPE_BROADCAST  = 12;
}
