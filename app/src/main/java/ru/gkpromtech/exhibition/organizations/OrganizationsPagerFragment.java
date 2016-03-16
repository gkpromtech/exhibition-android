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

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Group;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.model.PlacesOrganization;
import ru.gkpromtech.exhibition.utils.DeviceUtils;


public class OrganizationsPagerFragment extends android.app.Fragment
        implements SearchView.OnQueryTextListener {

    public final static int GROUPED = 0;
    public final static int ALL = 1;

    private ViewPager mPager;
    private PagerAdapter mPagerAdapter;

    private static final String ARG_GROUP_ID = "id";
    private static final String ARG_TYPE = "type";
    private static final String ARG_SINGLE = "single";
    private String mGroupId;
    private int mType;
    private boolean mSingle;
    private int mGroupPosition = -1;
    private List<List<OrganizationsFragment.Item>> mItems;
    private List<WeakReference<OrganizationsFragment>> mFragments = new ArrayList<>();
    private String mFilter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrganizationsPagerFragment() {
    }

    public static OrganizationsPagerFragment newInstance(String groupId, int type, boolean single) {
        OrganizationsPagerFragment fragment = new OrganizationsPagerFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putInt(ARG_TYPE, type);
        args.putBoolean(ARG_SINGLE, single);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        if (arguments != null) {
            mGroupId = arguments.getString(ARG_GROUP_ID);
            mType = arguments.getInt(ARG_TYPE);
            mSingle = arguments.getBoolean(ARG_SINGLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (mPager != null)
            return mPager;

        mPager = (ViewPager) inflater.inflate(R.layout.fragment_organizations_pager, container, false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getActivity() == null)
                    return;

                int maxItemsPerView = mPager.getHeight() /
                        getResources().getDimensionPixelSize(R.dimen.item_size_1);
                fillItems(maxItemsPerView);

                if (mPagerAdapter == null)
                    mPagerAdapter = new OrganizationsPagerAdapter(((ActionBarActivity) getActivity())
                            .getSupportFragmentManager(), mItems.size());
                mPager.setAdapter(mPagerAdapter);
            }
        }, 50);

        return mPager;
    }

    private void fillItems(int maxItemsPerView) {
        DbHelper db = DbHelper.getInstance(getActivity());

        mItems = new ArrayList<>();
        Table<Organization> organizationsTable = db.getTableFor(Organization.class);
        try {
            List<Pair<Entity[], Organization>> placesOrganizations;

            if (mType == GROUPED) {
                placesOrganizations = organizationsTable.selectJoined(new Table.Join[]{
                        new Table.Join("id", PlacesOrganization.class, "organizationid"),
                        new Table.Join(Place.class, "f1.id = f0.placeid"),
                        new Table.Join(Group.class, "f2.id = f1.groupid")
                }, null, null, "f2.sortorder, t.fullname");
            } else {
                placesOrganizations = organizationsTable.selectJoined(new Table.Join[]{
                        new Table.Join("id", PlacesOrganization.class, "organizationid", "LEFT"),
                        new Table.Join(Place.class, "f1.id = f0.placeid", "LEFT"),
                        new Table.Join(Group.class, "f2.id = f1.groupid", "LEFT")
                }, null, null, "t.fullname");
            }

            String lastTitle = null;
            int pos = 0;
            SparseArray<OrganizationsFragment.Item> itemsCache = new SparseArray<>();
            for (Pair<Entity[], Organization> res : placesOrganizations) {
                Place place = (Place) res.first[1];
                Organization organization = res.second;
                Group group = (Group) res.first[2];

                OrganizationsFragment.Item item = null;
                if (mType == GROUPED) {
                    if (!group.name.equals(lastTitle)) {
                        if (mGroupId != null && mGroupPosition == -1 &&
                                mGroupId.equals(group.position))
                            mGroupPosition = pos;
                        if (!mSingle || mItems.isEmpty())
                            mItems.add(new ArrayList<OrganizationsFragment.Item>());
                        mItems.get(mItems.size() - 1).add(new OrganizationsFragment.Item(group.name));
                        ++pos;
                        lastTitle = group.name;
                    }
                } else {
                    if (mItems.isEmpty() || (!mSingle && pos == maxItemsPerView)) {
                        mItems.add(new ArrayList<OrganizationsFragment.Item>());
                        pos = 0;
                    }
                    item = itemsCache.get(organization.id);
                }

                if (item == null) {
                    item = new OrganizationsFragment.Item(group, place, organization);
                    item.placesStr = place.name;
                    mItems.get(mItems.size() - 1).add(item);
                    itemsCache.put(organization.id, item);
                } else {
                    if (item.addPlaces == null)
                        item.addPlaces = new ArrayList<>();
                    item.addPlaces.add(place);
                    item.placesStr += ", " + place.name;
                }
                ++pos;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private class OrganizationsPagerAdapter extends FragmentStatePagerAdapter {

        private final int mPagesCount;

        public OrganizationsPagerAdapter(FragmentManager fm, int pagesCount) {
            super(fm);
            mPagesCount = pagesCount;
        }

        @Override
        public Fragment getItem(int position) {
            OrganizationsFragment fragment = OrganizationsFragment.newInstance(mGroupId,
                    mType, mItems.get(position), mFilter);
            mFragments.add(new WeakReference<>(fragment));
            return fragment;
        }

        @Override
        public int getCount() {
            return mPagesCount;
        }


        @Override
        public float getPageWidth(int position) {
            return DeviceUtils.isLandscapeTablet(getActivity())
                    ? 0.4f     // 2,5 fragments / pages
                    : 1f;      // 1 fragment / pages
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mFilter = newText;
        for (WeakReference<OrganizationsFragment> fragment :  mFragments) {
            OrganizationsFragment ref = fragment.get();
            if (ref != null) {
                ref.onQueryTextChange(newText);
            }
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

}
