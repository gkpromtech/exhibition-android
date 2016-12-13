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
package ru.gkpromtech.exhibition.exhibitions;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.widget.AbsHListView;
import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.media.FullImageActivity;
import ru.gkpromtech.exhibition.media.ImagesGridActivity;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Group;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.ObjectsMedia;
import ru.gkpromtech.exhibition.model.Tag;
import ru.gkpromtech.exhibition.model.TagsObject;
import ru.gkpromtech.exhibition.organizations.OrganizationItem;
import ru.gkpromtech.exhibition.organizations.OrganizationPlaceActivity;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class ExhibitionActivity extends AppCompatActivity {

    public static final String EXHIBITION = "exhibition";
    public static final String MEDIA = "media";
    public static final String ORGANIZATION = "organization";

    private List<Media> mMediaList = new ArrayList<>();
    private Exhibition mExhibition;
    private OrganizationItem mOrganization;
    private Media mStartMedia;
    private StringBuilder mStringTags;

    private void loadMediaAndTags() {
        DbHelper db = DbHelper.getInstance(this);

        try {
            Table<Media> tableMedia = db.getTableFor(Media.class);
            List<Pair<Entity[], Media>> media = tableMedia.selectJoined(
                    new Table.Join[]{new Table.Join("id", ObjectsMedia.class, "mediaid")},
                    "f0.objectid = ?", new String[]{String.valueOf(mExhibition.id)}, null);

            for (Pair<Entity[], Media> res : media)
                mMediaList.add(res.second);

            Table<Tag> tableTag = db.getTableFor(Tag.class);
            List<Pair<Entity[], Tag>> tags = tableTag.selectJoined(
                    new Table.Join[]{new Table.Join("id", TagsObject.class, "tagid")},
                    "f0.objectid = ?", new String[]{String.valueOf(mExhibition.id)}, null);

            mStringTags = new StringBuilder(getString(R.string.title_tags) + ":");
            for (Pair<Entity[], Tag> pair : tags) {
                mStringTags.append(" ").append(pair.second.tag);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupMediaListPreview() {
        final float density = getResources().getDisplayMetrics().density;

        HListView listPreview = (HListView) findViewById(R.id.listPreview);
        listPreview.setDivider(new ColorDrawable(Color.TRANSPARENT));
        listPreview.setDividerWidth(0);

        final Activity activity = this;

        listPreview.setAdapter(new BaseAdapter() {

            @Override
            public int getCount() {
                return mMediaList.size();
            }

            @Override
            public Object getItem(int position) {
                return mMediaList.get(position);
            }

            @Override
            public long getItemId(int position) {
                return position;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                final ImageView imagePreview;

                if (convertView == null) {
                    imagePreview = new ImageView(activity);
                    AbsHListView.LayoutParams layoutParams =
                            new AbsHListView.LayoutParams((int) (86 * density), (int) (72 * density));
                    imagePreview.setLayoutParams(layoutParams);
                    imagePreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    convertView = imagePreview;
                } else {
                    imagePreview = (ImageView) convertView;
                }

                ImageLoader.load(mMediaList.get(position).preview, imagePreview);

                return convertView;
            }
        });

        listPreview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, final int pos, long l) {
                Intent i = new Intent(activity, FullImageActivity.class);
                i.putExtra("items", (Serializable) mMediaList);
                i.putExtra("index", pos);
                startActivity(i);
            }
        });

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int oneImageWidth = (int) (86 * density);

        View imageButton = findViewById(R.id.buttonPreview);

        if (mMediaList.size() * oneImageWidth < screenWidth) {
            imageButton.setVisibility(View.GONE);
        } else {
            imageButton.setVisibility(View.VISIBLE);

            imageButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onGalleryClicked();
                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibition);

        Bundle extras = getIntent().getExtras();
        mExhibition = (Exhibition) extras.getSerializable(EXHIBITION);
        mStartMedia = (Media) extras.getSerializable(MEDIA);
        mOrganization = (OrganizationItem) extras.getSerializable(ORGANIZATION);

        loadMediaAndTags();

        TextView textName = (TextView) findViewById(R.id.textName);
        textName.setText(mExhibition.name);

        TextView textOrganization = (TextView) findViewById(R.id.textOrganization);
        textOrganization.setText(mOrganization.organization.fullname);

        textOrganization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrganizationClicked();
            }
        });

        TextView textPlace = (TextView) findViewById(R.id.textPlace);
        textPlace.setText(mOrganization.place.name);

        textPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrganizationClicked();
            }
        });

        TextView textDescription = (TextView) findViewById(R.id.textDescription);
        textDescription.setText(mExhibition.text);

        TextView textTags = (TextView) findViewById(R.id.textTags);
        textTags.setText(mStringTags);

        ImageView imageView = (ImageView) findViewById(R.id.imageView);
        ImageLoader.load((mStartMedia.type == Media.IMAGE) ? mStartMedia.url : mStartMedia.preview,
                imageView);

        if (mMediaList.size() < 2) {
            LinearLayout previewStrip = (LinearLayout) findViewById(R.id.previewStrip);
            previewStrip.setVisibility(View.GONE);
        } else {
            setupMediaListPreview();
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        CollapsingToolbarLayout collapsingToolbar =
                (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        collapsingToolbar.setTitle(mExhibition.name);

        AnalyticsManager.sendEvent(this, R.string.exhibition_category, R.string.action_open, mExhibition.id);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onGalleryClicked() {
        Intent i = new Intent(this, ImagesGridActivity.class);
        i.putExtra("items", (Serializable) mMediaList);
        i.putExtra("header", mExhibition.name);
        startActivity(i);
    }

    public void onOrganizationClicked() {
        Intent intent = new Intent(this, OrganizationPlaceActivity.class);
        intent.putExtra("organization", mOrganization);
        startActivity(intent);
    }
}
