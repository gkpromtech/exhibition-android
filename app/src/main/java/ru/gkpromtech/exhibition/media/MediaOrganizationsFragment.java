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
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.Organization;

public class MediaOrganizationsFragment extends Fragment {
    private final static int MAX_ROWS_IN_ORG_GROUP = 1;

    private Context mContext;

    public MediaOrganizationsFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.fragment_media_tab, container, false);
        mContext = getActivity();

        final MediaGalleryView tab = (MediaGalleryView)view.findViewById(R.id.tab1);
        setTabContentByOrg(tab);

        return view;
    }

    void setTabContentByOrg(MediaGalleryView tab) {
        final Hashtable<String, List<Media>> images_by_org = new Hashtable<String, List<Media>>();
        final Hashtable<String, Organization> organizations = new Hashtable<String, Organization>();

        try {
            Table<Media> media = DbHelper.getInstance(getActivity()).getTableFor(Media.class);

            List<Pair<Entity[], Media>> joinedMedias =
                    media.selectJoined(new Table.Join[]{
                                    new Table.Join("organizationid", Organization.class, "id")
                            },
                            "(t.type=0 OR t.type=1)",
                            null,
                            "f0.shortname");

            for (Pair<Entity[], Media> m : joinedMedias) {
                Organization org = (Organization) m.first[0];

                if (!images_by_org.containsKey(org.shortname)) {
                    images_by_org.put(org.shortname, new ArrayList<Media>());
                    organizations.put(org.shortname, org);
                }
                images_by_org.get(org.shortname).add(m.second);
            }

            // Отображение по организациям
            for (String id : images_by_org.keySet()) {
                List<Media> mediaList = images_by_org.get(id);

                if (mediaList.size() > 0) {
                    tab.add(id, mediaList, MAX_ROWS_IN_ORG_GROUP);
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        tab.setOnMediaGalleryInteraction(new MediaGalleryView.MediaGalleryInteraction() {
            @Override
            public void onItemCliked(String group, int index) {
                List<Media> items = images_by_org.get(group);
                Media item = items.get(index);
                if (item.type == Media.IMAGE) {
                    Intent i = new Intent(getActivity(), FullImageActivity.class);
                    i.putExtra("items", (Serializable) items);
                    i.putExtra("index", index);
                    startActivity(i);
                } else {
                    VideoPlayerActivity.showVideo(mContext, item);
//                        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(item.url)));
                }
            }

            @Override
            public void onMoreButtonClicked(String group) {
                List<Media> items = images_by_org.get(group);

                Intent i = new Intent(getActivity(), ImagesGridActivity.class);
                i.putExtra("items", (Serializable) items);
                i.putExtra("header", group);
                startActivity(i);
            }

            @Override
            public void onOnlineItemClicked(int index) {
            }

        });

    }
}
