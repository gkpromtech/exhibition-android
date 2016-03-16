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
package ru.gkpromtech.exhibition.utils;


import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.SparseArray;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import ru.gkpromtech.exhibition.R;

public class ImageLoader {

    private final static int TAG_URL = 81273012;
    private final static Map<ImageView, String> mLoadQueue = new HashMap<>();
    private final static Map<String, Long> mCacheExpiration = new HashMap<>();
    private final static long CACHE_LEASE_TIME = 60 * 60 * 1000; // 1 hour

    private final static SparseArray<Bitmap> mBmpPlaceholders = new SparseArray<>();

    public static void load(String url, final ImageView imageView) {
        load(url, imageView, R.drawable.empty_image);
    }

    public static void load(String url, ImageView imageView, int placeholderResId) {
        load(url, imageView, placeholderResId, null, true);
    }

    public static void load(String url, ImageView imageView, int placeholderResId,
                            Images.Size outputSize) {
        load(url, imageView, placeholderResId, outputSize, true);
    }

    public static void load(String url, ImageView imageView, boolean keepAspectRatio) {
        load(url, imageView, R.drawable.empty_image, keepAspectRatio);
    }

    public static void load(String url, ImageView imageView, int placeholderResId,
                            boolean keepAspectRatio) {
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        load(url, imageView, placeholderResId, new Images.Size(params.width, params.height),
                keepAspectRatio);
    }

    public static void load(String url, final ImageView imageView, final int placeholderResId,
                            final Images.Size outputSize, boolean keepAspectRatio) {

        if (url != null && !url.isEmpty() && !url.matches("^http[s]?://.*$")) {
            if (url.startsWith("/"))
                url = url.substring(1);
            url = SharedData.WEB_SERVER_URL + url;
        }

        boolean enableDownload = false;
        Long cacheExpires = mCacheExpiration.get(url);
        long now = Calendar.getInstance().getTimeInMillis();
        if (cacheExpires == null || cacheExpires < now) {
            mCacheExpiration.put(url, now + CACHE_LEASE_TIME);
            enableDownload = true;
        } else {
            String existingUrl = (String) imageView.getTag(TAG_URL);
            if (existingUrl != null && existingUrl.equals(url))
                return;

            Log.d("MON", "Using cached images for " + url);
        }

        imageView.setImageBitmap(null);
        mLoadQueue.remove(imageView);
        mLoadQueue.put(imageView, url);

        if (enableDownload) {
            // сначала загрузим из кеша, если есть
            Images.get(url, new Callback<Bitmap>() {
                @Override
                public void onSuccess(Bitmap data) throws Exception {
                    imageView.setImageBitmap(data);
                }

                @Override
                public void onError(Throwable exception) {
                }
            }, outputSize, keepAspectRatio, false);
        }


        final String finalUrl = url;
        Images.get(url, new Callback<Bitmap>() {
            @Override
            public void onSuccess(Bitmap data) throws Exception {
                if (finalUrl != null && finalUrl.equals(mLoadQueue.get(imageView))) {
                    mLoadQueue.remove(imageView);
                    imageView.setImageBitmap(data);
                    imageView.setTag(TAG_URL, finalUrl);
                } else {
                    data.recycle();
                }
            }

            @Override
            public void onError(Throwable exception) {
                Bitmap bmp = mBmpPlaceholders.get(placeholderResId);
                if (bmp == null) {
                    try {
                        bmp = BitmapFactory.decodeResource(imageView.getResources(),
                                placeholderResId);
                        mBmpPlaceholders.put(placeholderResId, bmp);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                    }
                }
                imageView.setImageBitmap(bmp);
                imageView.setTag(TAG_URL, finalUrl);
            }
        }, outputSize, keepAspectRatio, enableDownload);

    }

    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inScaled = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
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
}
