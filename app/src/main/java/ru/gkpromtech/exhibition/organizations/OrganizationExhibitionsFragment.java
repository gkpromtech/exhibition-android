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
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.SerializablePair;

public class OrganizationExhibitionsFragment extends OrganizationBaseFragment
        implements AdapterView.OnItemClickListener {

    public interface OnExhibitionsFragmentInteractionListener {
        void onExhibitionClicked(Exhibition exhibition, Media media);
    }

    private OnExhibitionsFragmentInteractionListener mListener;
    private LayoutInflater mLayoutInflater;
    private List<SerializablePair<Exhibition, Media>> mItems;

    public OrganizationExhibitionsFragment() {
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnExhibitionsFragmentInteractionListener) activity;
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
        View v = inflater.inflate(R.layout.fragment_organization_exhibitions, container, false);
        super.init(v);

        ListView listSamples = (ListView) v.findViewById(R.id.listSamples);

        @SuppressWarnings("unchecked")
        List<SerializablePair<Exhibition, Media>> items =
                (List<SerializablePair<Exhibition, Media>>) getArguments()
                        .getSerializable("exhibitionsMedia");

        mItems = items;
        if (mItems == null)
            return v;

        listSamples.setAdapter(new ExhibitionSamplesAdapter());
        listSamples.setOnItemClickListener(this);
        return v;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        SerializablePair<Exhibition, Media> pair = mItems.get(position);
        if (mListener != null)
            mListener.onExhibitionClicked(pair.first, pair.second);
    }

    private class ExhibitionSamplesAdapter extends BaseAdapter {

        private class Holder {
            ImageView imagePreview;
            TextView textTitle;
            TextView textDescription;
        }

        public ExhibitionSamplesAdapter() {
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
            final SerializablePair<Exhibition, Media> item = mItems.get(position);
            final Holder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.two_line_with_preview_item, parent, false);
                holder = new Holder();
                holder.imagePreview = (ImageView) convertView.findViewById(R.id.imagePreview);
                holder.textTitle = (TextView) convertView.findViewById(R.id.textTitle);
                holder.textDescription = (TextView) convertView.findViewById(R.id.textDescription);
                convertView.setTag(holder);
            } else {
                holder = (Holder) convertView.getTag();
            }

            holder.textTitle.setText(item.first.name.replace('\n',' '));
            holder.textDescription.setText(item.first.text.replace('\n',' '));
            ImageLoader.load(item.second.preview, holder.imagePreview);

            convertView.setBackgroundResource((position % 2 == 0)
                    ? R.drawable.listview_selector_even
                    : R.drawable.listview_selector_odd);

            return convertView;
        }
    }
}
