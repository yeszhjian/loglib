package com.dzcx.core.log.loginterface;

import com.dzcx.core.log.bean.NetInfoModel;

/**
 * Created by chen3 on 2017/9/21.
 */

public interface AppProxy {
    /**
     * 获取用户的token
     * @return
     */
    String getUserToken();

    /**
     * 获取保存设备信息时的url，可以不用写全，但是需要有一些特征信息;
     * 如果返回null，则会在Analysis.init方法中保存设备的信息
     * @return
     */
    String[] getStoreDeviceInfoJudgeUrl();

    /**
     * 获取网络调用的一下url，参数必须一致
     * @return
     */
    NetInfoModel getNetInfo();
}
