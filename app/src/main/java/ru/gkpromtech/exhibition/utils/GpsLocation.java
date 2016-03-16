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
package ru.gkpromtech.exhibition.utils;

import android.content.Context;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.LocationListener;
import android.location.LocationManager;
import android.util.Log;

public class GpsLocation {

    private final Context mContext;
    private LocationManager mLocationManager;
    private GpsStatus mStatus;
    private int mSatellites = 0;
//    private Handler mHandler = new Handler();

    private GpsStatus.Listener mGpsStatusListener = new GpsStatus.Listener() {

        @Override
        public void onGpsStatusChanged(int event) {

            if (event == GpsStatus.GPS_EVENT_SATELLITE_STATUS 
                    || event == GpsStatus.GPS_EVENT_FIRST_FIX) {

                mStatus = mLocationManager.getGpsStatus(null);
                Iterable<GpsSatellite> satellites = mStatus.getSatellites();

                mSatellites = 0;
                for (GpsSatellite sat : satellites)
                    if (sat.usedInFix())
                        ++mSatellites;

                Log.d("GpsLocation", "Satellites used in view: " + mSatellites);
            }
        }

    };

    public GpsLocation(Context context) {
        mContext = context;
    }

    public boolean start(final int minUpdateTime, final LocationListener locationListener) {
        if (mLocationManager == null) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
                    mLocationManager = (LocationManager)
                            mContext.getSystemService(Context.LOCATION_SERVICE);

                    mStatus = mLocationManager.getGpsStatus(null);

                    try {
                        mLocationManager.requestLocationUpdates(
                                LocationManager.GPS_PROVIDER,
                                minUpdateTime, 0, locationListener);
//                        mLocationManager.requestLocationUpdates(
//                                LocationManager.NETWORK_PROVIDER,
//                                minUpdateTime, 0, mLocationListener);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    mLocationManager.addGpsStatusListener(mGpsStatusListener);
                    mSatellites = 0;
//                }
//            });
        }
        return true;
    }

    public void stop(final LocationListener locationListener) {
        if (mLocationManager != null) {
//            mHandler.post(new Runnable() {
//                @Override
//                public void run() {
                    mLocationManager.removeUpdates(locationListener);
                    mLocationManager.removeGpsStatusListener(mGpsStatusListener);
                    mLocationManager = null;
//                }
//            });
        }
    }
}
