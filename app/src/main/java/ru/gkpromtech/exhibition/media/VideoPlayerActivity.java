package ru.gkpromtech.exhibition.media;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.android.gms.analytics.GoogleAnalytics;

import ru.gkpromtech.exhibition.R;
import ru.gkpromtech.exhibition.model.Media;
import ru.gkpromtech.exhibition.model.Online;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;
import ru.gkpromtech.exhibition.utils.ImageLoader;

public class VideoPlayerActivity extends ActionBarActivity implements MediaPlayer.OnCompletionListener {
    private Online channel;
    private Media media;

    private MediaController mMediaController;
    private String playingURL;

    static final String SAVE_PLAY_ONLINE_STATE = "play_online";
    static final String SAVE_PLAY_VIDEO_STATE = "play_video";
    static final String SAVE_PLAY_VIDEO_CHANNEL = "channelURL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        boolean playOnline = false;
        boolean playVideo = false;
        if (savedInstanceState != null) {
            playOnline = savedInstanceState.getBoolean(SAVE_PLAY_ONLINE_STATE, false);
            playVideo = savedInstanceState.getBoolean(SAVE_PLAY_VIDEO_STATE, false);
            playingURL = savedInstanceState.getString(SAVE_PLAY_VIDEO_CHANNEL, "");
        }

        setContentView(R.layout.layout_media_videoplayer);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        mMediaController = new MediaController(this);
        mMediaController.setMediaPlayer(videoView);
        mMediaController.setAnchorView(videoView);
        videoView.setMediaController(mMediaController);
        videoView.setOnCompletionListener(this);
        videoView.setVisibility(View.INVISIBLE);

        if (getIntent().getExtras() != null) {

            channel = (Online) getIntent().getExtras().getSerializable("channel");
            media = (Media) getIntent().getExtras().getSerializable("media");
            if (channel != null) {

                if (actionBar != null) {
                    actionBar.setTitle(R.string.media_title_online);
                }

                TextView channelText = (TextView) findViewById(R.id.textChannel);
                channelText.setText(channel.name);
                ImageView imagePreview = (ImageView) findViewById(R.id.imagePreview);

                if (channel.preview != null) {
                    ImageLoader.load(channel.preview, imagePreview);
                }

                if (channel.description != null) {
                    TextView channelDesc = (TextView) findViewById(R.id.textChannelDescription);
                    channelDesc.setText(channel.description);
                }

                playingURL = channel.url1;
                if (!playingURL.contains("http://"))
                    playingURL = channel.url2;

                videoView.setVideoURI(Uri.parse(playingURL));
                mMediaController.setEnabled(true);

                imagePreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlayVideo(videoView);
                    }
                });
            } else if (media != null) {
                if (actionBar != null) {
                    actionBar.setTitle(R.string.media_title_video);
                }

                TextView channelText = (TextView) findViewById(R.id.textChannel);
                channelText.setText(media.name);
                ImageView imagePreview = (ImageView) findViewById(R.id.imagePreview);

                if (media.preview != null) {
                    ImageLoader.load(media.preview, imagePreview);
                }

//            if (media.description != null) {
//                TextView channelDesc =(TextView)findViewById(R.id.textChannelDescription);
//                channelDesc.setText(channel.description);
//            }

                playingURL = media.url;

                videoView.setVideoURI(Uri.parse(playingURL));
                mMediaController.setEnabled(true);

                imagePreview.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlayVideo(videoView);
                    }
                });
            }
        }

        if (playOnline || playVideo) {
            startPlayVideo(videoView);
        }

        AnalyticsManager.sendEvent(this, R.string.media_category, R.string.action_video);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(menuItem);
    }


    @Override
    protected void onStart() {
        super.onStart();
        GoogleAnalytics.getInstance(this).reportActivityStart(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        mMediaController.hide();
        GoogleAnalytics.getInstance(this).reportActivityStop(this);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        final VideoView videoView = (VideoView) findViewById(R.id.videoView);
        savedInstanceState.putBoolean(SAVE_PLAY_ONLINE_STATE, videoView.isPlaying());
        savedInstanceState.putString(SAVE_PLAY_VIDEO_CHANNEL, playingURL);

        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onCompletion(MediaPlayer v) {
        finish();
    }

    private void startPlayVideo(VideoView videoView) {
        setPlaceholderVisible(false);

        videoView.setLayoutParams(
                new RelativeLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
        videoView.setVisibility(View.VISIBLE);
        videoView.start();
    }

    //Convenience method to show a video
    public static void showOnlineVideo(Context ctx, Online channel) {
        Intent i = new Intent(ctx, VideoPlayerActivity.class);

        i.putExtra("channel", channel);
        ctx.startActivity(i);
    }

    public static void showVideo(Context ctx, Media media) {
        if (media.url.contains("youtu")) {
            ctx.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(media.url)));
        }
        else {
            Intent i = new Intent(ctx, VideoPlayerActivity.class);

            i.putExtra("media", media);
            ctx.startActivity(i);
        }
    }

    protected void setPlaceholderVisible(boolean visible) {
        ImageView imagePreview = (ImageView)findViewById(R.id.imagePreview);
        ImageView imageButtonPreview = (ImageView)findViewById(R.id.imagePlayButton);

        if (visible) {
            imagePreview.setVisibility(View.VISIBLE);
            imageButtonPreview.setVisibility(View.VISIBLE);
            imagePreview.setLayoutParams(
                    new RelativeLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT));
        }
        else {
            imagePreview.setVisibility(View.INVISIBLE);
            imageButtonPreview.setVisibility(View.INVISIBLE);
            imagePreview.setLayoutParams(
                    new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0));
        }

    }
}