package ru.gkpromtech.exhibition.media;


import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.events.EventReader;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.Online;
import ru.gkpromtech.exhibition.model.Organization;

public class MediaAllFragment extends Fragment {

    private final static int MAX_ROWS_IN_DAY_GROUP = 2;

    private Table<Online> onlineTable;
    private List<Online> online;
    private Context mContext;

    private final static SimpleDateFormat mDateFormat =
            new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());

    public MediaAllFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_tab, container, false);
        mContext = getActivity();

        final MediaGalleryView tab = (MediaGalleryView)view.findViewById(R.id.tab1);
        setTabContentByDate(tab);

        mDateFormat.setTimeZone(TimeZone.getDefault());

        return view;
    }

    void setTabContentByDate(MediaGalleryView tab1) {
        final Hashtable<String, List<Media>> images_by_date = new Hashtable<>();

        onlineTable = DbHelper.getInstance(getActivity()).getTableFor(Online.class);
        online = new ArrayList<>();
        online = onlineTable.select(null, null, null, null, null);

        List<Date> days = EventReader.getInstance(mContext).getDays();

        Calendar calendar = Calendar.getInstance();
        calendar.set(1970, 1, 1);
        Date dayOld = calendar.getTime();
        calendar.set(2100, 12, 31);
        Date dayFuture = calendar.getTime();

        // Идентификаторы Организаторов и соорганизаторов
        Table<Organization> organizations = DbHelper.getInstance(getActivity()).getTableFor(Organization.class);
        String selection = "status IN (0, 1)";
        List<Organization> rows = organizations.select(selection, null, null, null, "status");
        Integer[] MKV_ORG_ID = new Integer[rows.size()];
        int org_index = 0;
        for (Organization org: rows) {
            MKV_ORG_ID[org_index++] = org.id;
        }

        // все до начала выставки
        final List<Media> itemsOld = getMedia(dayOld, days.get(0), MKV_ORG_ID);
        if (itemsOld.size() > 0) {
            String text = getResources().getString(R.string.media_title_old_images);
            images_by_date.put(text, itemsOld);
            tab1.add(text, itemsOld, MAX_ROWS_IN_DAY_GROUP);
        }

        // дни во время выставки
        for (int i = 1; i < days.size()-1; i ++) {
            List<Media> media = getMedia(days.get(i), days.get(i+1), MKV_ORG_ID);
            if (media.size() > 0) {
                tab1.add(mDateFormat.format(days.get(i)), media, MAX_ROWS_IN_DAY_GROUP);
            }
        }

        // все после выставки
        final List<Media> itemsFuture = getMedia(days.get(days.size()-1), dayFuture, MKV_ORG_ID);
        if (itemsFuture.size() > 0) {
            String text = getResources().getString(R.string.media_title_afterparty_images);
            images_by_date.put(text, itemsFuture);
            tab1.add(text, itemsFuture, MAX_ROWS_IN_DAY_GROUP);
        }

        tab1.setOnMediaGalleryInteraction(new MediaGalleryView.MediaGalleryInteraction() {
            @Override
            public void onItemCliked(String group, int index) {
                List<Media> items = images_by_date.get(group);
                Media item = items.get(index);
                if (item.type == Media.IMAGE) {
                    Intent i = new Intent(getActivity(), FullImageActivity.class);
                    i.putExtra("items", (Serializable) items);
                    i.putExtra("index", index);
                    startActivity(i);
                }
                else {
                    VideoPlayerActivity.showVideo(mContext, item);
                }
            }

            @Override
            public void onMoreButtonClicked(String group) {
                List<Media> items = images_by_date.get(group);

                Intent i = new Intent(getActivity(), ImagesGridActivity.class);
                i.putExtra("items", (Serializable) items);
                i.putExtra("header", group);
                startActivity(i);
            }

            @Override
            public void onOnlineItemClicked(int index) {
                Online channel = online.get(index);
                VideoPlayerActivity.showOnlineVideo(getActivity(), channel);
            }

        });
    }

    private List<Media> getMedia(Date from, Date to, Integer[] organizationIds) {
        List<Media> result = new ArrayList<Media>();

        try {
            Table<Media> media = DbHelper.getInstance(getActivity()).getTableFor(Media.class);

            String selection = "f0.createtime >= ? AND f0.createtime < ? AND (t.type=0 OR t.type=1)";

            int orgsLen = (organizationIds != null)? organizationIds.length: 0;

            String[] args = new String[2 + orgsLen];
            args[0] = String.valueOf(from.getTime());
            args[1] = String.valueOf(to.getTime());
            if (orgsLen > 0) {
                selection += " AND t.organizationid IN (" + DbHelper.makePlaceholders(organizationIds.length) + ")";
                System.arraycopy(DbHelper.makeArguments(organizationIds), 0, args, 2, organizationIds.length);
            }

            List<Pair<Entity[], Media>> joinedMedias =
                    media.selectJoined(new Table.Join[]{
                                    new Table.Join("id", ru.gkpromtech.exhibition.model.Object.class, "id")
                            },
                            selection, args, "f0.createtime DESC");

            for (Pair<Entity[], Media> m : joinedMedias) {
                result.add(m.second);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }
}