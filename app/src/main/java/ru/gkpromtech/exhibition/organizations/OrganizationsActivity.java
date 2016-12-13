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

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.SearchView;
import android.util.Pair;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.NavigationActivity;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Group;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.model.PlacesOrganization;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.DeviceUtils;

public class OrganizationsActivity extends NavigationActivity
        implements OrganizationsFragment.OnFragmentInteractionListener,
        SearchView.OnQueryTextListener {

    private List<OrganizationsFragment.Item> loadItemsAll() {
        DbHelper db = DbHelper.getInstance(this);

        Table<Organization> organizationsTable = db.getTableFor(Organization.class);
        List<Pair<Entity[], Organization>> placesOrganizations;

        try {
            placesOrganizations = organizationsTable.selectJoined(new Table.Join[]{
                    new Table.Join("id", PlacesOrganization.class, "organizationid", "LEFT"),
                    new Table.Join(Place.class, "f1.id = f0.placeid", "LEFT")
            }, null, null, "t.fullname");
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }

        List<OrganizationsFragment.Item> items = new ArrayList<>();
        SparseArray<OrganizationsFragment.Item> itemsCache = new SparseArray<>();

        for (Pair<Entity[], Organization> res : placesOrganizations) {
            Place place = (Place) res.first[1];
            Organization organization = res.second;

            OrganizationsFragment.Item item = itemsCache.get(organization.id);

            if (item == null) {
                item = new OrganizationsFragment.Item(place, organization);
                item.placesStr = place.name;
                items.add(item);
                itemsCache.put(organization.id, item);
            }
            else {
                if (item.addPlaces == null) {
                    item.addPlaces = new ArrayList<>();
                }

                item.addPlaces.add(place);
                item.placesStr += ", " + place.name;
            }
        }

        return items;
    }

    @Override
    protected Fragment getFragment() {
        List<OrganizationsFragment.Item> items = loadItemsAll();
        OrganizationsFragment fragment = OrganizationsFragment.newInstance(items);
        fragment.setOnFragmentInteractionListener(this);
        AnalyticsManager.sendEvent(this, R.string.organizations_category, R.string.action_open, "ALL");
        return fragment;
    }

    @Override
    public void onOrganizationClicked(final OrganizationItem item, final List<Place> addPlaces) {
        if (addPlaces == null) {
            startOrganizationPlaceActivity(item);
            return;
        }

        List<String> places = new ArrayList<>();
        places.add(item.place.name);
        for (Place place : addPlaces)
            places.add(place.name);

        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                OrganizationItem resItem = (which == 0) ? item
                        : new OrganizationItem(addPlaces.get(which - 1), item.organization);
                startOrganizationPlaceActivity(resItem);
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.stand))
                .setItems(places.toArray(new String[places.size()]), clickListener)
                .show();
    }

    private void startOrganizationPlaceActivity(OrganizationItem item) {
        Intent intent = new Intent(this, OrganizationPlaceActivity.class);
        intent.putExtra("organization", item);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_organizations, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(this);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        return ((SearchView.OnQueryTextListener) getActiveFragment()).onQueryTextSubmit(s);
    }

    @Override
    public boolean onQueryTextChange(String s) {
        return ((SearchView.OnQueryTextListener) getActiveFragment()).onQueryTextChange(s);
    }
}
