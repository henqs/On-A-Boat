package com.example.tess.sailinggadgets;

import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;

/**
 * Created by Henq on 2016-05-17.
 */
public interface GPSListener  extends LocationListener, GpsStatus.Listener {

        void onLocationChanged(Location location);

        void onProviderDisabled(String provider);

        void onProviderEnabled(String provider);

        void onStatusChanged(String provider, int status, Bundle extras);

        void onGpsStatusChanged(int event);

    }

