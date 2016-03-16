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


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class FullImageFragment extends Fragment  {

    private final static String ARG_PARAM_ITEM = "item";
    private final static String ARG_PARAM_FILE = "file";
    private Media item;
    private String file;

    public FullImageFragment() {
        // Required empty public constructor
    }

    public static FullImageFragment newInstance(Media media) {
        FullImageFragment fragment = new FullImageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_ITEM, media);
        fragment.setArguments(args);
        return fragment;
    }

    public static FullImageFragment newInstance(String file) {
        FullImageFragment fragment = new FullImageFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM_FILE, file);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            item = (Media) getArguments().getSerializable(ARG_PARAM_ITEM);
            file = getArguments().getString(ARG_PARAM_FILE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_full_image, container, false);
        final TouchImageView image = (TouchImageView) view.findViewById(R.id.fullscreen_content);
        final View playButton = view.findViewById(R.id.imagePlayButton);

        if (item != null) {
            String path = (item.type == Media.IMAGE) ? item.url : item.preview;
            playButton.setVisibility((item.type == Media.IMAGE)? View.GONE: View.VISIBLE);
            ImageLoader.load(path, image);

            playButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    VideoPlayerActivity.showVideo(getActivity(), item);
                }
            });
        }
        else if (file != null) {
            Bitmap myBitmap = BitmapFactory.decodeFile(file);
            image.setImageBitmap(myBitmap);
            playButton.setVisibility(View.GONE);
        }

        return view;
    }
}
