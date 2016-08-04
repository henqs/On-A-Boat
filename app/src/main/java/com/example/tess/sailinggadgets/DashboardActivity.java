package com.example.tess.sailinggadgets;

/**
 * Created by Henrik Svensson on 2016-05-10.
 */

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
import android.media.Image;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.text.TextWatcher;
import android.view.animation.RotateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
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
    private static String stringMeasurement = "M/S";
    private Spinner spinner;

    //To get GPS data
    private LocationManager lm;//LocationManager is used to get the location
    TextView longV, latV, latV2; //Textviews to display the current long- and latitude


    //Weather data variables
    private static boolean hasWeatherUpdaterRun = false;//For updating the Wind conditions
    private static String windString = "";
    private TextInputLayout inputLayoutCity, inputLayoutCountry;
    private EditText inputCity, inputCountry;
    private Button btnRetrieve;
    private ImageView upArrow, downArrow;

    //Layout variables
    private LinearLayout map_layout, layout_weather_default, layout_weather_shown;

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

        /**
        //Form for the weather API
         **/
        inputLayoutCity = (TextInputLayout) findViewById(R.id.input_layout_city);
        inputLayoutCountry = (TextInputLayout) findViewById(R.id.input_layout_country_code);
        inputCity = (EditText) findViewById(R.id.input_city);
        inputCountry = (EditText) findViewById(R.id.input_country) ;
        btnRetrieve = (Button) findViewById(R.id.btn_getWeather);
        layout_weather_default = (LinearLayout) findViewById(R.id.layout_weather_default);
        layout_weather_shown = (LinearLayout) findViewById(R.id.layout_weather_shown);
        upArrow = (ImageView) findViewById(R.id.up_arrow);
        downArrow = (ImageView) findViewById(R.id.down_arrow);
        inputCity.addTextChangedListener(new MyTextWatcher(inputCity));
        inputCountry.addTextChangedListener(new MyTextWatcher(inputCountry));


        /**
         * OnClickListener for the retrieve weather button
         */
        btnRetrieve.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                submitForm();
                layout_weather_shown.setVisibility(View.GONE);
                upArrow.setVisibility(View.GONE);
                downArrow.setVisibility(View.VISIBLE);
            }
        });

        /**
         * OnClickListener for the layout that hold the form for the OpenWeather API
         * Handles actions taken when the user clicks the layout
         */
        layout_weather_default.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(layout_weather_shown.getVisibility()==View.GONE){ //To show the extended layout
                    layout_weather_shown.setVisibility(View.VISIBLE);
                    upArrow.setVisibility(View.VISIBLE);
                    downArrow.setVisibility(View.GONE);

                } else if(layout_weather_shown.getVisibility()==View.VISIBLE){//To hide the extended layout
                    layout_weather_shown.setVisibility(View.GONE);
                    upArrow.setVisibility(View.GONE);
                    downArrow.setVisibility(View.VISIBLE);
                }
            }
        });

        updateLocation();//Calls the method for updating the location.


        spinner = (Spinner) findViewById(R.id.To_Units);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {//When the user selects another item on the spinner
                String measurement = spinner.getSelectedItem().toString();
                String spinnerSelect = measurement;

                if (spinnerSelect == "Meters/Seconds"){
                    stringMeasurement = "M/S";
                } else if(spinnerSelect == "Knots/Hour"){
                    stringMeasurement = "Knt/H";
                }else if(spinnerSelect == "Kilometers/Hour"){
                    stringMeasurement = "Km/H";
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parentView) {
                stringMeasurement = "M/S";
            }//When nothing is selected on the spinner
        });
    }

    public static String getStringMeasurement(){
        return stringMeasurement;
    }

    /**
     * Update location needs to first check to see if it has permission to use FINE_LOCATION.
     */
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

    /**
    *   This method is triggered when the device senses that the location has changed.
    *   This method will be getting the GPS data on where this device is located.
    *   With this information it will call the update speed and coordinates.
     */
    @Override
    public void onLocationChanged(Location location) {
        LocationSpeed myLocation = new LocationSpeed(location);
        this.updateSpeed(myLocation);
        this.updateCoordinates(myLocation);
        showMapLayout();
    }

    /**
     *  Called if the provider is turned off
     */
    @Override
    public void onProviderDisabled(String provider) {
        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(intent);
        Toast.makeText(getBaseContext(), "Most functions of this app requires the use of the devices GPS.",
                Toast.LENGTH_SHORT).show();
    }

    /**
     * Called if the provider is turned back on
     */
    @Override
    public void onProviderEnabled(String provider) {
        Toast.makeText(getBaseContext(), "Gps is turned on. ",
                Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        //Not used
    }

    /**
     * Called if the app is resumed.
     * Functions that were disabled if the app is paused needs to be reengaged.
     */
    @Override
    protected void onResume() {
        super.onResume();
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
                SensorManager.SENSOR_DELAY_GAME);  // for the system's orientation sensor registered listeners

        windString = "";

        try {
            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    0, 1, this);
            this.updateSpeed(null);
        } catch (SecurityException se) {
            Toast.makeText(getBaseContext(), "Location updates failed to restart. ",
                    Toast.LENGTH_SHORT).show();
            Log.e("This app", "exception", se);
        }
    }

    /**
     *  Called if the app is paused
     *  i.e if the user starts another app without fully closing this app
     */
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

    /**
     *  Method for the compass needle. When the sensor senses a change this method will trigger.
     *  This method spins the compass needle to match the bearing of the device.
     */
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

    /**
     *  Method used for updating the coordinates layout.
     *  The actual coordinates will be formatted with DEGREES
     *  Also updates coordinates for the map
     */
    private void updateCoordinates(LocationSpeed location) {
        String strLongitude, strLatitude;

     //The map needs a format of its own.
        if (location != null) {
            strLongitude = location.getStringLongitude();
            strLatitude = location.getStringLatitude();

            mMap.clear();//Clears the previous marker

            longV.setText(strLongitude);
            latV.setText(strLatitude);

            LatLng currentPlace = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.addMarker(new MarkerOptions()
                    .position(currentPlace)
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_medium)));
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom((currentPlace), 11.0F));
        }
    }

    /**
     * Method used to control the map layout
     */
    public void showMapLayout(){
        map_layout = (LinearLayout) findViewById(R.id.map_layout);
        map_layout.setVisibility(View.VISIBLE);
    }

    /**
     *  Method for getting the wind and wind strength from the OpenWeather API
     */
    private void updateWeather(String longitude, String latitude){

        Bundle bundle = new Bundle(); //Creates a bundle to send information about the location
        bundle.putString("Long", longitude);   //parameters are (key, value).
        bundle.putString("Lat", latitude);   //parameters are (key, value).
        OpenWeatherFragment fm = new OpenWeatherFragment();
        fm.setArguments(bundle);

        getSupportFragmentManager().beginTransaction().replace(R.id.container, fm)
                    .commit();
    }

    /**
     *
     *  Method for updating the speed.
     *  Asks the class LocationSpeed for the current speed of the device.
     */
    private void updateSpeed(LocationSpeed location) {
        float nCurrentSpeed = 0;

        if(location != null) { //As long as the device has a location
            nCurrentSpeed = location.getSpeed();
        }
        TextView txtCurrentSpeed = (TextView) this.findViewById(R.id.txtCurrentSpeed);

        String currentSpeed = nCurrentSpeed + stringMeasurement;
        if(txtCurrentSpeed != null){
            txtCurrentSpeed.setText(currentSpeed);
        }
    }


    /**
     * PERMISSIONS
     *
     * This method checks the permissions that needs to be granted
     */

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

    /**
     * When the map is ready this method will be called
     * Sets the options chosen for the map.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setScrollGesturesEnabled(false);//Cant scroll around the map
        mMap.getUiSettings().setZoomControlsEnabled(true);//Sets an option to zoom in and out
    }

    public static void setWindString(String windStringData) {
        windString = windStringData;

    }

    public static String getWindString(){
        return windString;
    }


    /**
     ***
     ****
     ***** This section contains methods and a class for validating input into the weather API
     ****
     ***
     **/

    /**
     * Validating form
     * Making sure the input is correct
     */
    private void submitForm() {
        if (!validateCity()) {
            return;
        }

        if (!validateCountry()) {
            return;
        }

        String city = inputCity.getText().toString();
        String country = inputCountry.getText().toString();

        updateWeather(city, country);
    }

    /**
     * Method for focusing on view
     */
    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    /**
     * Validating the city input
     */
    private boolean validateCity() {
        if (inputCity.getText().toString().trim().isEmpty()) {
            inputLayoutCity.setError(getString(R.string.err_msg_city));
            requestFocus(inputCity);
            return false;
        } else {
            inputLayoutCity.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Validating the country input
     */
    private boolean validateCountry() {
        String email = inputCountry.getText().toString().trim();

        if (email.isEmpty() || (email.length() > 2))  {
            inputLayoutCountry.setError(getString(R.string.err_msg_country));
            requestFocus(inputCountry);
            return false;
        } else {
            inputLayoutCountry.setErrorEnabled(false);
        }

        return true;
    }

    /**
     * Class for handling the input
     */
    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.input_country:
                    validateCountry();
                    break;
                case R.id.input_city:
                    validateCity();
                    break;
            }
        }
    }

}



