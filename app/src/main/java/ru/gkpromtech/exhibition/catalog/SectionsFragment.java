package ru.gkpromtech.exhibition.catalog;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
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

import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Section;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.SharedData;


public class SectionsFragment extends Fragment
        implements AbsListView.OnItemClickListener, SearchView.OnQueryTextListener {


    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(int sectionId, int sectionNum, String title);
    }

    private class Item {

        public int id;
        public int num;
        public String title;

        private Item(int id, int num, String title) {
            this.id = id;
            this.num = num;
            this.title = title;
        }

    }

    private OnFragmentInteractionListener mListener;
    private SectionsAdapter mAdapter;
    private List<Item> mItems;


    public static SectionsFragment newInstance() {

        return new SectionsFragment();
    }

    public SectionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DbHelper db = DbHelper.getInstance(getActivity());

        mItems = new ArrayList<>();

        List<Section> sectionsTable;

        try {
            Table<Section> sections = db.getTableFor(Section.class);
            sectionsTable = sections.select(null, null, null, null, "num");

            Table<Exhibition> exhibitionTable = db.getTableFor(Exhibition.class);
            for (Section section : sectionsTable) {
                mItems.add(new Item(section.id, section.num, section.name));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        mAdapter = new SectionsAdapter(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sections_list, container, false);

        AbsListView listView = (AbsListView) view.findViewById(android.R.id.list);
        listView.setAdapter(mAdapter);
        listView.setTextFilterEnabled(true);
        listView.setOnItemClickListener(this);

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
        if (null != mListener)
            mListener.onFragmentInteraction(mItems.get(position).id,
                    mItems.get(position).num,
                    mItems.get(position).title);
    }

    private class SectionsAdapter extends BaseAdapter implements Filterable {

        private LayoutInflater mLayoutInflater;
        public List<Item> mBackupItems;
        final int mImagesId[] = SharedData.SECTION_IMAGE_RESOURCES;

        @Override
        public Filter getFilter() {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    final FilterResults results = new FilterResults();
                    final List<Item> resultItems = new ArrayList<>();
                    if (mBackupItems == null)
                        mBackupItems = mItems;
                    if (constraint != null) {
                        String search = constraint.toString().toLowerCase();
                        if (mBackupItems != null && mBackupItems.size() > 0) {
                            for (Item item : mBackupItems) {
                                if (item.title != null && item.title.toLowerCase().contains(search))
                                    resultItems.add(item);
                            }
                        }
                        results.values = resultItems;
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

        private class SectionHolder {
            ImageView imageLogo;
            TextView textName;
            View card_layout;
        }

        public SectionsAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
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
            Item item = mItems.get(position);

            final SectionHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(
                        R.layout.fragment_sections_item, parent, false);
                holder = new SectionHolder();
                holder.imageLogo = (ImageView)convertView.findViewById(R.id.imageLogo);
                holder.textName = (TextView) convertView.findViewById(R.id.textName);
                holder.card_layout = convertView.findViewById(R.id.card_layout);

                convertView.setTag(holder);

            } else {
                holder = (SectionHolder) convertView.getTag();
            }

            if ( position % 2 == 0) {
                if (holder.card_layout != null)
                    holder.card_layout.setBackgroundResource(R.drawable.listview_selector_even);
                else
                    convertView.setBackgroundResource(R.drawable.listview_selector_even);
            }
            else {
                if (holder.card_layout != null)
                    holder.card_layout.setBackgroundResource(R.drawable.listview_selector_odd);
                else
                    convertView.setBackgroundResource(R.drawable.listview_selector_odd);
            }


            holder.textName.setText(item.title.replace('\n', ' '));

            ViewGroup.LayoutParams params = holder.imageLogo.getLayoutParams();
            Bitmap b = ImageLoader.decodeSampledBitmapFromResource(getResources(),
                    mImagesId[item.num % (mImagesId.length)], params.width, params.height);
            holder.imageLogo.setImageBitmap(b);

            return convertView;
        }
    }


    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mAdapter.getFilter().filter(newText);
        return true;
    }

}
