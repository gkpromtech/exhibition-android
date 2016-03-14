package ru.gkpromtech.exhibition.media;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import ru.gkpromtech.exhibition.NavigationActivity;
import ru.gkpromtech.exhibition.R;

public class MediaActivity extends NavigationActivity implements MediaFavoriteFragment.OnFragmentInteractionListener{

    private final static int REQUEST_IMAGE_CAPTURE = 1;

    private final static String FRAGMENT_ALL_TAG = "all";
    private final static String FRAGMENT_ORGANIZATIONS_TAG = "orgs";
    private final static String FRAGMENT_FAVORITE_TAG = "fav";

    private String appDirectoryName;
    private File imageRoot;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        appDirectoryName = getResources().getString(R.string.app_name);
        imageRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), appDirectoryName);

        FrameLayout view = (FrameLayout)findViewById(R.id.container);
        getLayoutInflater().inflate(R.layout.activity_media, view, true);

        TabHost tabHost = (TabHost)findViewById(R.id.tabHost_media);
        tabHost.setup();

        TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {

            @Override
            public void onTabChanged(String tabId) {
                FragmentManager fm = getFragmentManager();
                Fragment fragment_all = fm.findFragmentByTag(FRAGMENT_ALL_TAG);
                Fragment fragment_organizations = fm.findFragmentByTag(FRAGMENT_ORGANIZATIONS_TAG);
                Fragment fragment_favorite = fm.findFragmentByTag(FRAGMENT_FAVORITE_TAG);

                FragmentTransaction ft = fm.beginTransaction();
                if (fragment_all != null)
                    ft.detach(fragment_all);
                if (fragment_organizations != null)
                    ft.detach(fragment_organizations);
                if (fragment_favorite != null)
                    ft.detach(fragment_favorite);

                if (tabId.equalsIgnoreCase(FRAGMENT_ALL_TAG)) {

                    if (fragment_all == null) {
                        ft.add(R.id.realtabcontent, new MediaAllFragment(), FRAGMENT_ALL_TAG);
                    } else {
                        ft.attach(fragment_all);
                    }

                }
                else if (tabId.equalsIgnoreCase(FRAGMENT_ORGANIZATIONS_TAG)) {
                    if (fragment_organizations == null) {
                        ft.add(R.id.realtabcontent, new MediaOrganizationsFragment(), FRAGMENT_ORGANIZATIONS_TAG);
                    } else {
                        ft.attach(fragment_organizations);
                    }
                }
                else {
                    if (fragment_favorite == null) {
                        ft.add(R.id.realtabcontent,new MediaFavoriteFragment(), FRAGMENT_FAVORITE_TAG);
                    } else {
                        ft.attach(fragment_favorite);
                    }
                }
                ft.commit();
            }
        };

        tabHost.setOnTabChangedListener(tabChangeListener);

        TabHost.TabSpec tSpec1 = tabHost.newTabSpec(FRAGMENT_ALL_TAG);
        tSpec1.setIndicator(createIndicatorView(tabHost, getResources().getString(R.string.media_tab1), R.drawable.ic_white_images_all));
        tSpec1.setContent(new DummyTabContent(getBaseContext()));
        tabHost.addTab(tSpec1);

        TabHost.TabSpec tSpec2 = tabHost.newTabSpec(FRAGMENT_ORGANIZATIONS_TAG);
        tSpec2.setIndicator(createIndicatorView(tabHost, getResources().getString(R.string.media_tab2), R.drawable.ic_white_images_org));
        tSpec2.setContent(new DummyTabContent(getBaseContext()));
        tabHost.addTab(tSpec2);

        TabHost.TabSpec tSpec3 = tabHost.newTabSpec(FRAGMENT_FAVORITE_TAG);
        tSpec3.setIndicator(createIndicatorView(tabHost, getResources().getString(R.string.media_tab3), R.drawable.ic_white_my_photo));
        tSpec3.setContent(new DummyTabContent(getBaseContext()));
        tabHost.addTab(tSpec3);
    }

    private View createIndicatorView(TabHost tabHost, String text, int resId) {
        View tabIndicator = LayoutInflater.from(this).inflate(R.layout.layout_image_tab_indicator,
                tabHost.getTabWidget(), false);

        final TextView textView = (TextView) tabIndicator.findViewById(R.id.textView);
        final ImageView imageView = (ImageView) tabIndicator.findViewById(R.id.imageView);
        textView.setText(text);
        imageView.setImageDrawable(getResources().getDrawable(resId));

        return tabIndicator;
    }

    @Override
    public void onNewPhotoClicked() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    public void onLongClicked(final int index, View anchor) {
        PopupMenu p = new PopupMenu(MediaActivity.this, anchor);
        p.getMenuInflater().inflate(R.menu.menu_media_popup, p.getMenu());

        p.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        FragmentManager fm = getFragmentManager();
                        MediaFavoriteFragment fragment3 = (MediaFavoriteFragment) fm.findFragmentByTag(FRAGMENT_FAVORITE_TAG);
                        if (fragment3 != null) {
                            if (fragment3.deleteItem(index) == true) {
                                Toast.makeText(getApplicationContext(), getResources().getText(R.string.photo_delete_ok), Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), getResources().getText(R.string.photo_delete_error), Toast.LENGTH_SHORT).show();
                            }
                        }

                        return true;
                    default:
                        return false;
                }
            }
        });
        p.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            String filename = getResources().getString(R.string.app_name) + "_" + sdf.format(new Date()) + ".png";
            String fullPath = doSaveImage(imageBitmap, filename);

            FragmentManager fm = getFragmentManager();
            MediaFavoriteFragment fragment3 = (MediaFavoriteFragment) fm.findFragmentByTag("tab3");
            if (fragment3 != null) {
                fragment3.addItem(fullPath);
            }
        }
    }

    private String doSaveImage(Bitmap imageBitmap, String filename) {
        imageRoot.mkdirs();
        File newFile = new File(imageRoot.getAbsolutePath() + "/" + filename);
        saveBitmapToFile(newFile, imageBitmap);
        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)));

        Toast.makeText(this, getResources().getText(R.string.photo_saved), Toast.LENGTH_SHORT).show();

        return newFile.getAbsolutePath();
    }

    public void saveBitmapToFile(File path, Bitmap bmp){
        FileOutputStream out = null;
        try {
            out = new FileOutputStream(path);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, out); // bmp is your Bitmap instance
            // PNG is a lossless format, the compression factor (100) is ignored
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (out != null) {
                    out.flush();
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public class DummyTabContent implements TabHost.TabContentFactory {
        private Context mContext;

        public DummyTabContent(Context context){
            mContext = context;
        }

        @Override
        public View createTabContent(String tag) {
            View v = new View(mContext);
            return v;
        }
    }

}
