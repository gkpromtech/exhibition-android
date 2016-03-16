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

import android.os.Environment;

import java.io.File;

import ru.gkpromtech.exhibition.R;

public final class SharedData {
    private final static String SERVER_URL = "http://portal.rusarmyexpo.ru";
    public final static String WEB_SERVER_URL = SERVER_URL + "/";
    public final static String REST_SERVER_URL = SERVER_URL + ":8888/";

    public static final String LOCAL_DATABASE_NAME = "main"; // локальная БД пользователя
    public static final String EXHIBITION_DATABASE_NAME = "exhibition"; // обновляемая БД

    public final static String EXTERNAL_DIR = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            .getAbsolutePath() + "/ru.gkpromtech.exhibition/";

    public final static String GOOGLE_ANALYTICS_TRACKING_ID = "UA-61746150-2";

    public final static int SECTION_IMAGE_RESOURCES[] = {
            R.drawable.section1,
            R.drawable.section1,
            R.drawable.section2,
            R.drawable.section3,
            R.drawable.section4,
            R.drawable.section5,
            R.drawable.section6,
            R.drawable.section7,
            R.drawable.section8,
            R.drawable.section9,
            R.drawable.section10,
            R.drawable.section11,
            R.drawable.section12,
            R.drawable.section13,
            R.drawable.section14,
            R.drawable.section15,
            R.drawable.section16,
            R.drawable.section17,
            R.drawable.section18,
            R.drawable.section19,
            R.drawable.section20,
            R.drawable.section21,
            R.drawable.section22,
            R.drawable.section23,
            R.drawable.section24,
            R.drawable.section25,
            R.drawable.section26,
            R.drawable.section27,
            R.drawable.section28,
            R.drawable.section29,
            R.drawable.section30,
            R.drawable.section31,
            R.drawable.section32,
            R.drawable.section33,
            R.drawable.section34,
            R.drawable.section35,
            R.drawable.section36,
            R.drawable.section37,
            R.drawable.section38,
            R.drawable.section39,
            R.drawable.section40,
            R.drawable.section41,
            R.drawable.section42,
            R.drawable.section43,
            R.drawable.section44,
            R.drawable.section45,
            R.drawable.section46,
            R.drawable.section47,
            R.drawable.section48,
            R.drawable.section49,
            R.drawable.section50,
            R.drawable.section51
    };
    static {
        new File(EXTERNAL_DIR).mkdirs();
    }
}
