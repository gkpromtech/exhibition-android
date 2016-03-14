package ru.gkpromtech.exhibition.media;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ShareActionProvider;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.List;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.db.DbHelper;
import ru.gkpromtech.exhibition.db.Table;
import ru.gkpromtech.exhibition.events.util.SystemUiHider;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.MediaFavorite;
import ru.gkpromtech.exhibition.utils.Images;

public class FullImageActivity extends FragmentActivity {

    private static final boolean AUTO_HIDE = false;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;
    private static final int HIDER_FLAGS = SystemUiHider.FLAG_HIDE_NAVIGATION;
    private SystemUiHider mSystemUiHider;

    private static final int SHOW_ITEMS_MODE = 1;
    private static final int SHOW_FILES_MODE = 2;

    private List<Media> items;
    private List<String> files;
    private ShareActionProvider mShareActionProvider;
    private Intent shareIntent = null;
    private String appDirectoryName;
    private File imageRoot;

    private int mode = SHOW_ITEMS_MODE;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_full_image);

        Bundle extras = getIntent().getExtras();
        items = (List<Media>) extras.getSerializable("items");
        files = (List<String>) extras.getSerializable("files");
        int index = extras.getInt("index");

        if (items != null)
            mode = SHOW_ITEMS_MODE;
        else if (files != null)
            mode = SHOW_FILES_MODE;

        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final ViewPager pager = (ViewPager) findViewById(R.id.pager);

        appDirectoryName = getResources().getString(R.string.app_name);
        imageRoot = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), appDirectoryName);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        pager.setAdapter(new FullImagePagerAdapter(getSupportFragmentManager()));
        pager.setCurrentItem(index);
        if (mode == SHOW_ITEMS_MODE)
            setTitle(items.get(index).name);
        else if (mode == SHOW_FILES_MODE)
            setTitle(new File(files.get(index)).getName());
        else
            setTitle("Photo");

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, pager, HIDER_FLAGS);
        mSystemUiHider.setup();
        mSystemUiHider
                .setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
                    // Cached values.
                    int mControlsHeight;
                    int mShortAnimTime;

                    @Override
                    public void onVisibilityChange(boolean visible) {
                        // If the ViewPropertyAnimator API is available
                        // (Honeycomb MR2 and later), use it to animate the
                        // in-layout UI controls at the bottom of the
                        // screen.
                        if (mControlsHeight == 0) {
                            mControlsHeight = controlsView.getHeight();
                        }
                        if (mShortAnimTime == 0) {
                            mShortAnimTime = getResources().getInteger(
                                    android.R.integer.config_shortAnimTime);
                        }
                        controlsView.animate()
                                .translationY(visible ? 0 : mControlsHeight)
                                .setDuration(mShortAnimTime);

                        if (visible && AUTO_HIDE) {
                            // Schedule a hide().
                            delayedHide(AUTO_HIDE_DELAY_MILLIS);
                        }
                    }
                });

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {
            }

            @Override
            public void onPageSelected(int index) {
                if (mode == SHOW_ITEMS_MODE)
                    setTitle(items.get(index).name);
                else if (mode == SHOW_FILES_MODE)
                    setTitle(new File(files.get(index)).getName());
                else
                    setTitle("Photo");
            }

            @Override
            public void onPageScrollStateChanged(int i) {
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        pager.setOnTouchListener(mDelayHideTouchListener);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        if (AUTO_HIDE)
            delayedHide(100);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate menu resource file.
        getMenuInflater().inflate(R.menu.menu_fullscreen_image, menu);

        MenuItem itemSave = menu.findItem(R.id.menu_item_save);
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        itemSave.setVisible(mode == SHOW_FILES_MODE? false: true);

        // Fetch and store ShareActionProvider
        mShareActionProvider = (ShareActionProvider) shareItem.getActionProvider();
        if (mShareActionProvider == null) {
            // Following http://stackoverflow.com/questions/19358510/why-menuitemcompat-getactionprovider-returns-null
            mShareActionProvider = new ShareActionProvider(this);
        }

        final ViewPager pager = (ViewPager) findViewById(R.id.pager);
        int index = pager.getCurrentItem();
        Object mediaItem = getMediaItem(index);

        shareIntent = getShareIntent();
        setShareIntentMedia(shareIntent, mediaItem);
        setShareIntent(shareIntent);

        pager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Object item = getMediaItem(position);
                setShareIntentMedia(shareIntent, item);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_save:
                doSaveImage(((ViewPager) findViewById(R.id.pager)).getCurrentItem());
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private Object getMediaItem(int index) {
        if (mode == SHOW_ITEMS_MODE) {
            return items.get(index);
        }
        else if (mode == SHOW_FILES_MODE) {
            return files.get(index);
        }
        else {
            return null;
        }
    }

    private Intent getShareIntent() {
        if (shareIntent == null) {
            shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.setType("image/*");
        }
        return shareIntent;
    }

    // Call to update the share intent
    private void setShareIntent(Intent shareIntent) {
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    private void setShareIntentMedia(Intent shareIntent, Object item) {
        if (mode == SHOW_ITEMS_MODE) {
            String path;
            Media mediaItem = (Media)item;
            try {
                path = Images.getLocalPath(mediaItem.url);
            } catch (Throwable e) {
                return;
            }

            Uri uri = Uri.fromFile(new File(path));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, mediaItem.name);
            shareIntent.putExtra(Intent.EXTRA_TEXT, mediaItem.name);  // For debug
        }
        else if (mode == SHOW_FILES_MODE) {
            String path = (String)item;
            Uri uri = Uri.fromFile(new File(path));
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, new File(path).getName());
        }

    }

    private void doSaveImage(int index) {
        Media item = items.get(index);

//        insertFavoriteMedia(item.id, 1);

        String path;
        try {
            path = Images.getLocalPath(item.url);
        } catch (Throwable e) {
            return;
        }

        String filename = new File(path).getName();
        File newFile = new File(imageRoot.getAbsolutePath() + "/" + filename);

        imageRoot.mkdirs();
        try {
            copy(new File(path), newFile);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(newFile)));


        Toast.makeText(this, R.string.media_saved, Toast.LENGTH_SHORT).show();
    }

    public void copy(File src, File dst) throws IOException {
        FileInputStream inStream = new FileInputStream(src);
        FileOutputStream outStream = new FileOutputStream(dst);
        FileChannel inChannel = inStream.getChannel();
        FileChannel outChannel = outStream.getChannel();
        inChannel.transferTo(0, inChannel.size(), outChannel);
        inStream.close();
        outStream.close();
    }

    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }



    public class FullImagePagerAdapter extends FragmentStatePagerAdapter {
        public FullImagePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            if (mode == SHOW_ITEMS_MODE) {
                return FullImageFragment.newInstance(items.get(i));
            }
            else if (mode == SHOW_FILES_MODE) {
                return FullImageFragment.newInstance(files.get(i));
            }
            else {
                return null;
            }
        }

        @Override
        public int getCount() {
            if (mode == SHOW_ITEMS_MODE) {
                return items.size();
            }
            else if (mode == SHOW_FILES_MODE) {
                return files.size();
            }
            else {
                return 0;
            }
        }

    }

    private void insertFavoriteMedia(int mediaId, int state) {

        Table<MediaFavorite> media_favoritesTable = DbHelper.getInstance(this).getTableFor(MediaFavorite.class);

        MediaFavorite fav = new MediaFavorite();
        fav.id = mediaId;
        fav.mediaid = mediaId;
        fav.favorite = state;
        media_favoritesTable.insert(fav);
    }
}
