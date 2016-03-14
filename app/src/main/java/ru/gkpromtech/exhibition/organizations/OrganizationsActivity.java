package ru.gkpromtech.exhibition.organizations;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.widget.SearchView;
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
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.utils.DeviceUtils;

public class OrganizationsActivity extends NavigationActivity
        implements OrganizationsFragment.OnFragmentInteractionListener,
        SearchView.OnQueryTextListener {

    private OrganizationsPagerFragment[] mFragments = new OrganizationsPagerFragment[2];
    private int mPosition;

    @Override
    protected Fragment getFragment() {
        return getFragment(mPosition);
    }

    private void setupSpinner(MenuItem item) {
        item.setVisible(true);

        View view = item.getActionView();

        if (view instanceof Spinner) {
            Spinner spinner = (Spinner) view;
            ArrayAdapter<CharSequence> listAdapter = ArrayAdapter.createFromResource(
                    getSupportActionBar().getThemedContext(),
                    R.array.organizations_filter, R.layout.spinner_text);
            listAdapter.setDropDownViewResource(R.layout.layout_spinner_dropdown_item);
            spinner.setAdapter(listAdapter);
            spinner.setOnItemSelectedListener(mOnSpinnerItemClicked);
        }
    }

    private AdapterView.OnItemSelectedListener mOnSpinnerItemClicked = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            mPosition = position;
            setActiveFragment(getFragment(position));
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

    private Fragment getFragment(int position) {
        OrganizationsPagerFragment fragment = mFragments[position];

        if (fragment == null) {
            boolean single = !DeviceUtils.isLandscapeTablet(this);
            switch (position) {
                case 0: { // все
                    fragment = OrganizationsPagerFragment.newInstance("",
                            OrganizationsPagerFragment.ALL, single);
                    break;
                }

                case 1: { // по стендам
                    fragment = OrganizationsPagerFragment.newInstance(getIntent().getStringExtra("id"),
                            OrganizationsPagerFragment.GROUPED, single);
                    break;
                }

                default:
                    return null;
            }
            mFragments[position] = fragment;
        }

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
                        : new OrganizationItem(item.group, addPlaces.get(which - 1), item.organization);
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

        MenuItem spinnerItem = menu.findItem(R.id.spinner);
        setupSpinner(spinnerItem);

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
