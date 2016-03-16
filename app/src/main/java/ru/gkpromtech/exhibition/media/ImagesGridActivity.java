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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.io.Serializable;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.Images;

public class ImagesGridActivity extends Activity {

    private List<Media> items;
    private int mPhotoSize;
    private int mPhotoSpacing;
    private ImageBaseAdapter imageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_images_grid);

        Bundle extras = getIntent().getExtras();
        //noinspection unchecked
        items = (List<Media>) extras.getSerializable("items");
        setTitle(extras.getCharSequence("header"));

        mPhotoSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
        mPhotoSpacing = getResources().getDimensionPixelSize(R.dimen.image_gallery_spacing);

        imageAdapter = new ImageBaseAdapter(this);

        final GridView grid = (GridView)findViewById(R.id.gridView1);
        grid.setAdapter(imageAdapter);

        // get the view tree observer of the grid and set the height and numcols dynamically
        grid.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (imageAdapter.getNumColumns() == 0) {
                    final int numColumns = (int) Math.floor(grid.getWidth() / (mPhotoSize + mPhotoSpacing));
                    if (numColumns > 0) {
                        final int columnWidth = (grid.getWidth() / numColumns) - mPhotoSpacing;
                        imageAdapter.setNumColumns(numColumns);
                        imageAdapter.setItemHeight(columnWidth);
                    }
                }
            }
        });


        final Context context = this;
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (items.get(position).type == Media.IMAGE) {
                    Intent i = new Intent(context, FullImageActivity.class);
                    i.putExtra("items", (Serializable) items);
                    i.putExtra("index", position);
                    startActivity(i);
                } else {
                    VideoPlayerActivity.showVideo(context, items.get(position));
                }
            }
        });

        AnalyticsManager.sendEvent(this, R.string.media_category, R.string.action_images);
    }

    private class ImageBaseAdapter extends BaseAdapter {
        private class Holder {
            ImageView picture;
        }

        private LayoutInflater inflater;
        private int mItemHeight = 0;
        private int mNumColumns = 0;
        private RelativeLayout.LayoutParams mImageViewLayoutParams;

        public ImageBaseAdapter(Context context) {
            inflater = LayoutInflater.from(context);
            mImageViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);
        }

        @Override
        public int getCount() {
            return items.size();
        }

        public void setNumColumns(int numColumns) {
            mNumColumns = numColumns;
        }

        public int getNumColumns() {
            return mNumColumns;
        }

        public void setItemHeight(int height) {
            if (height == mItemHeight) {
                return;
            }
            mItemHeight = height;
            mImageViewLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, mItemHeight);
            notifyDataSetChanged();
        }

        @Override
        public Object getItem(int position) {
            return items.get(position);
        }

        @Override
        public long getItemId(int position) {
            return items.get(position).id;
        }

        @Override
        public View getView(int position, View view, ViewGroup viewGroup) {
            final Holder holder;
            Media item = (Media)getItem(position);

            if (view == null) {
                view = inflater.inflate(R.layout.layout_image_preview, viewGroup, false);
                final View video = view.findViewById(R.id.picture_video);
                if (item.type != Media.VIDEO) {
                    video.setVisibility(View.INVISIBLE);
                }
                else {
                    video.setVisibility(View.VISIBLE);
                }
                holder = new Holder();
                holder.picture = (ImageView) view.findViewById(R.id.picture);
                view.setTag(holder);
            }
            else {
                holder = (Holder) view.getTag();
            }

            holder.picture.setLayoutParams(mImageViewLayoutParams);

            // Check the height matches our calculated column width
            if (holder.picture.getLayoutParams().height != mItemHeight) {
                holder.picture.setLayoutParams(mImageViewLayoutParams);
            }


            ImageLoader.load(item.preview, holder.picture,
                    R.drawable.empty_image,
                    new Images.Size(getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size),
                            getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size)));

            return view;
        }
    }

}
