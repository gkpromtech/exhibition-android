package ru.gkpromtech.exhibition.organizations;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import it.sephiroth.android.library.widget.AdapterView;
import it.sephiroth.android.library.widget.HListView;
import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Map;
import ru.gkpromtech.exhibition.model.MapsPoint;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.ObjectsMedia;
import ru.gkpromtech.exhibition.schema.SchemaActivity;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class OrganizationPlaceFragment extends OrganizationBaseFragment {
    int mImagePreviewPosition = 0;

    OrganizationPlaceFragmentListener mListener;

    public OrganizationPlaceFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_organization_place, container, false);
        super.init(v);

        final OrganizationItem item = getOrganization();

        if (!item.organization.address.isEmpty()) {
            ((TextView) v.findViewById(R.id.textAddress)).setText(item.organization.address);
        } else {
            v.findViewById(R.id.textAddressHeader).setVisibility(View.GONE);
            v.findViewById(R.id.textAddress).setVisibility(View.GONE);
        }

        String text = "";
        if (!item.organization.phone.isEmpty())
            text += getString(R.string.phone) + ": " + item.organization.phone + "\n";
        if (!item.organization.email.isEmpty())
            text += getString(R.string.email) + ": " + item.organization.email + "\n";
        if (!item.organization.site.isEmpty())
            text += getString(R.string.www) + ": " + item.organization.site + "\n";

        if (!text.isEmpty()) {
            ((TextView) v.findViewById(R.id.textContacts)).setText(text);
        } else {
            v.findViewById(R.id.textContactsHeader).setVisibility(View.GONE);
            v.findViewById(R.id.textContacts).setVisibility(View.GONE);
        }

        if (item.organization.description != null && !item.organization.description.isEmpty()) {
            String data = "<html><body><p>" + item.organization.description + "</p></body></html>";
            data = data.replace("\n", "<br/>").replace("\r\n", "<br/>");
            ((WebView) v.findViewById(R.id.textDescription)).loadData(data , "text/html; charset=utf-8", "utf-8");

        }
        else {
            v.findViewById(R.id.textDescription).setVisibility(View.INVISIBLE);
        }

        ImageView imageView = (ImageView) v.findViewById(R.id.imagePlace);
        Media media = (Media) getArguments().getSerializable("media");
        String url = null;
        if (media != null)
            url = (media.type == Media.IMAGE) ? media.url : media.preview;
        if (url != null && !url.isEmpty()) {
            ImageLoader.load(url, imageView);
        } else {
            imageView.setVisibility(View.GONE);
        }

        View imageShowOnSchema = v.findViewById(R.id.imageShowOnSchema);
        if (imageShowOnSchema != null)
            imageShowOnSchema.setOnClickListener(mOnClick);

        final HListView previewList = (HListView)v.findViewById(R.id.hListView1);
        previewList.setHeaderDividersEnabled(true);
        previewList.setFooterDividersEnabled(true);

        final List<Media> mediaItems = getMedia(item.organization.id);
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
                        mListener.onPreviewImageClicked(mediaItems, mImagePreviewPosition);
                    }
                }
            });
        }
        return v;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OrganizationPlaceFragmentListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException("Activity must implement OrganizationPlaceFragmentListener.");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    View.OnClickListener mOnClick = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            OrganizationItem organizationItem = getOrganization();
            String groupId;
            String schemaId = organizationItem.place.schemaid;
            int pos = schemaId.indexOf('-');
            if (pos != -1)
                schemaId = schemaId.substring(0, pos);
            try {
                Table<MapsPoint> mapsPointTable = DbHelper.getInstance(getActivity()).getTableFor(MapsPoint.class);
                List<Pair<Entity[], MapsPoint>> result = mapsPointTable.selectJoined(new Table.Join[]{
                        new Table.Join("mapid", Map.class, "id")
                }, "placename = ?", new String[]{schemaId}, null);
                if (result.isEmpty())
                    return;

                groupId = ((Map) result.get(0).first[0]).groupname;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }

            List<SchemaActivity.Marker> markers = new ArrayList<>();
            markers.add(new SchemaActivity.Marker(schemaId,
                    organizationItem.organization.shortname, organizationItem.organization));

            Intent intent = new Intent(getActivity(), SchemaActivity.class);
            intent.putExtra("markers", (Serializable) markers);
            startActivity(intent);
        }
    };

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

    private List<Media> getMedia(int organizationId) {
        List<Media> result = new ArrayList<>();

        try {
            Table<ObjectsMedia> organization_media = DbHelper.getInstance(getActivity()).getTableFor(ObjectsMedia.class);

            List<ObjectsMedia> rows = organization_media.select("objectid = ?",
                    new String[]{String.valueOf(organizationId)}, null, null, "ordernum");

            if (rows.size() > 0) {
                Table<Media> media = DbHelper.getInstance(getActivity()).getTableFor(Media.class);

                Integer[] mediaIds = new Integer[rows.size()];
                for (int i = 0; i < rows.size(); i ++) {
                    mediaIds[i] = rows.get(i).mediaid;
                }

                String selection2 = "id IN (" + DbHelper.makePlaceholders(mediaIds.length) + ")";
                String[] args = DbHelper.makeArguments(mediaIds);

                result = media.select(selection2, args, null, null, null);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public interface OrganizationPlaceFragmentListener {
        void onPreviewImageClicked(List<Media> media, int index);
    }
}
