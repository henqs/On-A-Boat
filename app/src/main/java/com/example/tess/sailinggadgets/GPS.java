package com.example.tess.sailinggadgets;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;


/**
 * Created by HenQ on 2016-05-10.
 */
public class GPS  extends AppCompatActivity implements LocationListener  {

        private LocationManager lm;
        TextView longV, latV;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main);

            longV = (TextView) findViewById(R.id.long_view);
            latV = (TextView) findViewById(R.id.lat_view);

            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        }

        @Override
        public void onLocationChanged(Location location) {
            DecimalFormat df = new DecimalFormat("#.##");
            String longStr = " "+ df.format(location.getLatitude());
            String latStr = " "+ df.format(location.getLongitude());

            longV.setText(longStr);
            latV.setText(latStr);
        }

        @Override
        public void onProviderDisabled(String provider) {

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            Toast.makeText(getBaseContext(), "Gps is turned off!! ",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onProviderEnabled(String provider) {

            Toast.makeText(getBaseContext(), "Gps is turned on!! ",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            // TODO Auto-generated method stub

        }

    }


