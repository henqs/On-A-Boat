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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Formatter;
import java.util.Locale;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

public class DashboardActivity extends AppCompatActivity implements SensorEventListener, LocationListener, OnMapReadyCallback {

    //For the map API
    private GoogleMap mMap;


    //For the compass needle
    private ImageView compassNeedle;//The image of the compass needle
    private float currentDegree = 0f;//The current degrees
    TextView currentHeading;//TextView to ddisplay the current heading in text
    SensorManager sm;//For accessing the devices sensor

    ///GPS permission request code
    private static final int MY_PERMISSIONS_REQUEST_GPS = 123;
    private static String measurement = "M/S", measurement2 = "M/S";
    private Spinner spinner;

    //To get GPS data
    private LocationManager lm;//LocationManager is used to get the location
    TextView longV, latV, latV2; //Textviews to display the current long- and latitude

    //Weather data variables
    private static boolean mHasItRun = false;//For updating the Wind conditions
    private static String windString = "";

    //Layout variables
    private LinearLayout map_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //Getting the map support fragment
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(this);



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
                String measurement = spinner.getSelectedItem().toString();
                String spinnerSelect = measurement;

                if (spinnerSelect == "Meters/Seconds"){
                    measurement2 = "M/S";
                } else if(spinnerSelect == "Knots/Hour"){
                    measurement2 = "Knt/H";
                }else if(spinnerSelect == "Kilometers/Hour"){
                    measurement2 = "Km/H";
                }


            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                measurement = "m/s";
                measurement2 = "M/S";
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

    public static String getMeasurement2(){
        return measurement2;
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

    //When senses that the location has changed this method will be called.
    // This method will be getting the GPS data on where this device is located.
    //With this information it will call the update speed and update coords methods.
    @Override
    public void onLocationChanged(Location location) {
        LocationSpeed myLocation = new LocationSpeed(location, measurement);
        this.updateSpeed(myLocation);
        this.updateCoordinates(myLocation);
        showMapLayout();
    }

    //Method called if the provider is turned off
    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Most functions of this app requires the use of the devices GPS.",
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

    //Method called if the app is resumed.
    //Functions that were disabled if the app is paused needs to be reengaged.
    @Override
    protected void onResume() {
        super.onResume();
        // for the system's orientation sensor registered listeners
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);

        try {
        lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                0, 1, this);
        this.updateSpeed(null);
            Toast.makeText(getBaseContext(), "Location updates reengaged. ",
                    Toast.LENGTH_SHORT).show();
       }catch(SecurityException se){
            Toast.makeText(getBaseContext(), "Location updates failed to reengage. ",
                    Toast.LENGTH_SHORT).show();
            Log.e("This app", "exception", se);
    }
    }

    //Method called if the app is paused i.e if the user starts another app without fully closing this app.
    @Override
    protected void onPause() {
        super.onPause();
        //Stop the  Sensor listener
        sm.unregisterListener(this);

        try { //Stop the location manager
            lm.removeUpdates(this);
        }catch(SecurityException se){
            Log.e("This app", "exception", se);
        }
    }

    //Method for the compass needle. When the sensor senses a change this method will trigger.
    //This method spins the compass needle to match the bearing of the device.
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

    private void updateCoordinates(LocationSpeed location) {
        String strLongitude, strLatitude;

        if (location != null) {
            strLongitude = location.getStringLongitude();
            strLatitude = location.getStringLatitude();

            longV.setText(strLongitude);
            latV.setText(strLatitude);

            LatLng currentPlace = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions().position(currentPlace));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((currentPlace), 11.0F));

            //({ lat: location.getLatitude(), lng: location.getLongitude() })
        }
    }

    //Method for controlling the layouts
    public void showMapLayout(){
        map_layout = (LinearLayout) findViewById(R.id.map_layout);
        map_layout.setVisibility(View.VISIBLE);

       // before_GPS_layout = (LinearLayout) findViewById(R.id.before_GPS_layout);
       // before_GPS_layout.setVisibility(View.GONE);
    }

    //Method for getting the wind and wind strength
    private void updateWeather(){
           // OpenWeatherFragment fm = new OpenWeatherFragment();
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new OpenWeatherFragment())
                    .commit();
    }

    //Method for updating the speed. Ask the class LocationSpeed for the current speed of the device.
    private void updateSpeed(LocationSpeed location) {
        float nCurrentSpeed = 0;

        if(location != null) { //As long as the device has a location
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder()); //Formatter is used to format the data received.
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.txtCurrentSpeed);
        txtCurrentSpeed.setText(strCurrentSpeed + measurement2);
    }

    public static void setWindString(String windStringData) {
    windString = windStringData;
    }

    //PERMISSIONS
    //This method is for checking permission results.
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
                        Toast.makeText(DashboardActivity.this, "Failed to engage the GPS.", Toast.LENGTH_SHORT)
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

    //When the map is ready this method will be called
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
    }
}