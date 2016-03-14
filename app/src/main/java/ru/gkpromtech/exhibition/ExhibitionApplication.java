package ru.gkpromtech.exhibition;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import java.io.File;

import ru.gkpromtech.exhibition.utils.Images;
import ru.gkpromtech.exhibition.utils.SharedData;


public class ExhibitionApplication extends Application {

    private static int mActivityLevel;
    private static GoogleAnalytics mAnalytics;
    private static Tracker mTracker;

    public static Tracker getTracker() {
        return mTracker;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        File dir = getExternalCacheDir();
        String cachePath = (dir != null) ? (dir.getAbsolutePath() + "/images/")
                : SharedData.EXTERNAL_DIR + "cache/";
        new File(cachePath).mkdirs();

        Images.setCachePath(cachePath);
        registerActivityLifecycleCallbacks(new ExhibitionActivityLifecycleCallbacks());
        mActivityLevel = 0;

        mAnalytics = GoogleAnalytics.getInstance(this);
        mTracker = mAnalytics.newTracker(SharedData.GOOGLE_ANALYTICS_TRACKING_ID);
        mTracker.enableExceptionReporting(true);
        mTracker.enableAutoActivityTracking(true);
    }

    public static int getActivityLevel() {
        return mActivityLevel;
    }

    private class ExhibitionActivityLifecycleCallbacks implements ActivityLifecycleCallbacks {

        public void onActivityCreated(Activity activity, Bundle bundle) {
            ++mActivityLevel;
        }

        public void onActivityDestroyed(Activity activity) {
            --mActivityLevel;
        }


        public void onActivityPaused(Activity activity) {
        }

        public void onActivityResumed(Activity activity) {
        }

        public void onActivitySaveInstanceState(Activity activity, Bundle outState) {
        }

        public void onActivityStarted(Activity activity) {
        }

        public void onActivityStopped(Activity activity) {
        }
    }
}
