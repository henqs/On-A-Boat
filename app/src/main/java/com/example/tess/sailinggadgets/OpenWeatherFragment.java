package com.example.tess.sailinggadgets;

import org.json.JSONObject;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by Henrik Svensson on 2016-05-19.
 */
public class OpenWeatherFragment extends Fragment{

    String longitude, latitude;
    TextView windField, cityField;
    Handler handler;

    public OpenWeatherFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_openwe, container, false);
        cityField = (TextView)rootView.findViewById(R.id.txtCurrentWindSpeed);
        windField = (TextView)rootView.findViewById(R.id.txtCurrentWind);

        return rootView;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        latitude = getArguments().getString("Lat");
        longitude = bundle.getString("Long");

        if(latitude != null && longitude != null){
                updateWeatherData(latitude, longitude);

        }
    }

    private void updateWeatherData(final String latitude, final String longitude){

        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(getActivity(),latitude, longitude);
                if(json == null){
                    handler.post(new Runnable(){
                        public void run(){
                            Toast.makeText(getActivity(),
                                    getActivity().getString(R.string.place_not_found),
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                } else {

                    handler.post(new Runnable(){
                        public void run(){
                            renderWeather(json);
                        }
                    });
                }
            }
        }.start();
    }

    private void renderWeather(JSONObject json){


        try {
          /*
          String cityString, windString;
          cityString = (json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

        */

            //JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("wind");
            windField.setText(main.getString("speed"));


           // cityField.setText(cityString);
            cityField.setText(main.getString("deg" ));
            DashboardActivity.setWindString(main.getString("deg" ));

        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }
}
