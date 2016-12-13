package ru.gkpromtech.exhibition.organizations;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import java.io.Serializable;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.media.FullImageActivity;
import ru.gkpromtech.exhibition.model.Media;

public class OrganizationDetailActivity extends AppCompatActivity implements
        OrganizationPlaceFragment.OrganizationPlaceFragmentListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organization_detail);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        OrganizationItem organizationItem = (OrganizationItem) getIntent().getSerializableExtra("organization");

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment,
                OrganizationPlaceFragment.newInstance(OrganizationPlaceFragment.class, organizationItem)).commit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_organization_detail, menu);
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

    @Override
    public void onPreviewImageClicked(List<Media> media, int index) {
        Intent i = new Intent(this, FullImageActivity.class);
        i.putExtra("items", (Serializable) media);
        i.putExtra("index", index);
        startActivity(i);
    }
}
