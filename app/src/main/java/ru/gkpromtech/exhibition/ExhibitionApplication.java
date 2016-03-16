/*
 * Copyright 2016 Promtech. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
            GoogleAnalytics.getInstance(activity).reportActivityStart(activity);
        }

        public void onActivityStopped(Activity activity) {
            GoogleAnalytics.getInstance(activity).reportActivityStop(activity);
        }
    }
}
