package ru.gkpromtech.exhibition.organizations;

import android.widget.Filter;

import java.util.ArrayList;
import java.util.List;

class OrganizationsFilter extends Filter {
    private List<OrganizationsFragment.Item> mOriginalItems;
    private OrganizationsAdapter mAdapter;

    OrganizationsFilter(OrganizationsAdapter adapter) {
        mAdapter = adapter;
        mOriginalItems = adapter.mItems;
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        String needle = constraint.toString().trim().toLowerCase();

        if (needle == null || needle.length() == 0) {
            FilterResults results = new FilterResults();
            results.values = mOriginalItems;
            results.count = mOriginalItems.size();
            return results;
        }

        List<OrganizationsFragment.Item> resultItems = new ArrayList<>();

        for (OrganizationsFragment.Item item : mOriginalItems) {
            if (item.organization != null) {
                if (item.organization.shortname != null
                        && item.organization.shortname.toLowerCase().contains(needle))
                {
                    resultItems.add(item);
                    continue;
                }

                if (item.organization.fullname != null
                        && item.organization.fullname.toLowerCase().contains(needle))
                {
                    resultItems.add(item);
                    continue;
                }
            }

            if (item.placesStr != null && item.placesStr.toLowerCase().contains(needle)) {
                resultItems.add(item);
                continue;
            }
        }

        FilterResults results = new FilterResults();
        results.values = resultItems;
        results.count = resultItems.size();
        return results;
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        if (mAdapter.mItems != results.values) {
            mAdapter.mItems = (List<OrganizationsFragment.Item>) results.values;
            mAdapter.notifyDataSetChanged();
        }
    }
}
