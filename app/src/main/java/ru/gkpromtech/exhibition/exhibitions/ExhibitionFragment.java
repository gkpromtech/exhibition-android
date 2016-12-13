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
import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
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
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class ExhibitionFragment extends Fragment {

    private static final String EXHIBITION = "exhibition";
    private static final String MEDIA = "media";
    private static final String ORGANIZATION = "organization";

    private List<Media> mMediaList = new ArrayList<>();
    private Exhibition mExhibition;
    private OrganizationItem mOrganization;
    private Media mStartMedia;
    private StringBuilder mStringTags;

    public static ExhibitionFragment newInstance(Exhibition exhibition, Media startMedia,
                                                 OrganizationItem organization) {
        ExhibitionFragment fragment = new ExhibitionFragment();
        Bundle args = new Bundle();

        args.putSerializable(EXHIBITION, exhibition);
        args.putSerializable(MEDIA, startMedia);
        args.putSerializable(ORGANIZATION, organization);
        fragment.setArguments(args);

        return fragment;
    }

    public ExhibitionFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStartMedia = (Media) getArguments().getSerializable(MEDIA);
            mExhibition = (Exhibition) getArguments().getSerializable(EXHIBITION);
            mOrganization = (OrganizationItem) getArguments().getSerializable(ORGANIZATION);

            DbHelper db = DbHelper.getInstance(getActivity());
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

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_exhibition, container, false);

        final float density = getResources().getDisplayMetrics().density;

        TextView textName = (TextView) view.findViewById(R.id.textName);
        TextView textOrganization = (TextView) view.findViewById(R.id.textOrganization);
        TextView textPlace = (TextView) view.findViewById(R.id.textPlace);
        TextView textDescription = (TextView) view.findViewById(R.id.textDescription);
        TextView textTags = (TextView) view.findViewById(R.id.textTags);
        final ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPreviewClicked();
            }
        });

        textName.setText(mExhibition.name);
        textOrganization.setText(mOrganization.organization.fullname);
        textPlace.setText(mOrganization.place.name);
        textDescription.setText(mExhibition.text);
        textTags.setText(mStringTags);

        textOrganization.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrganizationClicked();
            }
        });

        textPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onOrganizationClicked();
            }
        });


        ImageLoader.load((mStartMedia.type == Media.IMAGE) ? mStartMedia.url : mStartMedia.preview,
                imageView);

        LinearLayout previewStrip = (LinearLayout) view.findViewById(R.id.previewStrip);
        if (mMediaList.size() < 2) {
            previewStrip.setVisibility(View.GONE);
        } else {
            HListView listPreview = (HListView) view.findViewById(R.id.listPreview);
            listPreview.setDivider(new ColorDrawable(Color.TRANSPARENT));
            listPreview.setDividerWidth(0);
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
                        imagePreview = new ImageView(getActivity());
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
                    Intent i = new Intent(getActivity(), FullImageActivity.class);
                    i.putExtra("items", (Serializable) mMediaList);
                    i.putExtra("index", pos);
                    startActivity(i);
                }
            });

            int screenWidth = getResources().getDisplayMetrics().widthPixels;
            int oneImageWidth = (int) (86 * density);

            View imageButton = view.findViewById(R.id.buttonPreview);
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

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void onPreviewClicked() {
        Intent i = new Intent(getActivity(), FullImageActivity.class);
        i.putExtra("items", (Serializable) mMediaList);
        i.putExtra("index", 0);
        startActivity(i);
    }

    public void onGalleryClicked() {
        Intent i = new Intent(getActivity(), ImagesGridActivity.class);
        i.putExtra("items", (Serializable) mMediaList);
        i.putExtra("header", mExhibition.name);
        startActivity(i);
    }

    public void onOrganizationClicked() {
        Intent intent = new Intent(getActivity(), OrganizationPlaceActivity.class);
        intent.putExtra("organization", mOrganization);
        startActivity(intent);
    }

}
