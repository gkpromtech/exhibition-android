package ru.gkpromtech.exhibition.organizations;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Pair;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
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
import ru.gkpromtech.exhibition.utils.ImageLoader;

/**
 * A fragment representing a list of Items.
 * <p/>
 * Large screen devices (such as tablets) are supported by replacing the ListView
 * with a GridView.
 * <p/>
 * Activities containing this fragment MUST implement the {@link OnFragmentInteractionListener}
 * interface.
 */
public class OrganizationsFragment extends Fragment implements AbsListView.OnItemClickListener,
        SearchView.OnQueryTextListener {

    public final static int GROUPED = 0;
    public final static int ALL = 1;
    private String mFilter;

    public interface OnFragmentInteractionListener {
        void onOrganizationClicked(OrganizationItem item, List<Place> addPlaces);
    }

    public static class Item extends OrganizationItem {
        public final static int HEADER = 0;
        public final static int ORGANIZATION = 1;

        public String title;
        public List<Place> addPlaces;
        public String placesStr;

        public Item(Group group, Place place, Organization organization) {
            super(group, place, organization);
        }

        public Item(String title) {
            this.title = title;
        }

        public int getType() {
            return title != null ? HEADER : ORGANIZATION;
        }
    }

    private static final String ARG_GROUP_ID = "id";
    private static final String ARG_TYPE = "type";
    private static final String ARG_ITEMS = "items";
    private static final String ARG_FILTER = "filter";
    private String mGroupId;
    private int mType;
    private List<Item> mItems;
    private int mGroupPosition = -1;
    private OnFragmentInteractionListener mListener;
    private AbsListView mListView;
    private OrganizationsAdapter mAdapter;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public OrganizationsFragment() {
    }

    public static OrganizationsFragment newInstance(String groupId, int type, List<Item> items,
                                                    String filter) {
        OrganizationsFragment fragment = new OrganizationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GROUP_ID, groupId);
        args.putInt(ARG_TYPE, type);
        args.putSerializable(ARG_ITEMS, (Serializable) items);
        args.putString(ARG_FILTER, filter);
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
            //noinspection unchecked
            mItems = (List<Item>) arguments.getSerializable(ARG_ITEMS);
            mFilter = arguments.getString(ARG_FILTER);
        }

        if (mItems == null) {
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
                SparseArray<Item> itemsCache = new SparseArray<>();
                for (Pair<Entity[], Organization> res : placesOrganizations) {
                    Place place = (Place) res.first[1];
                    Organization organization = res.second;
                    Group group = (Group) res.first[2];

                    Item item = null;
                    if (mType == GROUPED) {
                        if (!group.name.equals(lastTitle)) {
                            if (mGroupId != null && mGroupPosition == -1 &&
                                    mGroupId.equals(group.position))
                                mGroupPosition = pos;
                            mItems.add(new Item(group.name));
                            ++pos;
                            lastTitle = group.name;
                        }
                    } else {
                        item = itemsCache.get(organization.id);
                    }

                    if (item == null) {
                        item = new Item(group, place, organization);
                        item.placesStr = place.name;
                        mItems.add(item);
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


        mAdapter = new OrganizationsAdapter(getActivity());
        if (mGroupPosition != -1) {
            new Handler().post(new Runnable() {
                @Override
                public void run() {
                    mListView.setSelection(mGroupPosition);
                    if (mFilter != null)
                        onQueryTextChange(mFilter);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_organizations2, container, false);

        mListView = (AbsListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(mAdapter);
        mListView.setTextFilterEnabled(true);
        mListView.setOnItemClickListener(this);
        setEmptyText(R.string.empty);

        if (mAdapter != null && mFilter != null)
            onQueryTextChange(mFilter);

        return view;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (null != mListener) {
            Item item = mItems.get(position);
            mListener.onOrganizationClicked(item, item.addPlaces);
        }
    }

    /**
     * The default content for this Fragment has a TextView that is shown when
     * the list is empty. If you would like to change the text, call this method
     * to supply the text it should use.
     */
    public void setEmptyText(int emptyText) {
        View emptyView = mListView.getEmptyView();

        if (emptyView instanceof TextView) {
            ((TextView) emptyView).setText(emptyText);
        }
    }

    private class OrganizationsAdapter extends BaseAdapter implements Filterable {

        private LayoutInflater mLayoutInflater;
        public List<Item> mBackupItems;

        private class OrganizationHolder {
            private ImageView imageLogo;
            private TextView textName;
            private TextView textPlaceCorner;
            private TextView textPlace;
        }

        public OrganizationsAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public int getItemViewType(int position) {
            return mItems.get(position).getType();
        }

        @Override
        public int getCount() {
            if (mItems != null)
                return mItems.size();
            return 0;
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
            Item item = mItems.get(position);
            switch (item.getType()) {
                case Item.HEADER: {
                    TextView textView;
                    if (convertView == null) {
                        convertView = mLayoutInflater.inflate(
                                R.layout.layout_list_header_item, parent, false);
                        convertView.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View v) {
                                return false;
                            }
                        });
                        convertView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        });
                        textView = (TextView) convertView.findViewById(R.id.textHeader);
                        convertView.setTag(textView);
                    } else {
                        textView = (TextView) convertView.getTag();
                    }
                    textView.setText(item.title);
                    break;
                }

                case Item.ORGANIZATION: {
                    final OrganizationHolder holder;
                    if (convertView == null) {
                        convertView = mLayoutInflater.inflate(
                                R.layout.layout_organizations_list_item, parent, false);
                        holder = new OrganizationHolder();
                        holder.imageLogo = (ImageView)convertView.findViewById(R.id.imageLogo);
                        holder.textName = (TextView) convertView.findViewById(R.id.textName);
                        holder.textPlaceCorner = (TextView) convertView.findViewById(R.id.textPlaceCorner);
                        holder.textPlace = (TextView) convertView.findViewById(R.id.textPlace);
                        convertView.setTag(holder);
                    } else {
                        holder = (OrganizationHolder) convertView.getTag();
                    }

                    holder.textName.setText(item.organization.shortname);
                    if (item.placesStr == null) {
                        holder.textPlaceCorner.setVisibility(View.GONE);
                        holder.textPlace.setVisibility(View.GONE);
                    } else {
                        holder.textPlaceCorner.setVisibility(View.VISIBLE);
                        holder.textPlace.setVisibility(View.VISIBLE);
                        holder.textPlace.setText(item.placesStr);
                    }

                    ImageLoader.load(item.organization.logo, holder.imageLogo, R.drawable.no_logo,
                            true);

                    convertView.setBackgroundResource((position % 2 == 0)
                            ? R.drawable.listview_selector_even
                            : R.drawable.listview_selector_odd);

                    break;
                }
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final FilterResults results = new FilterResults();
                    try {
                        final List<Item> resultItems = new ArrayList<>();
                        if (mBackupItems == null)
                            mBackupItems = mItems;
                        if (constraint != null) {
                            String search = constraint.toString().toLowerCase();
                            if (mBackupItems != null && mBackupItems.size() > 0) {
                                for (Item item : mBackupItems) {
                                    if (item.title != null && !resultItems.isEmpty() &&
                                            resultItems.get(resultItems.size() - 1).title != null) {
                                        resultItems.remove(resultItems.size() - 1);
                                    }

                                    if (item.title != null ||
                                            item.organization.shortname.toLowerCase().contains(search) ||
                                            item.organization.fullname.toLowerCase().contains(search) ||
                                            (item.placesStr != null &&
                                                    item.placesStr.toLowerCase().contains(search)))
                                        resultItems.add(item);
                                }

                                if (!resultItems.isEmpty() &&
                                        resultItems.get(resultItems.size() - 1).title != null)
                                    resultItems.remove(resultItems.size() - 1);
                            }
                            results.values = resultItems;
                        }
                    } catch(Exception e) {
                        e.printStackTrace();
                    }
                    return results;
                }

                @SuppressWarnings("unchecked")
                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    mItems = (List<Item>) results.values;
                    notifyDataSetChanged();
                }
            };
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
