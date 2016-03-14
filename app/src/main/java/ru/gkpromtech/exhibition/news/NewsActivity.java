package ru.gkpromtech.exhibition.news;

import android.app.Fragment;

import ru.gkpromtech.exhibition.NavigationActivity;

public class NewsActivity extends NavigationActivity {
    @Override
    protected Fragment getFragment() {
        return new NewsFragment();
    }
}
