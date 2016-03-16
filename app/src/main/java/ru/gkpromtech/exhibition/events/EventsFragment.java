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

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Event;


public class EventsFragment extends Fragment implements ListView.OnItemClickListener, EventsArrayAdapter.OnEventsArrayAdapterInteraction {
    static final String ARGUMENT_PAGE_NUMBER = "arg_page_number";
    static final String ARGUMENT_FILTER = "arg_filter";

    private int pageNumber;
    private int currentFilter;

    private ListView mListView;
    private List<Event> mEventItems;
    private EventsArrayAdapter adapter;
    private OnItemClickListener listener;
    private View mRootView;

    static EventsFragment newInstance(int page, int filter) {
        EventsFragment pageFragment = new EventsFragment();
        Bundle arguments = new Bundle();
        arguments.putInt(ARGUMENT_PAGE_NUMBER, page);
        arguments.putInt(ARGUMENT_FILTER, filter);
        pageFragment.setArguments(arguments);
        return pageFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageNumber = getArguments().getInt(ARGUMENT_PAGE_NUMBER);
        currentFilter = getArguments().getInt(ARGUMENT_FILTER);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // parent activity listens for click on item events
        this.listener = (OnItemClickListener)getActivity();

        EventReader reader = EventReader.getInstance(getActivity());
        mEventItems = new ArrayList<Event>(reader.getEvents(pageNumber, currentFilter));

        adapter = new EventsArrayAdapter(getActivity(), mEventItems, pageNumber);
        adapter.setOnItemChanged(this);

        // Set the adapter
        View view = inflater.inflate(R.layout.fragment_events, container, false);
        mRootView = view;
        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.addHeaderView(createHeader(inflater, pageNumber));

//        View banner = view.findViewById(R.id.textBusinessDay);
//        banner.setVisibility((pageNumber != 0)? View.GONE: View.VISIBLE);

        mListView.setAdapter(adapter);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);

        showEmptyText(mEventItems.size() > 0? View.GONE: View.VISIBLE);

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int index = position - mListView.getHeaderViewsCount();
        if (index >= 0) {
            listener.onClick(pageNumber, index);
        }
    }

    private View createHeader(LayoutInflater inflater, int index) {

        final View v = inflater.inflate(R.layout.layout_events_baner, null);
        final ImageView image = (ImageView)v.findViewById(R.id.imageView);
        final TextView textDay = (TextView)v.findViewById(R.id.textDateDay);
        final TextView textMonth = (TextView)v.findViewById(R.id.textDateMonth);

        final SimpleDateFormat dateFormatDay = new SimpleDateFormat("dd", Locale.getDefault());
        final SimpleDateFormat dateFormatMonth = new SimpleDateFormat("MMMM", Locale.getDefault());
        Date day = EventReader.getInstance(getActivity()).getDay(index);
        textDay.setText(dateFormatDay.format(day));
        textMonth.setText(dateFormatMonth.format(day));

        final int resourceIds[] = {
                R.drawable.dayimage1,
                R.drawable.dayimage2,
                R.drawable.dayimage3,
                R.drawable.dayimage4
        };
        final int num = index % (resourceIds.length);
        new Handler().postDelayed(
                new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() != null) {
                            Bitmap b = decodeSampledBitmapFromResource(getResources(),
                                    resourceIds[num], image.getWidth(), image.getHeight());
                            image.setImageBitmap(b);
                        }
                    }
                }, 50);

        return v;
    }

    static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        int inSampleSize = 1;
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            final int halfHeight = options.outHeight / 2;
            final int halfWidth = options.outWidth / 2;
            while (((halfHeight / inSampleSize) > reqHeight) && ((halfWidth / inSampleSize) > reqWidth)) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize * 2;
    }

    public void showEmptyText(int visibility) {
        View view = mRootView.findViewById(android.R.id.empty);
        view.setVisibility(visibility);
    }

    public void setFilter(int filter) {
        currentFilter = filter;

        // set source data & filter it
        // TODO заменить на передачу через параметры
        EventReader reader = EventReader.getInstance(getActivity());
        List<Event> events = reader.getEvents(pageNumber, filter);
        adapter.clear();
        adapter.addAll(events);
        adapter.notifyDataSetChanged();

        showEmptyText(events.size() > 0 ? View.GONE: View.VISIBLE);
    }

    @Override
    public void favoriteChanged(int eventId, int state) {
        listener.onFavoriteChanged(pageNumber, eventId, state);
    }

    public interface OnItemClickListener {
        public void onClick(int pageNumber, int position);
        public void onFavoriteChanged(int pageNumber, int eventId, int state);
    }

}
