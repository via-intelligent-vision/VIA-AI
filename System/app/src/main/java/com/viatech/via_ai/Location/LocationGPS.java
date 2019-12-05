/* /////////////////////////////////////////////////////////////////////////////////////////////////
//
//  IMPORTANT: READ BEFORE DOWNLOADING, COPYING, INSTALLING OR USING.
//
//  By downloading, copying, installing or using the software you agree to this license.
//  If you do not agree to this license, do not download, install,
//  copy or use the software.
//
//                                 MIT License
//                            Copyright (c) 2019 VIA, Inc.
//
// Permission is hereby granted, free of charge, to any person obtaining a copy of this software
// and associated documentation files (the "Software"), to deal in the Software without restriction,
// including without limitation the rights to use, copy, modify, merge, publish, distribute,
// sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
// furnished to do so, subject to the following conditions:
//
// The above copyright notice and this permission notice shall be included in all copies or
// substantial portions of the Software.
//
// THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
// NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
// NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
// DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
//
//
// ////////////////////////////////////////////////////////////////////////////////////////////// */

package com.viatech.via_ai.Location;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

public class LocationGPS {
    public interface OnLocationChangedListener {
        void onLocationChanged(Location location);
    }

    final static String TAG = "LocationGPS";
    private Context mContext = null;
    ;
    private LocationManager locationManager = null;
    private LocationListener locationListener_GPS = null;
    private LocationListener locationListener_Net = null;
    private Location bestLocation = null;
    private Object locationLock = new Object();
    private OnLocationChangedListener mListener = null;
    private boolean bIsStart = false;

    public LocationGPS(Context context) {
        mContext = context;

    }

    public void init() {
        locationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                // Define a listener that responds to location updates
                locationListener_GPS = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "GPS : locationListener_GPS  " + location.getLatitude() + " , " + location.getLongitude());
                        synchronized (locationLock) {
                            if (isBetterLocation(location, bestLocation)) {
                                bestLocation = location;
                                if (mListener != null) mListener.onLocationChanged(getLocation());
                            }
                        }
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };
            }

            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationListener_Net = new LocationListener() {
                    public void onLocationChanged(Location location) {
                        Log.d(TAG, "GPS : locationListener_Net " + location.getLatitude() + " , " + location.getLongitude());

                        synchronized (locationLock) {
                            if (isBetterLocation(location, bestLocation)) {
                                bestLocation = location;
                                if (mListener != null) mListener.onLocationChanged(getLocation());
                            }
                        }
                    }

                    public void onStatusChanged(String provider, int status, Bundle extras) {
                    }

                    public void onProviderEnabled(String provider) {
                    }

                    public void onProviderDisabled(String provider) {
                    }
                };
            }

            if (locationListener_GPS == null || locationListener_Net == null) {
                Log.e(TAG, "GPS Error, no support provider.");
            }
        }
    }

    public void start() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    throw new IllegalAccessError("No location permission granted");
                }
                else {
                    if (locationListener_GPS != null) {

                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 0, locationListener_GPS);
                        bIsStart |= true;
                    }

                    if (locationListener_Net != null) {
                        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000, 0, locationListener_Net);
                        bIsStart |= true;
                    }
                }
            }
        });
    }


    public void stop() {
        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(locationListener_GPS != null) {
                    locationManager.removeUpdates(locationListener_GPS);
                }

                if(locationListener_Net != null) {
                    locationManager.removeUpdates(locationListener_GPS);
                }
                bIsStart = false;
            }
        });
    }

    public Location getLocation() {
        return bestLocation;
    }

    public void setOnLocationChangedListener(OnLocationChangedListener listener) {
        mListener = listener;
    }

    public boolean isStart() {
        return bIsStart;
    }

    /** Determines whether one Location reading is better than the current Location fix
     * @param location  The new Location that you want to evaluate
     * @param currentBestLocation  The current Location fix, to which you want to compare the new one
     */
    private static final int TWO_MINUTES = 1000 * 60 * 2;

    protected boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
        boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }


    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }

}
