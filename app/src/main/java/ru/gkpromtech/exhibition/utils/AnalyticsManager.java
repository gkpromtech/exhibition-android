package ru.gkpromtech.exhibition.utils;

import android.app.Activity;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import ru.gkpromtech.exhibition.ExhibitionApplication;


public class AnalyticsManager {

    public static void sendEvent(Activity activity, int category, int actionId) {
        Tracker t = ExhibitionApplication.getTracker();
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(activity.getString(category))
                .setAction(activity.getString(actionId))
                .setLabel(null)
                .build());
    }

    public static void sendEvent(Activity activity, int categoryId, int actionId, long id) {
        Tracker t = ExhibitionApplication.getTracker();
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(activity.getString(categoryId))
                .setAction(activity.getString(actionId))
                .setLabel(Long.toString(id))
                .build());
    }

    public static void sendEvent(Activity activity, int categoryId, int actionId, String id) {
        Tracker t = ExhibitionApplication.getTracker();
        // Build and send an Event.
        t.send(new HitBuilders.EventBuilder()
                .setCategory(activity.getString(categoryId))
                .setAction(activity.getString(actionId))
                .setLabel(id)
                .build());
    }
}
