package com.air.check;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.air.check.station.StationWios;
import com.air.check.station.StationAirly;
import com.air.check.google.GoogleAPI;
import com.air.check.utils.Distance;
import com.air.check.utils.JsonTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private Button b;
    private TextView t1;
    private TextView t2;
    private TextView t3;

    private LocationManager locationManager;
    private LocationListener listener;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = (TextView) findViewById(R.id.textView);
        t2 = (TextView) findViewById(R.id.textView2);
        t3 = (TextView) findViewById(R.id.textView3);
        b = (Button) findViewById(R.id.button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    t1.setText("Update...");
                    new JsonTask().execute("http://188.166.73.207/add/1/" + location.getLatitude() + "/" + location.getLongitude());
                    downloadParsePrintTable(location.getLatitude(), location.getLongitude());
                } catch (JSONException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {}

            @Override
            public void onProviderEnabled(String s) {}

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        checkPermission();
    }

    void checkPermission(){
        // Permission check
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED)
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}, 10);
            return;
        }
        buttonListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 10)
            checkPermission();
    }

    void buttonListener(){
        runServices(60*60,100);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Response: ", "> Button Pressed" );
                runServices(60,10);
            }
        });
    }

    void runServices(long time, float distance){
        // Time convert from milliseconds to seconds
        time *= 1000;
        Log.d("Response: ", "> Last Localisation Check" );
        int statusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(getApplicationContext());
        if (statusCode == ConnectionResult.SUCCESS) {
            buildGoogleApiClient();
            if (mGoogleApiClient != null)
                mGoogleApiClient.connect();
        }else
            Log.d("Response: ", "> No Google Play Services!" );
        if (statusCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
            Log.d("Response: ", "> Google Play Services outdated!" );
        Log.d("Response: ", "> GPS Check" );
        //noinspection ResourceType
        locationManager.requestLocationUpdates("gps", time, distance, listener);
    }

    void downloadParsePrintTable(Double Latitude, Double Longitude) throws JSONException, ExecutionException, InterruptedException {
//        Latitude =  50.05767;
//        Longitude = 19.926189;
        StationAirly Airly = new StationAirly().FindStation(Latitude, Longitude);
        t1.setText(" PM1: " + Airly.pm1 + "\n " + "PM2.5: " + Airly.pm25 + "\n " + "PM10: " + Airly.pm10 + "\n " + "Ciśnienie: " + Airly.pressure + "hPa" + "\n " + "Wilgotność: " + Airly.humidity + "%" + "\n " + "Temperatura: " + Airly.temperature + "°C" + "\n " + "Odleglość: " + Math.round(Airly.distanceTo * 100) / 100);
        StationWios Wios = new StationWios().FindStation(Latitude, Longitude);
        t2.setText("Numer stacji WIOŚ: " + Wios.stationId + "\n" + Wios.name + "\n" + "PM10: " + Wios.pm10 + "\n" + "PM2.5: " + Wios.pm25 + "\n" + "Odleglość: " + Math.round(Wios.distanceTo * 100) / 100);
        t3.setText("Miedzy stacjami: " + Math.round(Distance.calculate(Airly.latitude, Wios.latitude, Airly.longitude, Wios.longitude) * 100) / 100 + "\n" );
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        t3.append("" + timestamp);
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {}

    @Override
    public void onConnected(Bundle arg0){
        // requestLocationUpdates
        //noinspection ResourceType
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("Response: ", "> Getting data from last location" );
            try {
                downloadParsePrintTable(mLastLocation.getLatitude(), mLastLocation.getLongitude());
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
            // printResult(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {}
}