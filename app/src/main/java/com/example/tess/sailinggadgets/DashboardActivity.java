package com.example.tess.sailinggadgets;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.util.Formatter;
import java.util.Locale;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    //For the compass needle
    private ImageView compassNeedle;//The image of the compass needle
    private float currentDegree = 0f;//The current degrees
    TextView currentHeading;//TextView to ddisplay the current heading in text
    SensorManager sm;//For accessing the devices sensor

    ///GPS permission request code
    private static final int MY_PERMISSIONS_REQUEST_GPS = 123;
    private String measurement;
    private Spinner spinner;

    private LocationManager lm;//LocationManager is used to get the location
    TextView longV, latV, latV2; //Textviews to display the current long- and latitude

    //Weather data variables
    private static boolean mHasItRun = false;//For updating the Wind conditions
    private static String windString = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        //Compass
        compassNeedle = (ImageView) findViewById(R.id.imgCompass);//Ini the compass needles image.
        currentHeading = (TextView) findViewById(R.id.tvHeading); // TextView that will tell the user what degree is he heading.
        sm = (SensorManager) getSystemService(SENSOR_SERVICE);// initialize your android device sensor capabilities.

        //Initializing the GPS variables
        longV = (TextView) findViewById(R.id.long_view);
        latV = (TextView) findViewById(R.id.lat_view);
        latV2 = (TextView) findViewById(R.id.lat_direction);


        updateLocation();//Calls the method for updating the location.

        spinner = (Spinner) findViewById(R.id.To_Units);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {//When the user selects another item on the spinner
                measurement = spinner.getSelectedItem().toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                measurement = "m/s";
            }//When nothing is selected on the spinner

        });

        if (!mHasItRun) {
            Toast.makeText(getBaseContext(), "Got the wind",
                    Toast.LENGTH_SHORT).show();
            updateWeather();
            mHasItRun = true;
        }else{
            TextView windField = (TextView) findViewById(R.id.txtCurrentWind);
            windField.setText(windString);
        }


    }

    //Update location needs to first check to see if it has permission to use FINE_LOCATION.
    public void updateLocation() {
        if (ContextCompat.checkSelfPermission(DashboardActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) { //If the correct permission is not found then it will ask the user for it.

            //Requesting permission
            ActivityCompat.requestPermissions(DashboardActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_GPS);

        } else { //If the correct permission was found then go straight ahead to start up the location manager
            lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 1, this);
            this.updateSpeed(null);
        }
    }

    //Getting the GPS data
    @Override
    public void onLocationChanged(Location location) {
        CLocation myLocation = new CLocation(location, measurement);
        this.updateSpeed(myLocation);
        this.updateCoords(myLocation);
    }

    //Method called if the provider is turned off
    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Gps is turned off.",
                Toast.LENGTH_SHORT).show();
    }

    //Method called if the provider is turned back on
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "Gps is turned on. ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Not used
    }

    //Method called if the app is resumed
    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        try {//Testar ifall detta fungerar
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 1, this);
        this.updateSpeed(null);
            Toast.makeText(getBaseContext(), "Location updates re engaged. ",
                    Toast.LENGTH_SHORT).show();
       }catch(SecurityException se){
            Toast.makeText(getBaseContext(), "Location updates failed to re engage. ",
                    Toast.LENGTH_SHORT).show();
            Log.e("This app", "exception", se);
    }
    }

    //Method called if the app is paused
    @Override
    protected void onPause() {
        super.onPause();
        // to stop the listener and save battery
        sm.unregisterListener(this);
        try {//Testar ifall detta fungerar
            lm.removeUpdates(this);
        }catch(SecurityException se){
            Log.e("This app", "exception", se);
        }
    }

    //Method for the compass needle. When the sensor senses a change this method will trigger
    @Override
    public void onSensorChanged(SensorEvent event) {

        // get the angle around the z-axis rotated
        float degree = Math.round(event.values[0]);

        currentHeading.setText("" + Float.toString(degree));

        // create a rotation animation (reverse turn degree degrees)
        RotateAnimation ra = new RotateAnimation(
                currentDegree,
                -degree,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF,
                0.5f);

        // how long the animation will take place
        ra.setDuration(210);

        // set the animation after the end of the reservation status
        ra.setFillAfter(true);

        // Start the animation
        compassNeedle.startAnimation(ra);
        currentDegree = -degree;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Not used right now
    }



    private void updateCoords(CLocation location) {
        String strLongitude, strLatitude;

        if (location != null) {
            strLongitude = location.getStringLongitude();
            strLatitude = location.getStringLatitude();

            longV.setText(strLongitude);
            latV.setText(strLatitude);
        }
    }

    private void updateWeather(){
        //This starts the weather service.
            OpenWeatherFragment fm = new OpenWeatherFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new OpenWeatherFragment())
                    .commit();
    }


    private void updateSpeed(CLocation location) {
        float nCurrentSpeed = 0;

        if(location != null) {
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strCurrentSpeed + " ");
    }

    public static void setWindString(String windStringData) {
    windString = windStringData;
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

    //PERMISSIONS
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            //Case about the GPS permission
            case MY_PERMISSIONS_REQUEST_GPS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                    try {
                        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                                0, 1, this);
                        this.updateSpeed(null);
                    } catch (SecurityException se) {
                        Toast.makeText(DashboardActivity.this, "GPS was denied somehow. This app wont work fully without it", Toast.LENGTH_SHORT)
                                .show();
                    }

                } else {
                    //Permission denied - tell the user
                    Toast.makeText(DashboardActivity.this, "GPS was denied. This app wont work fully without it", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
        }
    }

    public void finish() {
        super.finish();
        System.exit(0);
    }

}