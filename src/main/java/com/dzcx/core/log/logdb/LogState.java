package com.dzcx.core.log.logdb;

/**
 * Created by chen3 on 2017/12/27.
 */

public class LogState {
    private String startTime;
    private String endTime;
    private int minId;
    private int maxId;
    private String filePath;

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public LogState(String startTime, int minId, String endTime, int maxId, String filePath) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.minId = minId;
        this.maxId = maxId;
        this.filePath = filePath;
    }

    public int getMinId() {
        return minId;
    }

    public int getMaxId() {
        return maxId;
    }

    public String getFilePath() {
        return filePath;
    }
}
