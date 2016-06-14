package com.example.tess.sailinggadgets;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

public class StartupActivity extends AppCompatActivity {


    private Handler mHandler = new Handler();//Handler is used to allow the SplashScreen to be visible for a while.
    private static final int MY_PERMISSIONS_REQUEST_GPS = 123; //Permission request code.

    private Button mainActivity_button, chartActivity_button;

    private Button noPermission_button;//Setting up the button for granting permissions.
    private LinearLayout permissionLayout;//The layout for the permission textview and button.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_startup);



        mainActivity_button = (Button) findViewById(R.id.button_mainActivity);
        mainActivity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartupActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        chartActivity_button = (Button) findViewById(R.id.button_Stats);
        chartActivity_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartupActivity.this, ChartActivity.class);
                startActivity(intent);
            }
        });

        permissionLayout = (LinearLayout) findViewById(R.id.permissionLayout);//Finding the permission Layout.

        //Setting up the onClick listener for the button. This listener will then get the getPermission() method.
        noPermission_button = (Button) findViewById(R.id.noPermission_button);
        noPermission_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(StartupActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        });

        //Checking to see if permission is already granted since before
        if (ContextCompat.checkSelfPermission(StartupActivity.this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            permissionNeeded();

        } else {//If permission was already given the nextActivity() method will be called.

         //   nextActivity();
        }
    }
/*
    //This method is to make sure the splashscreen is shown for a couple of seconds.
    public void nextActivity() {
        mHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(StartupActivity.this, DashboardActivity.class);
                startActivity(intent);
            }
        }, 4000); // 4 seconds
    }
*/

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }

     /////////Permission methods will be below here/////////
    //Showing the permission Layout
    public void permissionNeeded() {
        permissionLayout.setVisibility(View.VISIBLE);
    }

    //Getting the permission needed
    public void getPermission() {
        ActivityCompat.requestPermissions(StartupActivity.this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                MY_PERMISSIONS_REQUEST_GPS);
    }

    //onRequestPermissionResult is a method for handling whatever the user answered on the request message.
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {  //Case about the GPS permission
            case MY_PERMISSIONS_REQUEST_GPS: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //Starting up DashboardActivity
                    permissionLayout.setVisibility(View.GONE);


                  //  nextActivity();

                } else {
                    //Permission denied - tell the user
                    Toast.makeText(StartupActivity.this, "GPS was denied. This app wont work fully without it", Toast.LENGTH_SHORT)
                            .show();
                }
                break;
            }
        }
    }
}
