package com.dzcx.core.log.protect;

import android.annotation.TargetApi;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;


/**
 * Created by chen3 on 2017/10/17.
 */
class JobManager {

    private static final int JOB_ID = 1;

    private static JobManager mJobManager = null;

    private JobScheduler mScheduler;

    private JobManager() {
    }

    public synchronized static JobManager getInstance() {
        if (mJobManager == null) {
            mJobManager = new JobManager();
        }
        return mJobManager;
    }

    private void init(Context context) {
        if (mScheduler == null) {
            mScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        }

    }

    @TargetApi(21)
    public void start(Context context) {
//        if (isEnabled()&&!AliveJobService.isJobServiceAlive()){
//            init(context);
        // 构建JobInfo对象，传递给JobSchedulerService
//            JobInfo.Builder builder = new JobInfo.Builder(JOB_ID,new ComponentName(context, AliveJobService.class));
        // 设置每2秒执行一下任务
//            builder.setPeriodic(ProtectManager.PROTECT_SCAN_TIME);
//            // 当插入充电器，执行该任务
//            builder.setRequiresCharging(true);
//            JobInfo info = builder.build();
        //开始定时执行该系统任务
//            mScheduler.schedule(info);
//        }
    }

    private boolean isEnabled() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public void stop(Context context) {
        if (isEnabled()) {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            scheduler.cancel(JOB_ID);
        }
    }
}
