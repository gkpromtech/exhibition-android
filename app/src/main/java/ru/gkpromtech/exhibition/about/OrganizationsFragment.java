package ru.gkpromtech.exhibition.about;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.utils.ImageLoader;


public class OrganizationsFragment extends Fragment implements ListView.OnItemClickListener {

    private final static String ARG_PARAM = "organizations";
    private Organization[] mOrganizations;
    private LayoutInflater mInflater;
    private String[] mStatuses;

    public static OrganizationsFragment newInstance(Organization[] organizations) {
        OrganizationsFragment fragment = new OrganizationsFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM, organizations);
        fragment.setArguments(args);
        return fragment;
    }

    public OrganizationsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mStatuses = getResources().getStringArray(R.array.organization_status);
        if (getArguments() != null)
            mOrganizations = (Organization[]) getArguments().getSerializable("organizations");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_organizations, container, false);
        ListView listView = (ListView) view.findViewById(R.id.listOrganization);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(this);
        mInflater = inflater;
        return view;
    }

    private BaseAdapter mAdapter = new BaseAdapter() {

        class Holder {
            TextView textTitle;
            ImageView imageLogo;
            TextView textDescription;
        }

        @Override
        public int getCount() {
            return mOrganizations.length;
        }

        @Override
        public Object getItem(int position) {
            return mOrganizations[position];
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            Organization item = mOrganizations[position];
            final Holder holder;
            if (view == null) {
                view = mInflater.inflate(R.layout.layout_organization_item, parent, false);
                holder = new Holder();
                holder.textTitle = (TextView) view.findViewById(R.id.textTitle);
                holder.imageLogo = (ImageView) view.findViewById(R.id.imageLogo);
                holder.textDescription = (TextView) view.findViewById(R.id.textDescription);
                view.setTag(holder);
            } else {
                holder = (Holder) view.getTag();
            }


            holder.textTitle.setText(mStatuses[item.status]);
            ImageLoader.load(item.logo, holder.imageLogo);
            holder.textDescription.setText(item.fullname);
            return view;
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        ((AboutCallbacks)getActivity()).onOrganizationSelected(mOrganizations[position]);
    }
}
