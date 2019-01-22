package com.dzcx.core.log.loginterface;

import com.dzcx.core.log.bean.MsgModel;

import java.util.LinkedList;

/**
 * Created by chen3 on 2017/9/14.
 */

public interface ILogStore {
    /**
     * 存储log
     * @param list      log的集合
     * @param count     存储多少条数据
     * @return          true 成功 false 失败
     */
    boolean storeLog(LinkedList<MsgModel> list, int count);
}
