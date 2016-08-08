package com.example.tess.sailinggadgets;

/**
 * Created by Henrik Svensson on 2016-05-17.
 */
import android.annotation.SuppressLint;
import android.location.Location;

@SuppressLint("ParcelCreator")
public class LocationSpeed extends Location {

    String measurement2;

    public LocationSpeed(Location location, String measurement1) {
        super(location);
        this.measurement2 = measurement1;
        setMeasurement(measurement2);
    }

    @Override
    public float distanceTo(Location dest) {
        float nDistance = super.distanceTo(dest);
        return nDistance;
    }

    //Method for adding a indication of whether the latitude coordinate is south or north
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

    //Method for adding a indication of whether the longitude coordinate is west or east
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


    //Method for adding a indication of whether the latitude coordinate is south or north
    public String getStringLatitudeWeather(){
        String  strLatitude;
        strLatitude = Location.convert(super.getLatitude(), Location.FORMAT_SECONDS);

        return strLatitude;
    }

    //Method for adding a indication of whether the longitude coordinate is west or east
    public String getStringLongitudeWeather(){
        String  strLongitude;
        strLongitude = Location.convert(super.getLongitude(), Location.FORMAT_SECONDS);

        return strLongitude;
    }

    @Override
    public float getAccuracy() {
        float nAccuracy = super.getAccuracy();
        return nAccuracy;
    }

    @Override
    public double getAltitude() {
        double nAltitude = super.getAltitude();
        return nAltitude;
    }
    public void setMeasurement(String string){
        measurement2 = string;
    }

    public String getMeasurement(){
        return measurement2;
    }

    @Override //Current method of getting the speed of the device
    public float getSpeed() {
        float nSpeed = super.getSpeed();//Gets the speed in Meters per second

        String measure = getMeasurement();

        if (measure.equals("Knots")){//Knots per hour
           nSpeed = super.getSpeed() * 1.94384f;//From m/s to knots/h
        } else if(measure.equals("Kilometers/Hour")){//Kilometers per hour
           nSpeed = super.getSpeed() * 3.6f;//From m/s to km/h
        } else if(measure.equals("Miles/Hour")){
            nSpeed = super.getSpeed() * 2.236936f;
        }
        return nSpeed;

    }
}
