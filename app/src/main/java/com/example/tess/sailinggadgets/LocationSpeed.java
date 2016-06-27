package com.example.tess.sailinggadgets;

/**
 * Created by Tess on 2016-05-17.
 */
import android.annotation.SuppressLint;
import android.location.Location;

@SuppressLint("ParcelCreator")
public class LocationSpeed extends Location {

    private String measurement;



    public LocationSpeed(Location location, String measurement) {
        super(location);
        this.measurement = measurement;
    }


    public String getUseMetricUnits() {
        return this.measurement;
    }

    public void setUseMetricunits(String converterBool) {
        this.measurement = converterBool;
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);
       /* if(!this.getUseMetricUnits())
        {
            //Convert meters to feet
            nDistance = nDistance * 3.28083989501312f;
        }*/
        return nDistance;
    }

    public String getStringLatitude(){
        String  strLatitude;
        strLatitude = Location.convert(super.getLatitude(), Location.FORMAT_SECONDS);

        if(strLatitude.contains("-")){
            strLatitude += " S";
        }else{
            strLatitude += " N";
        }

        return strLatitude;
    }

    public String getStringLongitude(){
        String  strLongitude;
        strLongitude = Location.convert(super.getLongitude(), Location.FORMAT_SECONDS);

        if(strLongitude.contains("-")){
            strLongitude += " W";
        }else{
            strLongitude += " E";
        }

        return strLongitude;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();
      /*  if(!this.getUseMetricUnits())
        {
            //Convert meters to feet
            nAccuracy = nAccuracy * 3.28083989501312f;
        }*/
        return nAccuracy;
    }

    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();
       /* if(!this.getUseMetricUnits())
        {
            //Convert meters to feet
            nAltitude = nAltitude * 3.28083989501312d;
        }*/
        return nAltitude;
    }

    @Override //Current method of getting the speed of the cellphone
    public float getSpeed() {
        float nSpeed = super.getSpeed();
        String measurement2 = DashboardActivity.getMeasurement2();

        if (measurement2.equals("Knots/Hour")){
            nSpeed = super.getSpeed() * 1.94384f;
        } else if(measurement2.equals("Kilometers/Hour")){
            nSpeed = super.getSpeed() * 3.6f;
        }
        return nSpeed;
    }



}
