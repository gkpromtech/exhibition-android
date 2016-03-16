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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.TextUtils;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.media.FullImageActivity;
import ru.gkpromtech.exhibition.media.ImagesGridActivity;
import ru.gkpromtech.exhibition.model.Event;
import ru.gkpromtech.exhibition.model.EventFavorite;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;

public class EventDetailsActivity extends ActionBarActivity
        implements EventDetailsFragment.OnFragmentInteractionListener{

    static final String SAVE_INDEX = "save_index";
    static final String SAVE_ITEMS = "save_items";

    private static List<Event> items;
    private static int index;
    private List<Integer> changedItems;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                @SuppressWarnings("unchecked")
                List<Event> tmpItems = (List<Event>) extras.getSerializable("items");
                items = tmpItems;
                index = extras.getInt("index", 0);
            }
        }
        else {
            @SuppressWarnings("unchecked")
            List<Event> tmpItems = (List<Event>)savedInstanceState.getSerializable(SAVE_ITEMS);
            items = tmpItems;
            index = savedInstanceState.getInt(SAVE_INDEX, 0);
        }

        setContentView(R.layout.activity_event_details);
        context = this;

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        changedItems = new ArrayList<>();


        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        pager.setAdapter(new EventDetailsCollectionPagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(index);
        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                index = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        AnalyticsManager.sendEvent(this, R.string.event_details_category, R.string.action_open, index);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putSerializable(SAVE_ITEMS, (Serializable) items);
        savedInstanceState.putInt(SAVE_INDEX, index);
    }

    @Override
    public void onBackPressed() {
        Intent i = new Intent();
        setResult(!changedItems.isEmpty()? Activity.RESULT_OK: Activity.RESULT_CANCELED, i);

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent();
                setResult(!changedItems.isEmpty()? Activity.RESULT_OK: Activity.RESULT_CANCELED, i);

                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }

    public class EventDetailsCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public EventDetailsCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            final Event event = items.get(i);

            EventDetailsFragment fragment = new EventDetailsFragment();
            Bundle args = new Bundle();
            args.putSerializable("item", event);
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return items.get(position).header;
        }
    }


    @Override
    public void onFavoriteClicked(int eventId, int state) {
        EventReader.getInstance(context).updateFavorite(eventId, state, SQLiteDatabase.CONFLICT_REPLACE);

        if (!changedItems.contains(eventId)) {
            changedItems.add(eventId);
        }

        final int eventid = eventId;
        Event e = EventReader.getInstance(context).findItem(eventId);
        if (e != null) {

            EventCalendar cal = EventCalendar.getInstance(context);
            cal.setEventCalendarInteractionListener(new EventCalendar.EventCalendarInteraction() {
                @Override
                public void eventInserted(int calEventId) {
                    EventReader.getInstance(context).updateCalendarEvent(eventid, calEventId, SQLiteDatabase.CONFLICT_REPLACE);
                }

                @Override
                public void eventRemoved(int calEventId) {
                    EventReader.getInstance(context).updateCalendarEvent(eventid, -1, SQLiteDatabase.CONFLICT_REPLACE);
                }
            });

            // add to calendar
            if (state == 1) {
                // get places for event
                List<Place> places = EventReader.getInstance(context).getPlaces(eventId);
                String[] sPlaces = new String[places.size()];
                for (int i = 0; i < places.size(); i ++) {
                    sPlaces[i] = places.get(i).name;
                }

                EventCalendar.getInstance(context).insertEventToCalendar(
                        e.header, e.details, TextUtils.join(", ", sPlaces), e.date, true);
            }
            // remove from calendar
            else {
                EventFavorite fav = EventReader.getInstance(context).getEventFavorite(eventId);
                if (fav.calendarEventId != null) {
                    EventCalendar.getInstance(context).removeEventFromCalendar(fav.calendarEventId);
                }
            }
        }
    }

    @Override
    public void onPreviewImageClicked(Event event, int position) {
        List<Media> items = EventReader.getInstance(context).getMedia(event.id, EventReader.MEDIA_FILTER_ALL);
        Media item = items.get(position);
        if (item.type == Media.IMAGE) {
            Intent i = new Intent(context, FullImageActivity.class);
            i.putExtra("items", (Serializable) items);
            i.putExtra("index", position);
            startActivity(i);
        }
        else {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
        }
    }

    @Override
    public void onGotoImagesClicked(Event event) {
        List<Media> items = EventReader.getInstance(context).getMedia(event.id, EventReader.MEDIA_FILTER_ALL);

        Intent i = new Intent(context, ImagesGridActivity.class);
        i.putExtra("items", (Serializable) items);
        i.putExtra("header", event.header);
        startActivity(i);
    }

}
