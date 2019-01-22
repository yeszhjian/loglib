package com.dzcx.core.log.logdb;

import com.dzcx.core.log.bean.MsgModel;

import java.util.LinkedList;

/**
 * Created by chen3 on 2017/9/13.
 */

public class LogMsgManager {
    private static LogMsgManager mLogMsgManager;
    private LinkedList<MsgModel> mMsgList;
    /**
     * 消息队列保存的最大的消息数量：每秒最多10条数据，保存10分钟内的数据
     */
    private final int MINUTE_MAX_SIZE = 10 * 60;
    private final int MAX_SIZE = MINUTE_MAX_SIZE * 10;

    private LogMsgManager() {
        mMsgList = new LinkedList<>();
    }

    public static synchronized LogMsgManager getInstance() {
        if (mLogMsgManager == null) {
            mLogMsgManager = new LogMsgManager();
        }
        return mLogMsgManager;
    }

    public synchronized void addLogMsg(MsgModel model) {
        //判断消息队列的数量，移除一些过期的数据
        if (mMsgList.size() > MAX_SIZE) {
            int count = 0;
            while (count < MINUTE_MAX_SIZE) {
                mMsgList.removeFirst();
                ++count;
            }
        }
        mMsgList.add(model);
    }

    public synchronized void storeLog() {
        int count = mMsgList.size();
        //存储到数据库中
        LogDBManager.getInstance().storeLog(mMsgList, count);
    }
}
