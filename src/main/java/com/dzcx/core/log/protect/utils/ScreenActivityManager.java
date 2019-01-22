package com.dzcx.core.log.protect.utils;

import android.app.Activity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by chen3 on 2017/10/17.
 */

public class ScreenActivityManager {

    private static ScreenActivityManager mScreenActivityManager;

    private ArrayList<WeakReference<Activity>> mScreenActivityReferenceList;

    private ScreenActivityManager() {
        mScreenActivityReferenceList = new ArrayList<>();
    }

    public static ScreenActivityManager getInstance() {
        if (mScreenActivityManager == null) {
            mScreenActivityManager = new ScreenActivityManager();
        }
        return mScreenActivityManager;
    }

    public void add(Activity activity) {

        WeakReference<Activity> activityWeakReference = new WeakReference<>(activity);
        mScreenActivityReferenceList.add(activityWeakReference);
    }

    public void clear() {
        for (WeakReference<Activity> activityWeakReference : mScreenActivityReferenceList) {
            Activity activity = activityWeakReference.get();
            if (activity != null) {
                activity.finish();
            }
        }
        mScreenActivityReferenceList.clear();
    }

}
