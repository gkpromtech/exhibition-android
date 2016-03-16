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
package ru.gkpromtech.exhibition.events;


import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import android.widget.Toast;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import ru.gkpromtech.exhibition.R;

public class EventCalendar {
    static EventCalendar instance = null;
    private Context mContext;
    private EventCalendarInteraction listener;

    private String[] calNames;
    private int[] calIds;
    private SharedPreferences prefs;

    public final static String CHOICE_PREF = "save_events_choice";
    public final static String CHOICE_CALID_PREF = "save_cal_id_choice";
    public final static int CHOICE_NEVER_INSERT_EVENTS = 0;
    public final static int CHOICE_INSERT_EVENTS = 1;


    public static EventCalendar getInstance(Context context) {
        if (instance == null) {
            instance = new EventCalendar(context);
        }
        return instance;
    }

    private EventCalendar(Context contex) {
        this.mContext = contex;

        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String[] projection = new String[] {
                CalendarContract.Calendars._ID,
                CalendarContract.Calendars.ACCOUNT_NAME,
                CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
                CalendarContract.Calendars.NAME,
                CalendarContract.Calendars.CALENDAR_COLOR,
                CalendarContract.Calendars.OWNER_ACCOUNT
        };

        Cursor cursor = mContext.getContentResolver().query(uri, projection, CalendarContract.Calendars.VISIBLE + " = 1", null, null);

        calNames = new String[cursor.getCount()];
        calIds = new int[cursor.getCount()];

        cursor.moveToFirst();
        for (int i = 0; i < cursor.getCount(); i++, cursor.moveToNext()) {
            int n = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID);
            calIds[i] = cursor.getInt(n);
            n = cursor.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME);
            calNames[i] = cursor.getString(n);
        }
        cursor.close();

        prefs = mContext.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE);

    }

    public void insertEventToCalendar(String title, String addInfo, String place,
                                      Date beginTime, boolean needReminder) {

        final Date t1 = beginTime;
        final String sTitle = title;
        final String sPlace = place;
        final String sDescription = addInfo;
        final boolean bNeedReminder = needReminder;

        int choice = prefs.getInt(CHOICE_PREF, -1);
        int calId = prefs.getInt(CHOICE_CALID_PREF, -1);


        if (choice == -1 || (choice == CHOICE_INSERT_EVENTS && calId == -1)) {
            if (calIds.length > 0) {
                // create alert dialog
                new AlertDialog.Builder(mContext)
                        .setPositiveButton("OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        int index = ((AlertDialog) dialog).getListView().getCheckedItemPosition();
                                        int lastEventId = insertEvent(calIds[index], t1, sTitle, sPlace, sDescription);
                                        if (bNeedReminder)
                                            insertReminder(lastEventId, 15, CalendarContract.Reminders.METHOD_ALERT);

                                        if (listener != null)
                                            listener.eventInserted(lastEventId);

                                        prefs.edit().putInt(CHOICE_PREF, CHOICE_INSERT_EVENTS).apply();
                                        prefs.edit().putInt(CHOICE_CALID_PREF, calIds[index]).apply();

                                        if (lastEventId > 0) {
                                            Toast.makeText(mContext, R.string.insert_event_ok, Toast.LENGTH_LONG).show();
                                        }
                                    }
                                })
                        .setNeutralButton(R.string.cancel, null)
                        .setNegativeButton(R.string.never_insert_event,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int id) {
                                        prefs.edit().putInt(CHOICE_PREF, CHOICE_NEVER_INSERT_EVENTS).apply();
                                    }
                                })
                        .setTitle(R.string.select_calendar)
                        .setSingleChoiceItems(calNames, -1, null)
                        .show();
            }
            else {
                Toast.makeText(mContext, R.string.calendars_not_found, Toast.LENGTH_LONG).show();

            }
        }
        else if (choice == CHOICE_INSERT_EVENTS) {
            int lastEventId = insertEvent(calId, t1, sTitle, sPlace, sDescription);
            if (bNeedReminder)
                insertReminder(lastEventId, 15, CalendarContract.Reminders.METHOD_ALERT);

            if (listener != null)
                listener.eventInserted(lastEventId);

            if (lastEventId > 0) {
                Toast.makeText(mContext, R.string.insert_event_ok, Toast.LENGTH_LONG).show();
            }
        }
    }

    public void removeEventFromCalendar(int eventId) {

        removeEvent(eventId);
        if (listener != null)
            listener.eventRemoved(eventId);

        Toast.makeText(mContext, R.string.delete_event_ok, Toast.LENGTH_LONG).show();
    }

    public interface EventCalendarInteraction {
        public void eventInserted(int eventId);
        public void eventRemoved(int eventId);
    }
    public void setEventCalendarInteractionListener(EventCalendarInteraction listener) {
        this.listener = listener;
    }

    private int insertEvent(int calId, Date beginTime, String title, String place, String description) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(beginTime);
        cal.add(Calendar.HOUR_OF_DAY, 1);

        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        TimeZone timeZone = TimeZone.getDefault();
        values.put(CalendarContract.Events.DTSTART, beginTime.getTime());
        values.put(CalendarContract.Events.DTEND, cal.getTime().getTime());
        values.put(CalendarContract.Events.EVENT_TIMEZONE, timeZone.getID());
        values.put(CalendarContract.Events.TITLE, title);
        values.put(CalendarContract.Events.EVENT_LOCATION, place);
        values.put(CalendarContract.Events.DESCRIPTION, description);
        values.put(CalendarContract.Events.CALENDAR_ID, calId);
        values.put(CalendarContract.Events.GUESTS_CAN_INVITE_OTHERS, 1);
        values.put(CalendarContract.Events.GUESTS_CAN_MODIFY, 1);
        values.put(CalendarContract.Events.ACCESS_LEVEL, CalendarContract.Events.ACCESS_PRIVATE);
        values.put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY);
        Uri uriInsert = cr.insert(CalendarContract.Events.CONTENT_URI, values);

        return new Integer(uriInsert.getLastPathSegment());
    }

    private void insertReminder(int eventId, int minutes, int method) {
        String reminderUriString = "content://com.android.calendar/reminders";

        ContentResolver cr = mContext.getContentResolver();
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Reminders.EVENT_ID, eventId);
        values.put(CalendarContract.Reminders.MINUTES, minutes);
        values.put(CalendarContract.Reminders.METHOD, method);
        Uri uriInsert = cr.insert(Uri.parse(reminderUriString), values);
    }

    private int removeEvent(int eventId) {
        Uri deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId);
        ContentResolver cr = mContext.getContentResolver();
        return cr.delete(deleteUri, null, null);
    }
}
