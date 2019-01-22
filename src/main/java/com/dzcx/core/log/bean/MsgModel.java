package com.dzcx.core.log.bean;

import android.text.TextUtils;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dzcx.core.log.config.EventType;
import com.dzcx.core.log.utils.BuildProperties;

/**
 * user：yeszhjian on 2019/1/16 14:48
 * email：yeszhjian@163.com
 */
public class MsgModel {

    //消息的内容
    private String msg;
    //时间
    private String formatTime;
    //消息的类型
    private int type;
    //其他的备注信息
    private long id;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public MsgModel() {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss SSS");
        formatTime = format.format(new Date());
        id         = System.nanoTime();
    }

    public void setFormatTime(String formatTime) {
        this.formatTime = formatTime;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        if (!TextUtils.isEmpty(msg)) {
            StringBuilder builder = new StringBuilder(msg);
            builder.deleteCharAt(builder.length() - 1);
            builder.append(",\"thread\":\"");
            builder.append(Thread.currentThread().toString());
            builder.append("\"}");
            this.msg = builder.toString();
        } else {
            this.msg = msg;
        }
    }

    public String getFormatTime() {
        return formatTime;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "id=" + id + ",type=" + type + ",time=" + formatTime + "\nmsg=" + msg + "\n";
    }

    public static MsgModel createDeviceInfoMsg() {
        MsgModel model = new MsgModel();
        model.setType(EventType.TYPE_DEVICE_INFO);
        StringBuilder builder = new StringBuilder();
        builder.append("{\"type\":\"deviceInfo\",\"msg\":\"");
        builder.append(BuildProperties.getInstance().getAll());
        builder.append("\"}");
        model.setMsg(builder.toString());
        return model;
    }

}
