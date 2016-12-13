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

import android.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Place;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class OrganizationsFragment extends Fragment
        implements SearchView.OnQueryTextListener,
        OrganizationsAdapter.OnItemClickListener {

    public interface OnFragmentInteractionListener {
        void onOrganizationClicked(OrganizationItem item, List<Place> addPlaces);
    }

    public static class Item extends OrganizationItem {
        public String title;
        public List<Place> addPlaces;
        public String placesStr;

        public Item(Place place, Organization organization) {
            super(place, organization);
        }

        public Item(String title) {
            this.title = title;
        }
    }

    private static final String ARG_ITEMS = "items";
    private List<Item> mItems;
    private OnFragmentInteractionListener mListener;
    private RecyclerView mListView;
    private OrganizationsAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrganizationsFragment() {
    }

    public static OrganizationsFragment newInstance(List<Item> items) {
        OrganizationsFragment fragment = new OrganizationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_ITEMS, (Serializable) items);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle arguments = getArguments();
        mItems = (List<OrganizationsFragment.Item>) arguments.getSerializable(ARG_ITEMS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizations_list, container, false);
        // XXX ListView's layout manager is configured in layout file.
        mListView = (RecyclerView) view.findViewById(R.id.organizationsList);
        mAdapter = new OrganizationsAdapter(mItems);
        mAdapter.setOnItemClickListener(this);
        mListView.setAdapter(mAdapter);
        return view;
    }

    public void setOnFragmentInteractionListener(OnFragmentInteractionListener listener) {
        mListener = listener;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onItemClick(View view, int position) {
        if (null != mListener) {
            Item item = mItems.get(position);
            mListener.onOrganizationClicked(item, item.addPlaces);
        }
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.getFilter().filter(newText);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }
}
