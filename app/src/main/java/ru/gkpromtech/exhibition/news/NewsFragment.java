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
package ru.gkpromtech.exhibition.news;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.Serializable;
import java.text.DateFormat;
import java.util.List;
import java.util.Locale;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.net.HttpClient;
import ru.gkpromtech.exhibition.net.RssParser;
import ru.gkpromtech.exhibition.utils.Callback;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.SharedData;

public class NewsFragment extends Fragment implements AbsListView.OnItemClickListener {

    private final static String NEWS_URL = SharedData.WEB_SERVER_URL + "rss/";

    private AbsListView mListView;
    private List<RssParser.Item> mNewsItems;
    private LayoutInflater mInflater;

    public NewsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mInflater = inflater;
        View view = inflater.inflate(R.layout.fragment_news, container, false);

        // Set the adapter
        mListView = (AbsListView) view.findViewById(android.R.id.list);

        // Set OnItemClickListener so we can be notified on item clicks
        mListView.setOnItemClickListener(this);
        mListView.setEmptyView(view.findViewById(android.R.id.empty));

        setEmptyText(R.string.loading);

        HttpClient.get(NEWS_URL, new Callback<List<RssParser.Item>>() {
            @Override
            public void onSuccess(List<RssParser.Item> data) {
                mNewsItems = data;
                mListView.setAdapter(mAdapter);
                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Throwable e) {
                e.printStackTrace();
                setEmptyText(R.string.error_loading_data);
                mAdapter.notifyDataSetInvalidated();
            }
        }, new HttpClient.ResponseProcessor<List<RssParser.Item>>() {
            @Override
            public List<RssParser.Item> process(InputStream in) throws Exception {
                return new RssParser().parse(in);
            }
        });

        return view;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), NewsDetailsActivity.class);
        intent.putExtra("items", (Serializable) mNewsItems);
        intent.putExtra("index", position);
        startActivity(intent);
    }

    public void setEmptyText(int emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView)
            ((TextView) emptyView).setText(emptyText);
    }

    private BaseAdapter mAdapter = new BaseAdapter() {
        class Holder {
            ImageView imageEnclosure;
            TextView textDate;
            TextView textHeader;
            TextView textDescription;
        }

        private DateFormat mDateFormat =
                DateFormat.getDateInstance(DateFormat.DEFAULT, Locale.getDefault());

        @Override
        public int getCount() {
            return mNewsItems == null ? 0 : mNewsItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mNewsItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            RssParser.Item item = mNewsItems.get(position);
            final Holder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.layout_news_item, parent, false);
                holder = new Holder();
                holder.imageEnclosure = (ImageView) view.findViewById(R.id.imageEnclosure);
                holder.textDate = (TextView) view.findViewById(R.id.textDate);
                holder.textHeader = (TextView) view.findViewById(R.id.textHeader);
                holder.textDescription = (TextView) view.findViewById(R.id.textDescription);
                view.setTag(holder);
            } else {
                holder = (Holder) view.getTag();
            }

            ImageLoader.load(item.enclosureUrl, holder.imageEnclosure);

            holder.textHeader.setText(item.title);
            holder.textDate.setText(mDateFormat.format(item.pubDate));
            holder.textDescription.setText(item.description.replace("\n", " "));
            return view;
        }
    };

}
