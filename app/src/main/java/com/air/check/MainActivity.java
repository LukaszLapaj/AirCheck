package com.air.check;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private Button b;
    private TextView t;

    private LocationManager locationManager;
    private LocationListener listener;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t = (TextView) findViewById(R.id.textView);
        b = (Button) findViewById(R.id.button);

        checkPermission();
        buttonListener();

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                printResult(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
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

        buildGoogleApiClient();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        Log.d("Response: ", "> Initial GPS Check" );
        locationManager.requestLocationUpdates("gps", 1440000, 100, listener);
    }

    void checkPermission(){
        // Permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
            return;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 10) {
            buttonListener();
        }
    }

    void buttonListener(){
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d("Response: ", "> Button Pressed" );
                checkPermission();
                mGoogleApiClient.connect();
                locationManager.requestLocationUpdates("gps", 60000, 10, listener);
                Log.d("Response: ", "> GPS Service Running" );
            }
        });
    }

    String JsonTask(String params) {
        // Dirty hack
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                Log.d("Response: ", "> Establishing Connection" );
                URL url = new URL(params);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");
                    Log.d("Response: ", "> " + line);
                }
                return buffer.toString();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
    }

    void printResult(String Latitude, String Longitude) {
        try{
            String result = JsonTask("https://airapi.airly.eu/v1/mapPoint/measurements?latitude=" + Latitude + "&longitude=" + Longitude + "&apikey=0d23d883ef6a4689b938fa0dbf21e8f3");
            JSONObject obj = new JSONObject(result);

            // Get info
            Log.d("Response: ", "> Parsing data" );
            Double pm1 = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pm1"));
            Double pm10 = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pm10"));
            Double pm25 = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pm25"));
            // Double pressure = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pressure"));
            // Double humidity = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("humidity"));
            // Double temperature = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("temperature"));

            // Calculation
            Log.d("Response: ", "> Rounding" );
            pm1 = Math.round(pm1 * 100.0) / 100.0;
            pm10 = Math.round(pm10 * 100.0) / 100.0;
            pm25 = Math.round(pm25 * 100.0) / 100.0;
            // pressure = (double)(Math.round(pressure / 100));

            // Text update
            Log.d("Response: ", "> Text update" );
            t.setText(" PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n " /* + "Pressure: " + pressure + "\n " + "Humidity: " + humidity + "%" + "\n " + "Temperature: " + temperature + "°C" + "\n "*/);
        }
        catch (Exception e) {e.printStackTrace();
        }
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
    public void onConnected(Bundle arg0) {
        checkPermission();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("Response: ", "> GoogleAPI" );
            printResult(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {}
}