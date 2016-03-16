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
import android.content.res.Configuration;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.gkpromtech.exhibition.NavigationActivity;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Event;
import ru.gkpromtech.exhibition.model.EventFavorite;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.DeviceUtils;

public class EventsActivity extends NavigationActivity
        implements EventsFragment.OnItemClickListener {

    static final String SAVE_PAGE_NUMBER = "save_page_number";
    static final String SAVE_FILTER = "filter";

    private ViewPager pager;
    private EventsFragmentPagerAdapter pagerAdapter;

    private static int savedFilter = EventReader.EVENT_FILTER_ALL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // restore saved positions
        int savedPosition = 0;
        if (savedInstanceState != null) {
            savedFilter = savedInstanceState.getInt(SAVE_FILTER, EventReader.EVENT_FILTER_ALL);
            savedPosition = savedInstanceState.getInt(SAVE_PAGE_NUMBER, 0);
        }
        else {
            // calculate day to position ViewPager
            Calendar calendar = Calendar.getInstance();
            Date d = calendar.getTime();
            List<Date> days = new ArrayList<>(EventReader.getInstance(this).getDays());
            if (days.contains(d))
                savedPosition = days.indexOf(d);
        }

        FrameLayout view = (FrameLayout)findViewById(R.id.container);
        getLayoutInflater().inflate(R.layout.activity_events, view, true);

        TabHost tabHost = (TabHost) view.findViewById(R.id.tabHost);
        tabHost.setup();

        tabHost.addTab(tabHost.newTabSpec("tag" + EventReader.EVENT_FILTER_ALL)
                .setIndicator(createIndicatorView(tabHost, getResources().getString(R.string.all_uppercase), R.drawable.ic_events_all))
                .setContent(R.id.tab1));

        tabHost.addTab(tabHost.newTabSpec("tag" + EventReader.EVENT_FILTER_SHOW)
                .setIndicator(createIndicatorView(tabHost, getResources().getString(R.string.event_status_business), R.drawable.ic_white_plane))
                .setContent(R.id.tab2));

        tabHost.addTab(tabHost.newTabSpec("tag" + EventReader.EVENT_FILTER_MY)
                .setIndicator(createIndicatorView(tabHost, getResources().getString(R.string.event_status_my), R.drawable.ic_person))
                .setContent(R.id.tab3));

        pager = (ViewPager) findViewById(R.id.pager);
        pagerAdapter = new EventsFragmentPagerAdapter(getSupportFragmentManager(),
                EventReader.getInstance(this), savedFilter);
        pager.setAdapter(pagerAdapter);

        // set saved page
        pager.setCurrentItem(savedPosition);
        tabHost.setCurrentTabByTag("tag" + savedFilter);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int position) {
            }

            @Override
            public void onPageScrolled(int position, float positionOffset,
                                       int positionOffsetPixels) {
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                switch (tabId) {
                    case "tag" + EventReader.EVENT_FILTER_ALL:
                        savedFilter = EventReader.EVENT_FILTER_ALL;
                        break;
                    case "tag" + EventReader.EVENT_FILTER_SHOW:
                        savedFilter = EventReader.EVENT_FILTER_SHOW;
                        break;
                    case "tag" + EventReader.EVENT_FILTER_MY:
                        savedFilter = EventReader.EVENT_FILTER_MY;
                        break;
                }
                AnalyticsManager.sendEvent(EventsActivity.this, R.string.events_category, R.string.action_change, savedFilter);
                pagerAdapter.setFilter(savedFilter);
            }
        });

        AnalyticsManager.sendEvent(this, R.string.events_category, R.string.action_open, savedFilter);
    }

    private View createIndicatorView(TabHost tabHost, String text, int resId) {
        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.layout_image_tab_indicator,
                tabHost.getTabWidget(), // tab widget is the parent
                false); // no inflate params

        final TextView textView = (TextView) tabIndicator.findViewById(R.id.textView);
        final ImageView imageView = (ImageView) tabIndicator.findViewById(R.id.imageView);
        textView.setText(text);
        imageView.setImageDrawable(getResources().getDrawable(resId));

        return tabIndicator;
    }

    @Override
    public void onClick(int pageNumber, int index) {

        EventReader reader = EventReader.getInstance(this);

        Intent intent = new Intent(this, EventDetailsActivity.class);
        intent.putExtra("items", (Serializable)reader.getEvents(pageNumber, savedFilter));
        intent.putExtra("index", index);
        startActivityForResult(intent, 0);
    }

    @Override
    public void onFavoriteChanged(int pageNumber, final int eventId, int state) {
        EventReader.getInstance(this).updateFavorite(eventId, state, SQLiteDatabase.CONFLICT_REPLACE);

        final int eventid = eventId;
        final Context context = this;
        Event e = EventReader.getInstance(this).findItem(eventId);
        if (e != null) {

            EventCalendar cal = EventCalendar.getInstance(this);
            cal.setEventCalendarInteractionListener(new EventCalendar.EventCalendarInteraction() {
                @Override
                public void eventInserted(int calEventId) {
                    EventReader.getInstance(context).updateCalendarEvent(eventid, calEventId, SQLiteDatabase.CONFLICT_REPLACE);
                    AnalyticsManager.sendEvent(EventsActivity.this, R.string.events_category, R.string.action_favorite_add, eventId);
                }

                @Override
                public void eventRemoved(int calEventId) {
                    EventReader.getInstance(context).updateCalendarEvent(eventid, -1, SQLiteDatabase.CONFLICT_REPLACE);
                    AnalyticsManager.sendEvent(EventsActivity.this, R.string.events_category, R.string.action_favorite_removed, eventId);
                }
            });

            // add to calendar
            if (state == 1) {
                // get places for event
                List<Place> places = EventReader.getInstance(this).getPlaces(eventId);
                String[] sPlaces = new String[places.size()];
                for (int i = 0; i < places.size(); i ++) {
                    sPlaces[i] = places.get(i).name;
                }

                EventCalendar.getInstance(this).insertEventToCalendar(
                        e.header, e.details, TextUtils.join(", ", sPlaces), e.date, true);
            }
            // remove from calendar
            else {
                EventFavorite fav = EventReader.getInstance(this).getEventFavorite(eventId);
                if (fav.calendarEventId != null) {
                    EventCalendar.getInstance(this).removeEventFromCalendar(fav.calendarEventId);
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            pagerAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        // Always call the superclass so it can save the view hierarchy state
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(SAVE_FILTER, savedFilter);
        savedInstanceState.putInt(SAVE_PAGE_NUMBER, pager.getCurrentItem());
    }


    /**
     *   Adapter
     */
    public class EventsFragmentPagerAdapter extends FragmentStatePagerAdapter {
        private int PAGE_COUNT = 1;
        private List<EventsFragment> fragments = new ArrayList<>();

        private int filter;
        private EventReader reader;

        private SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

        public EventsFragmentPagerAdapter(FragmentManager fm, EventReader reader, int f) {
            super(fm);
            this.filter = f;
            this.reader = reader;

            reader.notifyOnChanges(new EventReader.EventReaderNotifier() {
                @Override
                protected void onChanged(EventReader reader) {
                    notifyDataSetChanged();
                }
            });

            // количество страниц
            PAGE_COUNT = reader.getDaysCount();
        }

        @Override
        public Fragment getItem(int position) {
            EventsFragment f = EventsFragment.newInstance(position, filter);
            fragments.add(f);
            return f;
        }

        @Override
        public int getCount() {
            return PAGE_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Date date = reader.getDay(position);
            return dateFormat.format(date);
        }

        @Override
        public float getPageWidth(int position) {
            // Check the device
            if(DeviceUtils.isTablet(getApplicationContext()) &&
                    getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                return 0.4f;     // 2,5 fragments / pages
            } else {
                return 1;     // 1 fragment / pages
            }
        }

        public void setFilter(int filter) {
            if (fragments.isEmpty())
                return;

            this.filter = filter;

            for (EventsFragment f: fragments) {
                f.setFilter(filter);
            }
        }

    }
}
