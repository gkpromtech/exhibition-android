/*
 * Copyright 2016 Promtech. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ru.gkpromtech.exhibition;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import ru.gkpromtech.exhibition.db.DbUpdater;
import ru.gkpromtech.exhibition.events.EventsActivity;
import ru.gkpromtech.exhibition.utils.AnalyticsManager;


public class SplashActivity extends Activity {

    private final static boolean GET_UPDATES = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE)
            setTheme(R.style.LandscapeSplashTheme);

        setContentView(R.layout.activity_splash);

        final ProgressBar progressBar = (ProgressBar) findViewById(R.id.progressBar);
        final Handler handler = new Handler();

        if (!GET_UPDATES) {
            findViewById(R.id.progressText).setVisibility(View.GONE);

            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    progressBar.setProgress(10);
                    Intent intent = new Intent(SplashActivity.this, EventsActivity.class);
                    startActivity(intent);
                    finish();
                }
            }, 2000);
        }
        else {
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
        }

        AnalyticsManager.sendEvent(this, R.string.application_category, R.string.action_open);
    }
}
