package com.example.tess.sailinggadgets;


import java.util.Locale;
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
 * Created by Tess on 2016-05-19.
 */
public class OpenWeatherFragment extends Fragment {

    TextView windField;
    TextView cityField;

    Handler handler;

    public OpenWeatherFragment(){
        handler = new Handler();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        View rootView = inflater.inflate(R.layout.fragment_openwe, container, false);
        cityField = (TextView)rootView.findViewById(R.id.txtCurrentLocation);
        windField = (TextView)rootView.findViewById(R.id.txtCurrentWind);

        //  weatherIcon.setTypeface(weatherFont);
        return rootView;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        //if(savedInstanceState != null){
         //   String longitude = getArguments().getString("long");
          //  String latitude = getArguments().getString("lat");

        updateWeatherData();
         //   Toast.makeText(getActivity(), "long: " + longitude + "lat" + latitude, Toast.LENGTH_SHORT)
           //         .show();
      //  }
    }

    private void updateWeatherData(){


        final String longitude2 = "18.0686";
        final String latitude2 = "59.3293";


        new Thread(){
            public void run(){
                final JSONObject json = RemoteFetch.getJSON(getActivity(), "stockholm", "46");
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
            cityField.setText(json.getString("name").toUpperCase(Locale.US) +
                    ", " +
                    json.getJSONObject("sys").getString("country"));

            JSONObject details = json.getJSONArray("weather").getJSONObject(0);
            JSONObject main = json.getJSONObject("wind");
            windField.setText(
                    details.getString("description").toUpperCase(Locale.US) +
                            "\n" + "Current wind: " + main.getString("speed") + "\n"
                            + "Current direction" +  main.getString("deg" ));


        }catch(Exception e){
            Log.e("SimpleWeather", "One or more fields not found in the JSON data");
        }
    }


}
