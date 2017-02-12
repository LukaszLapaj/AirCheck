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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


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

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    downloadParsePrintTable(Double.valueOf(location.getLatitude()), Double.valueOf(location.getLongitude()));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                // printResult(String.valueOf(location.getLatitude()), String.valueOf(location.getLongitude()));
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 10);
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
        buildGoogleApiClient();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        Log.d("Response: ", "> GPS Check" );
        //noinspection ResourceType
        locationManager.requestLocationUpdates("gps", time, distance, listener);
    }


    void downloadParsePrintTable(Double Latitude, Double Longitude) throws JSONException{
        // My apikey
        // String apikey = "0d23d883ef6a4689b938fa0dbf21e8f3";
        // Airly apikey
        // String apikey = "fae55480ef384880871f8b40e77bbef9";

        String result = JsonTask("https://airapi.airly.eu/v1/sensors/current?southwestLat=50.018630455297846&southwestLong=19.605224822998025&northeastLat=50.19786185757222&northeastLong=20.040214752197244&apikey=fae55480ef384880871f8b40e77bbef9");
        // t.setText(result);
        int id = 0;
        int index = 0;
        double distanceTo = Double.MAX_VALUE;

        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            JSONObject location = jsonobject.getJSONObject("location");
            String latitude = location.getString("latitude");
            String longitude = location.getString("longitude");
            String sensorId = jsonobject.getString("id");
            // String ln = name.getString("longitude");
            //String url = jsonobject.getString("longitude");
            // t.append(latitude + " " + longitude + "\n");
            // t.append(distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude), 0, 0) + "\n");
            if(distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude), 0, 0) <= distanceTo){
                id = Integer.parseInt(sensorId);
                index =  i;
                distanceTo = distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude), 0, 0);
                // t.append(latitude + " " + longitude + "\n" /* + distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude)) + "\n" */);
            }
        }
        JSONObject jsonobject = jsonarray.getJSONObject(index);
        JSONObject location = jsonobject.getJSONObject("location");
        String latitude = location.getString("latitude");
        String longitude = location.getString("longitude");
        String vendor = jsonobject.getString("vendor");
        t.append(latitude + " " + longitude + "\n" + distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude), 0, 0) + "\n");
        printResult(latitude, longitude);

        /*
        JSONObject jsonResponse = new JSONObject(result);
        JSONArray cast = jsonResponse.getJSONArray("abridged_cast");
        for (int i=0; i<cast.length(); i++) {
            JSONObject sensor = cast.getJSONObject(i);
            String latitude = sensor.getString("latitude");
            String longitude = sensor.getString("longitude");
            String sensorId = sensor.getString("id");
            if(distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude)) <= distance){
                id = Integer.parseInt(sensorId);
            }
            //allNames.add(name);
        }*/
        /*try {
            //JSON is the JSON code above

            JSONObject jsonResponse = new JSONObject(result);
            JSONArray latitude = jsonResponse.getJSONArray("latitude");
            JSONArray longitude = jsonResponse.getJSONArray("longitude");
            String latitude = latitude.toString();
            String longitude = longitude.toString();



        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/
        // t.setText(id);
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
            //My apikey
            // String apikey = "0d23d883ef6a4689b938fa0dbf21e8f3";
            //Airly apikey
            String apikey = "fae55480ef384880871f8b40e77bbef9";
            String result = JsonTask("https://airapi.airly.eu/v1/mapPoint/measurements?latitude=" + Latitude + "&longitude=" + Longitude + "&apikey=" + apikey);
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
            t.append("\n" + "PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n ");
            // t.setText(" PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n " + "Pressure: " + pressure + "\n " + "Humidity: " + humidity + "%" + "\n " + "Temperature: " + temperature + "°C" + "\n ");
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
    public void onConnected(Bundle arg0){
        //noinspection ResourceType
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("Response: ", "> Getting data from last location" );
            try {
                downloadParsePrintTable(Double.valueOf(mLastLocation.getLatitude()), Double.valueOf(mLastLocation.getLongitude()));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // printResult(String.valueOf(mLastLocation.getLatitude()), String.valueOf(mLastLocation.getLongitude()));
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {}

    public static double distance(double lat1, double lat2, double lon1, double lon2, double el1, double el2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        double height = el1 - el2;

        distance = Math.pow(distance, 2) + Math.pow(height, 2);

        return Math.sqrt(distance);
    }
}