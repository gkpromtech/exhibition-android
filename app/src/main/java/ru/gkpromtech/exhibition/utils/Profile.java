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

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Locale;

public class Profile {
    public final static int LANG_RU = 0;
    public final static int LANG_EN = 1;

    private int mLangId = LANG_RU;
    private static Profile mInst;
    private String mToken;
    private int mPersonId;
    private String mEmail;
    private String mVisitorName;
    private boolean mVisitorEmailConfirmed;
    private Context mContext;

    private Profile(Context context) {
        String lang = Locale.getDefault().getLanguage();
        switch (lang.toLowerCase()) {
            case "ru":
                mLangId = LANG_RU;
                break;

            default:
                mLangId = LANG_EN;
        }
        mContext = context.getApplicationContext();

        SharedPreferences prefs = context.getSharedPreferences(this.getClass().getName(),
                Context.MODE_PRIVATE);
        mToken = prefs.getString("token", null);
        mPersonId = prefs.getInt("personId", 0);
        mEmail = prefs.getString("email", null);
        mVisitorName = prefs.getString("visitorName", null);
        mVisitorEmailConfirmed = prefs.getBoolean("emailConfirmed", false);
    }

    public static Profile getInstance(Context context) {
        if (mInst == null)
            mInst = new Profile(context);
        return mInst;
    }

    public int getLangId() {
        return mLangId;
    }

    public String getToken() {
        return mToken;
    }

    public int getPersonId() {
        return mPersonId;
    }

    public String getEmail() {
        return mEmail;
    }

    public String getVisitorName() {
        return mVisitorName;
    }

    public boolean isVisitorEmailConfirmed() {
        return mVisitorEmailConfirmed;
    }

    public void setVisitorEmailConfirmed(boolean confirmed) {
        mContext.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE)
                .edit()
                .putBoolean("emailConfirmed", confirmed)
                .apply();

        this.mVisitorEmailConfirmed = confirmed;
    }

    public void setPersonAccountData(String token, int personId) {
        mContext.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE)
                .edit()
                .putString("token", token)
                .putInt("personId", personId)
                .apply();

        mToken = token;
        mPersonId = personId;
    }

    public void setVisitorAccountData(String email, String name) {
        mContext.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE)
                .edit()
                .putString("email", email)
                .putString("visitorName", name)
                .putBoolean("emailConfirmed", false)
                .apply();

        mEmail = email;
        mVisitorName = name;
        mVisitorEmailConfirmed = false;
    }

    public void resetPersonAccountData() {
        mContext.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE)
                .edit()
                .remove("token")
                .remove("personId")
                .apply();

        mToken = null;
        mPersonId = 0;
    }

    public void resetVisitorAccountData() {
        mContext.getSharedPreferences(this.getClass().getName(), Context.MODE_PRIVATE)
                .edit()
                .remove("email")
                .remove("visitorName")
                .remove("emailConfirmed")
                .apply();

        mEmail = null;
        mVisitorName = null;
        mVisitorEmailConfirmed = false;
    }
}
