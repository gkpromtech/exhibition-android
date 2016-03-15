package ru.gkpromtech.exhibition;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.Toast;

import ru.gkpromtech.exhibition.db.DbUpdater;
import ru.gkpromtech.exhibition.events.EventsActivity;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            setTheme(R.style.LandscapeSplashTheme);

        setContentView(R.layout.activity_splash);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final Handler handler = new Handler();

        new Thread(new Runnable() {
            @Override
            public void run() {
                final long startTime = System.currentTimeMillis();
                // initialize db
                boolean ok = new DbUpdater(SplashActivity.this).updateDb(new DbUpdater.OnProgressListener() {
                    @Override
                    public void onUpdateProgress(final int progress) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setProgress(progress);
                            }
                        });
                    }

                    @Override
                    public void onServerUpdateFailed() {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(SplashActivity.this, R.string.error_updating_data,
                                        Toast.LENGTH_LONG).show();
                                AnalyticsManager.sendEvent(SplashActivity.this, R.string.server_category, R.string.action_faild);
                            }
                        });
                    }
                });

                if (ok == true) {
                    AnalyticsManager.sendEvent(SplashActivity.this, R.string.server_category, R.string.action_ok,
                            new DbUpdater(SplashActivity.this).getInstalledDbRevision());
                }

                long delay = 2000 - (System.currentTimeMillis() - startTime);
                if (delay < 0)
                    delay = 0;

                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        progressBar.setProgress(10);
                        Intent intent = new Intent(SplashActivity.this, EventsActivity.class);
                        startActivity(intent);
                        finish();
                    }
                }, delay);

            }
        }).start();

        AnalyticsManager.sendEvent(this, R.string.application_category, R.string.action_open);
    }
}
