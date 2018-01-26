package com.medtrum.androidstudy;

import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;

import java.util.List;

public class MyApplication extends Application {
    private static MyApplication sInstance;

    public void onCreate() {
        super.onCreate();
        sInstance=this;
    }

    public static MyApplication getInstance() {
        return sInstance;
    }

    public boolean isBackground() {
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcessInfoList = activityManager.getRunningAppProcesses();
        if (appProcessInfoList == null || appProcessInfoList.size() == 0) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcessInfo : appProcessInfoList) {
            if (appProcessInfo.processName.equals(getPackageName())) {
                return !(appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND ||
                        appProcessInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_VISIBLE);
            }
        }
        return false;
    }
}
