package ru.gkpromtech.exhibition.catalog;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.ObjectsMedia;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class SectionContentFragment extends Fragment
        implements AbsListView.OnItemClickListener {

    private static final String SECTION_ID = "section_id";
    private static final String IS_CHECKABLE = "is_checkable";

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Item curItem = mItems.get(position);
        mListener.onFragmentInteraction(curItem.exhibition, curItem.place, curItem.media);

        if (mIsCheckable)
            mListView.setItemChecked(position, true);
    }

    public class Item {

        public Exhibition exhibition;
        public Media media;
        public Place place;

        public Item(Exhibition exhibition, Media media, Place place) {
            this.exhibition = exhibition;
            this.media = media;
            this.place = place;
        }
    }

    public Item getFirstExhibition() {
        if (!mItems.isEmpty())
            return mItems.get(0);
        else
            return null;
    }

    private List<Item> mItems;
    private OnFragmentInteractionListener mListener;
    private boolean mIsCheckable = false;
    private ListView mListView;


    public static SectionContentFragment newInstance(int sectionId, boolean isCheckable) {
        SectionContentFragment fragment = new SectionContentFragment();

        Bundle args = new Bundle();
        args.putInt(SECTION_ID, sectionId);
        args.putBoolean(IS_CHECKABLE, isCheckable);
        fragment.setArguments(args);

        return fragment;
    }

    public SectionContentFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            int sectionId = getArguments().getInt(SECTION_ID);
            mIsCheckable = getArguments().getBoolean(IS_CHECKABLE);

            mItems = new ArrayList<>();

            DbHelper db = DbHelper.getInstance(getActivity());

            Table<Exhibition> exhibitionTable = db.getTableFor(Exhibition.class);
            try {
                List<Pair<Entity[], Exhibition>> exhibitions = exhibitionTable.selectJoined(
                        new Table.Join[]{
                                new Table.Join("id", ObjectsMedia.class, "objectid", "left"),
                                new Table.Join(Media.class, "f0.mediaid = f1.id and f1.type = 0"),
                                new Table.Join("placeid", Place.class, "id")
                        },
                        "t.sectionid = ?", new String[]{String.valueOf(sectionId)}, "t.organizationid", "t.id");

                for (Pair<Entity[], Exhibition> res : exhibitions) {
                    Exhibition exhibition = res.second;
                    Media media = (Media) res.first[1];
                    Place place = (Place) res.first[2];

                    mItems.add(new Item(exhibition, media, place));
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_section_content, container, false);

        ListAdapter adapter = new ExhibitionsAdapter(getActivity());

        mListView = (ListView) view.findViewById(android.R.id.list);
        mListView.setAdapter(adapter);
        mListView.setOnItemClickListener(this);

        if (mIsCheckable)
            mListView.setItemChecked(mListView.getHeaderViewsCount(), true);

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


    public interface OnFragmentInteractionListener {

        void onFragmentInteraction(Exhibition exhibition, Place place, Media media);
    }

    private class ExhibitionsAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        private class ExhibitionHolder {
            private ImageView imagePreview;
            private TextView textTitle;
            private TextView description;
            private TextView place;
        }

        public ExhibitionsAdapter(Context context) {
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

            final ExhibitionHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(
                        R.layout.exhibition_list_item, parent, false);
                holder = new ExhibitionHolder();
                holder.imagePreview = (ImageView)convertView.findViewById(R.id.imagePreview);
                holder.textTitle = (TextView) convertView.findViewById(R.id.textTitle);
                holder.description = (TextView) convertView.findViewById(R.id.textDescription);
                holder.place = (TextView) convertView.findViewById(R.id.textPlace);

                convertView.setTag(holder);
            } else {
                holder = (ExhibitionHolder) convertView.getTag();
            }

            holder.textTitle.setText(item.exhibition.name.replace('\n', ' '));
            holder.description.setText(item.exhibition.text.replace('\n',' '));
            holder.place.setText(item.place.name);

            ImageLoader.load(item.media.preview, holder.imagePreview);

            convertView.setBackgroundResource((position % 2 == 0)
                    ? R.drawable.listview_selector_even
                    : R.drawable.listview_selector_odd);

            if (mIsCheckable &&
                    mListView.getCheckedItemPosition() == position + mListView.getHeaderViewsCount()) {
                convertView.setBackgroundResource(R.color.Exhibition_Secondary);

                holder.textTitle.setTextColor(getResources().getColor(R.color.Exhibition_White));
                holder.description.setTextColor(getResources().getColor(R.color.Exhibition_White));
            } else {
                holder.textTitle.setTextColor(getResources().getColor(R.color.Exhibition_Black));
                holder.description.setTextColor(getResources().getColor(R.color.Exhibition_DarkGray));
            }


            return convertView;
        }
    }

}
