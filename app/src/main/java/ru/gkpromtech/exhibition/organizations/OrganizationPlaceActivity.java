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
package ru.gkpromtech.exhibition.organizations;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTabHost;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.exhibitions.ExhibitionActivity;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.ObjectsMedia;
import ru.gkpromtech.exhibition.model.Person;
import ru.gkpromtech.exhibition.persons.PersonDetailsActivity;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.SerializablePair;

public class OrganizationPlaceActivity extends ActionBarActivity
        implements OrganizationExhibitionsFragment.OnExhibitionsFragmentInteractionListener,
        OrganizationPersonsFragment.OnPersonsFragmentInteractionListener {

    private final int PAGE_PLACE = 0;
    private final int PAGE_EXHIBITION = 1;
    private final int PAGE_REPRESENTATIVE = 2;

    private OrganizationItem mOrganization;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        setTitle(R.string.title_section_organizations);

        setContentView(R.layout.activity_organization_place);
        FragmentTabHost tabHost = (FragmentTabHost) findViewById(R.id.tabHost);

        mOrganization = (OrganizationItem) getIntent().getSerializableExtra("organization");

        tabHost.setup(this, getSupportFragmentManager(), R.id.realTabContent);

        Bundle bundle = new Bundle();
        bundle.putSerializable("organization", mOrganization);


        List<Pair<Entity[], Media>> media = null;
        List<SerializablePair<Exhibition, Media>> exhibitionsMedia = new ArrayList<>();
        List<Person> persons = null;
        DbHelper db = DbHelper.getInstance(this);
        try {
            Table<Media> tableMedia = db.getTableFor(Media.class);
            media = tableMedia.selectJoined(
                    new Table.Join[]{ new Table.Join("id", ObjectsMedia.class, "mediaid") },
                    "f0.objectid = ?", new String[] { String.valueOf(mOrganization.place.id) }, null);
            if (!media.isEmpty())
                bundle.putSerializable("media", media.get(0).second);


            Table<ObjectsMedia> tableObjectsMedia = db.getTableFor(ObjectsMedia.class);
            List<Pair<Entity[], ObjectsMedia>> exhibitions = tableObjectsMedia.selectJoined(
                    new Table.Join[]{
                            new Table.Join("objectid", Exhibition.class, "id"),
                            new Table.Join("mediaid", Media.class, "id")
                    }, "f0.organizationid = ?", new String[] {
                            String.valueOf(mOrganization.organization.id)
                    },
                    "f1.name", "t.objectid");

            for (Pair<Entity[], ObjectsMedia> res : exhibitions)
                exhibitionsMedia.add(new SerializablePair<>((Exhibition) res.first[0],
                        (Media) res.first[1]));

            bundle.putSerializable("exhibitionsMedia", (Serializable) exhibitionsMedia);


            Table<Person> tablePersons = db.getTableFor(Person.class);
            persons = tablePersons.select("organizationid = ? AND name IS NOT NULL",
                    new String[]{String.valueOf(mOrganization.organization.id)},
                    null, null, "ordernum");

            bundle.putSerializable("persons", (Serializable) persons);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!mOrganization.organization.phone.isEmpty() ||
                !mOrganization.organization.email.isEmpty() ||
                !mOrganization.organization.site.isEmpty() ||
                (mOrganization.organization.description != null &&
                        !mOrganization.organization.description.isEmpty()) ||
                media != null && !media.isEmpty()) {
            TabHost.TabSpec tab1 = tabHost.newTabSpec("tab1")
                    .setIndicator(createIndicatorView(tabHost, getTabTitle(PAGE_PLACE)));
            tabHost.addTab(tab1, OrganizationPlaceFragment.class, bundle);
        }

        if (!exhibitionsMedia.isEmpty()) {
            TabHost.TabSpec tab2 = tabHost.newTabSpec("tab2")
                    .setIndicator(createIndicatorView(tabHost, getTabTitle(PAGE_EXHIBITION)));
            tabHost.addTab(tab2, OrganizationExhibitionsFragment.class, bundle);
        }

        if (persons != null && !persons.isEmpty()) {
            TabHost.TabSpec tab3 = tabHost.newTabSpec("tab3")
                    .setIndicator(createIndicatorView(tabHost, getTabTitle(PAGE_REPRESENTATIVE)));
            tabHost.addTab(tab3, OrganizationPersonsFragment.class, bundle);
        }

        if (tabHost.getTabWidget().getTabCount() == 0) {
            setContentView(R.layout.layout_organization_place_header);

            TextView textName = (TextView) findViewById(R.id.textName);
            TextView textPlace = (TextView) findViewById(R.id.textPlace);
            final ImageView imageLogo = (ImageView) findViewById(R.id.imageLogo);

            textName.setText(mOrganization.organization.fullname);
            textPlace.setText(mOrganization.place.name);

            ImageLoader.load(mOrganization.organization.logo, imageLogo, R.drawable.no_logo);

            if (mOrganization.group == null || mOrganization.group.position == null)
                findViewById(R.id.imageShowOnSchema).setVisibility(View.GONE);
        }

        AnalyticsManager.sendEvent(this, R.string.organization_place_category, R.string.action_open, mOrganization.organization.id);
    }

    private View createIndicatorView(TabHost tabHost, String text) {
        View tabIndicator = getLayoutInflater().inflate(R.layout.layout_tab_passive_indicator,
                tabHost.getTabWidget(), false);
        ((TextView) tabIndicator.findViewById(R.id.textView)).setText(text);
        return tabIndicator;
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

    public void onSaveMaterialsClicked(View view) {
        OrganizationFilesDownloader.download(this, mOrganization.organization);
    }

    @Override
    public void onExhibitionClicked(Exhibition exhibition, Media media) {
        Intent intent = new Intent(this, ExhibitionActivity.class);
        intent.putExtra(ExhibitionActivity.SCHEMA_ID, mOrganization.group.position);
        intent.putExtra(ExhibitionActivity.EXHIBITION, exhibition);
        intent.putExtra(ExhibitionActivity.MEDIA, media);
        intent.putExtra(ExhibitionActivity.ORGANIZATION, mOrganization);
        startActivity(intent);
    }

    @Override
    public void onPersonClicked(int personId) {
        Intent intent = new Intent(this, PersonDetailsActivity.class);
        intent.putExtra("personId", personId);
        startActivity(intent);
    }

    public String getTabTitle(int position) {
        switch (position) {
            case PAGE_PLACE:
                return ((mOrganization.place.name != null && !mOrganization.place.name.isEmpty())
                        ? (getString(R.string.stand) + " " + mOrganization.place.name)
                        : getString(R.string.organization));
            case PAGE_EXHIBITION:
                return getString(R.string.exhibitions);
            case PAGE_REPRESENTATIVE:
                return getString(R.string.representatives);
            default:
                return null;
        }
    }
}
