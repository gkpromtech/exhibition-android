package ru.gkpromtech.exhibition.organizations;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Person;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class OrganizationPersonsFragment extends OrganizationBaseFragment
        implements AdapterView.OnItemClickListener {

    public interface OnPersonsFragmentInteractionListener {
        void onPersonClicked(int personId);
    }

    private OnPersonsFragmentInteractionListener mListener;
    private LayoutInflater mLayoutInflater;
    private List<Person> mPersons;

    public OrganizationPersonsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnPersonsFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OrganizationExhibitionsFragmentCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        View v = inflater.inflate(R.layout.fragment_organization_persons, container, false);
        super.init(v);

        ListView listPersons = (ListView) v.findViewById(R.id.listSamples);

        //noinspection unchecked
        mPersons = (List<Person>) getArguments().getSerializable("persons");

        listPersons.setAdapter(new PersonsAdapter());
        listPersons.setOnItemClickListener(this);

        return v;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mListener.onPersonClicked(mPersons.get(position).id);
    }

    private class PersonsAdapter extends BaseAdapter {

        private class Holder {
            ImageView imagePreview;
            TextView textTitle;
            TextView textName;
        }

        public PersonsAdapter() {
        }

        @Override
        public int getCount() {
            return mPersons.size();
        }

        @Override
        public Object getItem(int position) {
            return mPersons.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final Person person = mPersons.get(position);
            final Holder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.two_line_with_preview_item, parent, false);
                holder = new Holder();
                holder.imagePreview = (ImageView) convertView.findViewById(R.id.imagePreview);
                holder.textTitle = (TextView) convertView.findViewById(R.id.textTitle);
                holder.textName = (TextView) convertView.findViewById(R.id.textDescription);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.textTitle.setText(person.position != null
                    ? person.position.replace('\n', ' ') : "");
            holder.textName.setText(person.name.replace('\n', ' '));
            ImageLoader.load(person.photo, holder.imagePreview, R.drawable.no_photo);

            convertView.setBackgroundResource((position % 2 == 0)
                    ? R.drawable.listview_selector_even
                    : R.drawable.listview_selector_odd);

            return convertView;
        }
    }

}
