package ru.gkpromtech.exhibition.schema;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.NavigationActivity;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Event;
import ru.gkpromtech.exhibition.model.EventsPlace;
import ru.gkpromtech.exhibition.model.MapsPoint;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.model.PlacesOrganization;
import ru.gkpromtech.exhibition.model.SchemaSearch;
import ru.gkpromtech.exhibition.organizations.OrganizationItem;
import ru.gkpromtech.exhibition.organizations.OrganizationPlaceActivity;
import ru.gkpromtech.exhibition.organizations.OrganizationsActivity;
import ru.gkpromtech.exhibition.utils.AutoCompleteSearchView;

public class SchemaActivity extends NavigationActivity
        implements SchemaFragment.SchemaFragmentCallbacks {

    public static class Marker implements Serializable {
        public String placeId;
        public String text;
        public Organization organization;

        public Marker(String placeId, String text, Organization organization) {
            this.placeId = placeId;
            this.text = text;
            this.organization = organization;
        }

        public Marker(String placeId, String text) {
            this.placeId = placeId;
            this.text = text;
        }
    }

    private Boolean mHomeAsBack;
    private SchemaSearchAdapter mSearchAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!isNavigationDrawerEnabled()) {
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.setDisplayHomeAsUpEnabled(true);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search_autocomplete, menu);
        final AutoCompleteSearchView searchView =
                (AutoCompleteSearchView) menu.findItem(R.id.search).getActionView();

        mSearchAdapter = new SchemaSearchAdapter();
        searchView.setAdapter(mSearchAdapter);

        searchView.setOnItemClickListener(mSearchClicked);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected boolean isNavigationDrawerEnabled() {
        if (mHomeAsBack == null)
            mHomeAsBack = getIntent().getStringExtra("id") != null ||
                    getIntent().getSerializableExtra("markers") != null;
        return !mHomeAsBack;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!isNavigationDrawerEnabled()) {
            switch (item.getItemId()) {
                case android.R.id.home:
                    onBackPressed();
                    return true;
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Fragment getFragment() {
        @SuppressWarnings("unchecked")
        List<Marker> markers = (List<Marker>) getIntent().getSerializableExtra("markers");
        Fragment schemaFragment = SchemaFragment.newInstance(this, markers);

        if (schemaFragment == null) {
            Toast.makeText(this, R.string.error_loading_data, Toast.LENGTH_SHORT).show();
            finish();
            return null;
        }
        else {
            return schemaFragment;
        }
    }

    public void onOrganizationsClicked(View view) {
        Intent intent = new Intent(this, OrganizationsActivity.class);
        intent.putExtra("id", getIntent().getStringExtra("id"));
        startActivity(intent);
    }

    @Override
    public void onPlaceClicked(String placeId) {
        DbHelper db = DbHelper.getInstance(this);
        List<Pair<Entity[], MapsPoint>> organizations;

        try {
            Table<MapsPoint> mapsPointTable = db.getTableFor(MapsPoint.class);

            organizations = mapsPointTable.selectJoined(
                    new Table.Join[] {
                            new Table.Join("id", Place.class, "mappointid"),
                            new Table.Join(PlacesOrganization.class, "f1.placeid = f0.id"),
                            new Table.Join(Organization.class, "f2.id = f1.organizationid")
                    },
                    "t.placename = ? and t.mapid is null",
                    new String[] { placeId },
                    "f2.fullname"
            );
        }
        catch (Exception e) {
            e.printStackTrace();
            return;
        }

        if (organizations.size() == 0) {
            return;
        }

        final Place place = (Place) organizations.get(0).first[0];

        if (organizations.size() == 1) {
            Organization organization = (Organization) organizations.get(0).first[2];
            OrganizationItem organizationItem = new OrganizationItem(place, organization);
            Intent intent = new Intent(this, OrganizationPlaceActivity.class);
            intent.putExtra("organization", organizationItem);
            startActivity(intent);
            return;
        }

        List<String> strItems = new ArrayList<>();

        for (Pair<Entity[], MapsPoint> res : organizations) {
            Organization organization = (Organization) res.first[2];
            strItems.add(organization.shortname + " " + place.name);
        }

        final List<Pair<Entity[], MapsPoint>> finalOrganizations = organizations;

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.stand) + ": " + place.name)
                .setItems(strItems.toArray(new String[strItems.size()]),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(SchemaActivity.this,
                                        OrganizationPlaceActivity.class);
                                Organization organization = (Organization) finalOrganizations.get(which).first[2];
                                OrganizationItem organizationItem = new OrganizationItem(place, organization);
                                intent.putExtra("organization", organizationItem);
                                startActivity(intent);
                            }
                        })
                .show();
    }

    private class SchemaSearchAdapter extends BaseAdapter implements Filterable {

        private List<SchemaSearch> mItems = new ArrayList<>();
        private LayoutInflater mLayoutInflater;

        public SchemaSearchAdapter() {
            mLayoutInflater = getLayoutInflater();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public Object getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(android.R.layout.simple_list_item_1,
                        parent, false);
                ((TextView) convertView).setTextColor(getResources().getColor(R.color.Exhibition_Black));
                convertView.setBackgroundResource(R.color.Exhibition_White);
            }
            ((TextView) convertView).setText(mItems.get(position).name);
            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                public Table<SchemaSearch> mTable;

                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults results = new FilterResults();
                    if (constraint != null && constraint.length() != 0) {
                        if (mTable == null)
                            mTable = DbHelper.getInstance(SchemaActivity.this)
                                    .getTableFor(SchemaSearch.class);

                        List<SchemaSearch> list = mTable.select("name LIKE ?",
                                new String[] {"%" + constraint + "%"}, null, null, "name", "100");
                        results.values = list;
                        results.count = list.size();
                    }
                    return results;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results.values != null) {
                        mItems = (List<SchemaSearch>) results.values;
                    } else {
                        mItems.clear();
                    }
                    notifyDataSetChanged();
                }
            };
        }
    }

    private AdapterView.OnItemClickListener mSearchClicked = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            List<Marker> markers = new ArrayList<>();
            DbHelper db = DbHelper.getInstance(SchemaActivity.this);
            SchemaSearch search = ((SchemaSearch) mSearchAdapter.getItem(position));

            switch (search.entity) {
                case "events": {
                    Table<Event> table = db.getTableFor(Event.class);
                    List<Pair<Entity[], Event>> placesEvents;
                    try {
                        placesEvents = table.selectJoined(new Table.Join[]{
                                new Table.Join("id", EventsPlace.class, "eventid"),
                                new Table.Join(Place.class, "f1.id = f0.placeid")
                        }, "t.id = ?", new String[] { String.valueOf(search.id) }, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    for (Pair<Entity[], Event> res : placesEvents) {
                        Place place = (Place) res.first[1];
                        Event event = res.second;
                        markers.add(new Marker(place.schemaid, event.header));
                    }

                    break;
                }

                case "places": {
                    Table<Place> table = db.getTableFor(Place.class);
                    List<Place> places;

                    try {
                        places = table.select("id = ?", new String[] { String.valueOf(search.id) },
                                null, null, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    for (Place place : places) {
                        markers.add(new Marker(place.schemaid, place.name));
                    }

                    break;
                }

                case "organizations": {
                    Table<Organization> table = db.getTableFor(Organization.class);
                    List<Pair<Entity[], Organization>> placesOrganizations;
                    try {
                        placesOrganizations = table.selectJoined(new Table.Join[]{
                                new Table.Join("id", PlacesOrganization.class, "organizationid"),
                                new Table.Join(Place.class, "f1.id = f0.placeid")
                        }, "t.id = ?", new String[] { String.valueOf(search.id) }, null);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }

                    for (Pair<Entity[], Organization> res : placesOrganizations) {
                        Place place = (Place) res.first[1];
                        Organization organization = res.second;

                        // пропуск открытых композиций
                        if (!place.name.matches("^[0-9].*$"))
                            continue;

                        markers.add(new Marker(place.schemaid,
                                organization.shortname, organization));
                    }

                    break;
                }
            }

            if (markers.isEmpty()) {
                Toast.makeText(SchemaActivity.this, getString(R.string.stand_not_found),
                        Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(SchemaActivity.this, SchemaActivity.class);
            intent.putExtra("markers", (Serializable) markers);
            startActivity(intent);
        }
    };
}
