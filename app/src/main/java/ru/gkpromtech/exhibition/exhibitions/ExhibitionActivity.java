package ru.gkpromtech.exhibition.exhibitions;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.analytics.GoogleAnalytics;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.organizations.OrganizationItem;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;

public class ExhibitionActivity extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exhibition);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }


        Bundle extras = getIntent().getExtras();
        String schemaId = extras.getString("schemaId");
        Exhibition exhibition = (Exhibition) extras.getSerializable("exhibition");
        Media startMedia = (Media) extras.getSerializable("media");
        OrganizationItem organization = (OrganizationItem) extras.getSerializable("organization");

        setTitle(exhibition.name);

        getFragmentManager().beginTransaction().replace(R.id.fragment,
                ExhibitionFragment.newInstance(schemaId, exhibition, startMedia, organization)).commit();

        AnalyticsManager.sendEvent(this, R.string.exhibition_category, R.string.action_open, exhibition.id);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
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
}
