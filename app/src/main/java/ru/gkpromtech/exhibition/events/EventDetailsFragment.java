package ru.gkpromtech.exhibition.events;


import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Event;
import ru.gkpromtech.exhibition.model.EventFavorite;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.model.Tag;
import ru.gkpromtech.exhibition.utils.ImageLoader;


public class EventDetailsFragment extends Fragment  {
    private Event item;
    private List<Media> mediaItems;
    private int mImagePreviewPosition;

    private SimpleDateFormat mDateFormat =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    private SimpleDateFormat mTimeFormat =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    private OnFragmentInteractionListener mListener = null;

    public EventDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        item = (Event) getArguments().getSerializable("item");
        mediaItems = EventReader.getInstance(getActivity()).getMedia(item.id, EventReader.MEDIA_FILTER_ALL);
        mImagePreviewPosition = 0;

        mDateFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
        mTimeFormat.setTimeZone(TimeZone.getTimeZone("GMT+03:00"));
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mListener = (OnFragmentInteractionListener)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_event_details, container, false);

        final TextView textHeader = (TextView) view.findViewById(R.id.text_header);
        final ImageView imageEvent = (ImageView) view.findViewById(R.id.imageEvent);
        final TextView textDate = (TextView) view.findViewById(R.id.textDate);
        final TextView textTime = (TextView) view.findViewById(R.id.textTime);
        final TextView textPlace = (TextView) view.findViewById(R.id.textPlace);
        final TextView textEvent = (TextView) view.findViewById(R.id.textEvent);
        final TextView textTags = (TextView) view.findViewById(R.id.tags);
        final ImageButton imageFlag = (ImageButton) view.findViewById(R.id.imageFlag);
        final ImageButton imagePlace = (ImageButton) view.findViewById(R.id.imagePlace);

        textHeader.setText(item.header);
        textDate.setText(mDateFormat.format(item.date) + "/");
        textTime.setText(mTimeFormat.format(item.date));

        EventFavorite fav = EventReader.getInstance(getActivity()).getEventFavorite(item.id);
        imageFlag.setSelected(fav.favorite != 0);

        String place = new String();
        List<Place> places = EventReader.getInstance(getActivity()).getPlaces(item.id);
        for (Place p: places) {
            place = place + p.name + " ";
        }
        textPlace.setText(place);

        textEvent.setText(item.details);

        String sTags = new String(getResources().getString(R.string.title_tags) + ": ");
        List<Tag> tags = EventReader.getInstance(getActivity()).getTags(item.id);
        for (Tag t: tags) {
            sTags = sTags + t.tag + " ";
        }
        textTags.setText(sTags);

        imageFlag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imageFlag.setSelected(!imageFlag.isSelected());
                int state = imageFlag.isSelected()? 1: 0;
                mListener.onFavoriteClicked(item.id, state);
            }
        });

        final HListView previewList = (HListView)view.findViewById(R.id.hListView1);
        previewList.setHeaderDividersEnabled(true);
        previewList.setFooterDividersEnabled(true);

        if (mediaItems.size() <= 1) {
            previewList.setVisibility(View.INVISIBLE);
        }
        else {

            final HImageListAdapter mAdapter = new HImageListAdapter(getActivity(), R.layout.layout_image_preview, R.id.picture, mediaItems);
            previewList.setAdapter(mAdapter);

            previewList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    mImagePreviewPosition = position;
                    if (mListener != null) {
                        mListener.onPreviewImageClicked(item, mImagePreviewPosition);
                    }
                }
            });
        }

        if (mediaItems.isEmpty()) {
            imageEvent.setVisibility(ViewGroup.GONE);
        }
        else {
            Media m = mediaItems.get(mImagePreviewPosition);
            String url = m.url;
            if (m.type == Media.VIDEO)
                url = m.preview;

            imageEvent.setVisibility(ViewGroup.VISIBLE);
            ImageLoader.load(url, imageEvent);

            imageEvent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null)
                        mListener.onPreviewImageClicked(item, 0);
                }
            });
        }

        return view;
    }

    public interface OnFragmentInteractionListener {
        public void onFavoriteClicked(int eventId, int state);
        public void onPreviewImageClicked(Event event, int position);
        public void onGotoImagesClicked(Event event);
    }

    class HImageListAdapter extends ArrayAdapter<Media> {

        private final int PREVIEW_HEIGHT;
        private final int PREVIEW_WIDTH;

        Context context;
        List<Media> mItems;
        LayoutInflater mInflater;
        int mResource;
        int mImageResId;

        public HImageListAdapter(Context context, int resourceId, int imageViewResourceId, List<Media> objects) {
            super( context, resourceId, imageViewResourceId, objects );
            this.context = context;
            mInflater = LayoutInflater.from( context );
            mResource = resourceId;
            mImageResId = imageViewResourceId;
            mItems = objects;

            PREVIEW_HEIGHT = (int)(80f * (context.getResources().getDisplayMetrics().density));
            PREVIEW_WIDTH = (int)(120f * (context.getResources().getDisplayMetrics().density));
        }

        @Override
        public long getItemId( int position ) {
            return getItem(position).id;
        }

        @Override
        public View getView( int position, View convertView, ViewGroup parent ) {

            if( null == convertView ) {
                convertView = mInflater.inflate( mResource, parent, false );
            }
            final ImageView picture = (ImageView) convertView.findViewById(mImageResId);
            final ImageView picturePlay = (ImageView) convertView.findViewById(R.id.picture_video);
            ViewGroup.LayoutParams param = picture.getLayoutParams();
            param.width = PREVIEW_WIDTH;
            param.height = PREVIEW_HEIGHT;
            picture.setLayoutParams(param);

            Media m = mItems.get(position);
            picturePlay.setVisibility(m.type == Media.VIDEO? View.VISIBLE: View.INVISIBLE);
            ImageLoader.load(m.preview, picture);

            return convertView;
        }
    }
}
