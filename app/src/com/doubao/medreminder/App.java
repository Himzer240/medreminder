package com.doubao.medreminder;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import java.lang.ref.WeakReference;

/** 监听应用回到前台：每次回前台检查一次更新（没更新不弹窗） */
public class App extends Application {

    private int started = 0;
    private WeakReference<Activity> cur = new WeakReference<Activity>(null);

    @Override
    public void onCreate() {
        super.onCreate();
        Logx.init(this);
        registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity a, Bundle b) {
            }

            @Override
            public void onActivityStarted(Activity a) {
                started++;
                if (started == 1) {
                    Activity top = cur.get();
                    UpdateChecker.checkSilent(
                            (top != null && !top.isFinishing()) ? top : a);
                }
            }

            @Override
            public void onActivityResumed(Activity a) {
                cur = new WeakReference<Activity>(a);
            }

            @Override
            public void onActivityPaused(Activity a) {
            }

            @Override
            public void onActivityStopped(Activity a) {
                started--;
            }

            @Override
            public void onActivitySaveInstanceState(Activity a, Bundle b) {
            }

            @Override
            public void onActivityDestroyed(Activity a) {
            }
        });
    }
}
