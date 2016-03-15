package ru.gkpromtech.exhibition.news;

import android.app.Fragment;
import android.os.Bundle;

import ru.gkpromtech.exhibition.NavigationActivity;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;

public class NewsActivity extends NavigationActivity {
    @Override
    protected Fragment getFragment() {
        return new NewsFragment();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AnalyticsManager.sendEvent(this, R.string.news_category, R.string.action_open);
    }
}
