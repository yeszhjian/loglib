package com.dzcx.core.log.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.TrafficStats;
import android.util.Log;

import com.dzcx.core.log.bean.MsgModel;
import com.dzcx.core.log.config.EventType;

import java.util.List;

public class DeviceNetFlowInfoUtils {
    public static String getNetFlowInfo(Context context){
        PackageManager packageManager = context.getPackageManager();
        List<ApplicationInfo> installedApplications = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES);
        StringBuilder builder = new StringBuilder();
        builder.append("{\"send_total\":");
        builder.append(TrafficStats.getTotalTxBytes());
        builder.append(",\"send_packet_total\":");
        builder.append(TrafficStats.getTotalTxPackets());
        builder.append(",\"receive_total\":");
        builder.append(TrafficStats.getTotalRxBytes());
        builder.append(",\"receive_packet_total\":");
        builder.append(TrafficStats.getTotalRxPackets());
        builder.append(",\"apps\":[");
        for (ApplicationInfo info:installedApplications){
            builder.append("{\"name\":\"");
            builder.append(info.loadLabel(packageManager));
            builder.append("\",\"packgae\":\"");
            builder.append(info.packageName);
            builder.append("\",\"send\":");
            builder.append(TrafficStats.getUidTxBytes(info.uid));
            builder.append(",\"receive\":");
            builder.append(TrafficStats.getUidRxBytes(info.uid));
            builder.append(",\"send_packet\":");
            builder.append(TrafficStats.getUidTxPackets(info.uid));
            builder.append(",\"receive_packet\":");
            builder.append(TrafficStats.getUidRxPackets(info.uid));
            builder.append("},");
            Log.e("device", "=========>name:"+info.loadLabel(packageManager)+"     uid:"+info.uid);
        }
        builder.deleteCharAt(builder.length() - 1);
        builder.append("]}");
        String info=builder.toString();
        return info;

    }
    public static MsgModel create(Context context){
        MsgModel model=new MsgModel();
        model.setMsg(getNetFlowInfo(context));
        model.setType(EventType.TYPE_DEVICE_NET);
        return model;
    }
}
