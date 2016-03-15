package ru.gkpromtech.exhibition.catalog;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Pair;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.analytics.GoogleAnalytics;

import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.exhibitions.ExhibitionActivity;
import ru.gkpromtech.exhibition.exhibitions.ExhibitionFragment;
import ru.gkpromtech.exhibition.model.Entity;
import ru.gkpromtech.exhibition.model.Exhibition;
import ru.gkpromtech.exhibition.model.Group;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.Organization;
import ru.gkpromtech.exhibition.model.Place;
import ru.gkpromtech.exhibition.organizations.OrganizationItem;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.ImageLoader;
import ru.gkpromtech.exhibition.utils.SharedData;


public class SectionContentActivity extends ActionBarActivity
        implements SectionContentFragment.OnFragmentInteractionListener {

    ExhibitionFragment mExhibitionFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        int sectionId = intent.getIntExtra("sectionId", 0);
        int sectionNum = intent.getIntExtra("sectionNum", 0);
        String sectionTitle = intent.getStringExtra("sectionTitle");

        setContentView(R.layout.layout_section_content);

        ImageView imageView = (ImageView) findViewById(R.id.imageSectionPreview);
        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        Bitmap b = ImageLoader.decodeSampledBitmapFromResource(getResources(),
                SharedData.SECTION_IMAGE_RESOURCES[sectionNum % (SharedData.SECTION_IMAGE_RESOURCES.length)], params.width, params.height);
        imageView.setImageBitmap(b);

        TextView headerText = (TextView) findViewById(R.id.textHeader);
        headerText.setText(sectionTitle);

        FrameLayout detailsFrag = (FrameLayout) findViewById(R.id.details_frag);
        boolean isCheckable = detailsFrag != null;

        SectionContentFragment sectionContentFragment = SectionContentFragment.newInstance(sectionId, isCheckable);
        getFragmentManager().beginTransaction().replace(R.id.list_frag, sectionContentFragment).commit();
        sectionContentFragment.onCreate(null);

        if (detailsFrag != null) {
            SectionContentFragment.Item firsExhibition = sectionContentFragment.getFirstExhibition();
            if (firsExhibition != null) {
                OrganizationItem organizationItem = getOrganizationItem(firsExhibition.exhibition.id,
                        firsExhibition.place);
                mExhibitionFragment = ExhibitionFragment.newInstance(organizationItem.group.position,
                        firsExhibition.exhibition, firsExhibition.media, organizationItem);
                getFragmentManager().beginTransaction().replace(R.id.details_frag, mExhibitionFragment).commit();
            }
        }

        AnalyticsManager.sendEvent(this, R.string.catalog_category, R.string.action_section, sectionNum);
    }

    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onFragmentInteraction(Exhibition exhibition, Place place, Media media) {

        FrameLayout detailsFrag = (FrameLayout) findViewById(R.id.details_frag);
        OrganizationItem organizationItem = getOrganizationItem(exhibition.id, place);

        if (detailsFrag == null) {
            Intent intent = new Intent(this, ExhibitionActivity.class);

            intent.putExtra("schemaId", organizationItem.group.position);
            intent.putExtra("exhibition", exhibition);
            intent.putExtra("media", media);
            intent.putExtra("organization", organizationItem);

            startActivity(intent);
        } else {
            if (mExhibitionFragment != null) {
                getFragmentManager().beginTransaction().remove(mExhibitionFragment).commit();
            }
            mExhibitionFragment = ExhibitionFragment.newInstance(organizationItem.group.position,
                    exhibition, media, organizationItem);
            getFragmentManager().beginTransaction().replace(R.id.details_frag,
                    mExhibitionFragment).commit();
        }
    }

    private OrganizationItem getOrganizationItem(int exhibitionId, Place place) {
        OrganizationItem organizationItem = new OrganizationItem();
        DbHelper db = DbHelper.getInstance(this);

        Table<Exhibition> exhibitionTable = db.getTableFor(Exhibition.class);
        try {
            List<Pair<Entity[], Exhibition>> exhibitions = exhibitionTable.selectJoined(
                    new Table.Join[]{
                            new Table.Join("organizationid", Organization.class, "id"),
                            new Table.Join("placeid", Place.class, "id"),
                            new Table.Join(Group.class, "f1.groupid = f2.id"),
                    },
                    "t.id = ?", new String[]{String.valueOf(exhibitionId)}, null, null
            );

            if (!exhibitions.isEmpty()) {
                Organization org = (Organization) exhibitions.get(0).first[0];
                Group group = (Group) exhibitions.get(0).first[2];
                organizationItem = new OrganizationItem(group, place, org);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return organizationItem;
    }
}


