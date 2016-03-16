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
package ru.gkpromtech.exhibition.media;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.Online;
import ru.gkpromtech.exhibition.utils.DeviceUtils;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.Images;

public class MediaGalleryView extends LinearLayout {
    private List<MediaListItem> items;
    private MediaGalleryViewAdapter mAdapter;
    private MediaGalleryInteraction listener;

    class MediaListItem {
        public final static int MAX_ROWS_UNLIMIT = -1;
        public final static int TYPE_TEXT = 0;
        public final static int TYPE_IMAGES = 1;
        public final static int TYPE_IMAGES_TEXT = 2;
        public final static int TYPE_ONLINE = 3;
//        public final static int TYPE_FILES = 4;

        public String text;
        public List<Media> items;
        public List<Online> online;
        public List<String> files;
        int maxRows;
        int type;

        public MediaListItem(String text) {
            this.text = text;
            type = TYPE_TEXT;
        }
        public MediaListItem(List<Media> items, String text, int maxRows) {
            this.text = text;
            this.items = items;
            this.maxRows = maxRows;
            type = TYPE_IMAGES_TEXT;
        }
        public MediaListItem(List<Media> items) {
            this.items = items;
            this.maxRows = 6;//MAX_ROWS_UNLIMIT;
            type = TYPE_IMAGES;
        }
    }

    public MediaGalleryView(Context context) {
        super(context, null);
    }

    public MediaGalleryView(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.layout_media_items, this, true);

        items = new ArrayList<>();
        mAdapter = new MediaGalleryViewAdapter(context, items);

        ListView list = (ListView) findViewById(R.id.list_view_id);
        list.setAdapter(mAdapter);

    }

    public void add(String text, List<Media> media, int maxRows) {
        items.add(new MediaListItem(text));
        items.add(new MediaListItem(media, text, maxRows));
        mAdapter.notifyDataSetChanged();
    }

    public void add(List<Media> media) {
        items.add(new MediaListItem(media));
        mAdapter.notifyDataSetChanged();
    }

    public void setOnMediaGalleryInteraction(MediaGalleryInteraction listener) {
        this.listener = listener;
        mAdapter.listener = this.listener;
    }

    public interface MediaGalleryInteraction {
        void onItemCliked(String group, int index);
        void onMoreButtonClicked(String group);
        void onOnlineItemClicked(int index);
    }

    class MediaGalleryViewAdapter extends BaseAdapter {
        class Holder {
            TextView text;
        }

        private final Context context;
        private List<MediaListItem> items;
        public MediaGalleryInteraction listener;

        public final static int ITEM_TYPE_HEADER = 0;
        public final static int ITEM_TYPE_IMAGES = 1;
        public final static int ITEM_TYPE_ONLINE = 2;

        public int PREVIEW_IMAGE_WIDTH = 100;
        public int PREVIEW_IMAGE_HEIGHT = 80;

        public MediaGalleryViewAdapter(Context context, List<MediaListItem> items) {
            this.context = context;
            this.items = items;

            if (DeviceUtils.isTablet(context) == true) {
                PREVIEW_IMAGE_WIDTH = 200;
                PREVIEW_IMAGE_HEIGHT = 150;
            }

        }

        @Override
        public int getViewTypeCount(){
            return 3;
        }

        @Override
        public int getItemViewType(int position){
            switch (items.get(position).type) {
                case MediaListItem.TYPE_ONLINE:
                    return ITEM_TYPE_ONLINE;
                case MediaListItem.TYPE_TEXT:
                    return ITEM_TYPE_HEADER;
                default:
                    return ITEM_TYPE_IMAGES;
            }
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Holder holder;
            final MediaListItem item = items.get(position);
//            final int type = getItemViewType(position);

            if (convertView == null) {
                holder = new Holder();
                LayoutInflater inflator = LayoutInflater.from(context);
                switch (item.type) {
                    case MediaListItem.TYPE_TEXT:
                        convertView = inflator.inflate(R.layout.layout_media_items_header, parent, false);
                        holder.text = (TextView) convertView.findViewById(R.id.textGroupId);
                        break;

                    default:
                        convertView = inflator.inflate(R.layout.layout_media_items_images, parent, false);
                        break;
                }
                convertView.setTag(holder);
            }
            else {
                holder = (Holder) convertView.getTag();
            }


            switch (item.type) {
                case MediaListItem.TYPE_TEXT:
                    holder.text.setText(item.text);
                    break;

                default:
                    float density = getResources().getDisplayMetrics().density;
                    // calculate geometry
                    int minImageWidth = (int)((float)PREVIEW_IMAGE_WIDTH * density);
                    int items_size = (item.type == MediaListItem.TYPE_IMAGES_TEXT || item.type == MediaListItem.TYPE_IMAGES)?
                            item.items.size():
                            item.online.size();
                    int maxRowCount = item.maxRows;
                    int margin = (int)(24.0 * density);
                    int imageMargin = (int)(4.0 * density);
                    int layoutWidth = getResources().getDisplayMetrics().widthPixels - (margin * 2);
                    int height = (int)((float)PREVIEW_IMAGE_HEIGHT * density);

                    int columnCount = layoutWidth/minImageWidth;
                    if (maxRowCount == MediaListItem.MAX_ROWS_UNLIMIT)
                        maxRowCount = 65536;    // да, знаю что это не правильно, решение оставлю на потом
                    int itemsCount = maxRowCount * columnCount;

                    if (items_size < itemsCount)
                        itemsCount = items_size ;
                    int rowCount = (int)Math.ceil((float)itemsCount/(float)columnCount);

                    // stretch width
                    int imageWidth = (layoutWidth - columnCount * imageMargin) / columnCount;

                    // set grid geometry
                    GridLayout grid = (GridLayout)convertView.findViewById(R.id.gridLayout);
                    grid.removeAllViews();
                    ViewGroup.LayoutParams params = grid.getLayoutParams();
                    params.height = height * rowCount;
                    grid.setLayoutParams(params);
                    grid.setColumnCount(columnCount);
                    grid.setRowCount(rowCount);


                    LayoutInflater inflator = LayoutInflater.from(context);

                    // add items
                    for (int i = 0; i < itemsCount; i ++) {
                        View view = inflator.inflate(R.layout.layout_image_preview, parent, false);
                        final ImageView picture = (ImageView) view.findViewById(R.id.picture);
                        ViewGroup.LayoutParams p = picture.getLayoutParams();
                        p.width = imageWidth;
                        p.height = height;
                        picture.setLayoutParams(p);

                        // get preview image type
                        int t = (item.type == MediaListItem.TYPE_IMAGES_TEXT || item.type == MediaListItem.TYPE_IMAGES)? item.items.get(i).type: Media.VIDEO;
                        if (t != Media.VIDEO) {
                            final View video = view.findViewById(R.id.picture_video);
                            video.setVisibility(View.INVISIBLE);
                        }

                        class Holder {
                            public String text;
                            public int itemIndex;
                            int type;
                            public Holder(String text, int index, int type) {
                                this.text = text;
                                this.itemIndex = index;
                                this.type = type;
                            }
                        }
                        picture.setTag(new Holder((item.type == MediaListItem.TYPE_IMAGES_TEXT)?
                                item.text: "", i, item.type));

                        picture.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Holder holder = (Holder)v.getTag();
                                if (listener != null) {
                                    if (holder.type == MediaListItem.TYPE_IMAGES_TEXT || holder.type == MediaListItem.TYPE_IMAGES) {
                                        listener.onItemCliked(holder.text, holder.itemIndex);
                                    }
                                    else {
                                        listener.onOnlineItemClicked(holder.itemIndex);
                                    }
                                }
                            }
                        });

                        if (item.type == MediaListItem.TYPE_IMAGES_TEXT || item.type == MediaListItem.TYPE_IMAGES) {
                            ImageLoader.load(item.items.get(i).preview, picture,
                                    R.drawable.no_logo, new Images.Size(p.width, p.height));
                        }
                        else {
                            ImageLoader.load(item.online.get(i).preview, picture, R.drawable.online_placeholder);
                        }

                        grid.addView(view);
                    }


                    // "More..." button
                    View imageButton = convertView.findViewById(R.id.imageButton);
                    if (items_size <= itemsCount) {
                        imageButton.setVisibility(INVISIBLE);
                    }
                    else {
                        imageButton.setVisibility(VISIBLE);
                        imageButton.setOnClickListener(new OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (listener != null) {
                                    listener.onMoreButtonClicked(item.text);
                                }
                            }
                        });
                    }

                    break;
            }

            return convertView;
        }

    }

}
