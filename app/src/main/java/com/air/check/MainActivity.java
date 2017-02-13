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
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.wiosParser.*;


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

    class stacja {
        public double latitude;
        public double longitude;
        public int id;

        // constructor
        public stacja(double latitude, double longitude, int id) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.id = id;
        }

        /* // getter
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public int getId() { return id; }
        // setter

        public void getLatitude(double latitude) { this.latitude = latitude; }
        public void getLongitude(double longitude) { this.longitude = longitude; }
        public void getId(int id)  { this.id = id; } */
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
        String apikey = "fae55480ef384880871f8b40e77bbef9";
        String result = JsonTask("https://airapi.airly.eu/v1//sensors/current?southwestLat=0&southwestLong=0&northeastLat=89&northeastLong=180&apikey=" + apikey);
        // t.setText(result);
        int id = 0;
        int index = 0;
        stacja Airly = new stacja(0, 0, 0);
        double distanceTo = Double.MAX_VALUE;

        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            JSONObject location = jsonobject.getJSONObject("location");
            String latitude = location.getString("latitude");
            String longitude = location.getString("longitude");
            int sensorId = jsonobject.getInt("id");
            // String ln = name.getString("longitude");
            //String url = jsonobject.getString("longitude");
            // t.append(latitude + " " + longitude + "\n");
            // t.append(distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude), 0, 0) + "\n");
            if(distance(Latitude, Double.valueOf(latitude), Longitude, Double.valueOf(longitude)) <= distanceTo){
                // id = Integer.parseInt(sensorId);
                index =  i;
                Airly.latitude = Double.valueOf(latitude);
                Airly.longitude = Double.valueOf(longitude);
                Airly.id = sensorId;
                distanceTo = distance(Latitude, Double.valueOf(latitude), Longitude, Double.valueOf(longitude));

                // t.append(latitude + " " + longitude + "\n" /* + distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude)) + "\n" */);
            }
        }
        JSONObject jsonobject = jsonarray.getJSONObject(index);
        JSONObject location = jsonobject.getJSONObject("location");
        String latitude = location.getString("latitude");
        String longitude = location.getString("longitude");
        String vendor = jsonobject.getString("vendor");
        // t.append(latitude + " " + longitude + "\n" + distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude) + "\n"));
        printResult(Double.toString(Airly.latitude), Double.toString(Airly.longitude), "airly", Airly.id);
        //printResult(latitude, longitude, "airly", 0);

        int indexWios = 0;
        double distanceToWios = Double.MAX_VALUE;

        latitude = "0";
        longitude = "0";

        stacja krasinskiego = new stacja(50.05767, 19.926189, 1);
        stacja bulwarowa = new stacja(50.069308, 20.053492, 2);
        stacja bujaka = new stacja(50.010575, 19.949189, 3);
        stacja dietla = new stacja(50.057447, 19.946008, 13);
        stacja piastow = new stacja(50.099361, 20.018317, 14);
        stacja zlotyrog = new stacja(50.081197, 19.895358, 15);
        stacja stacje[] = {krasinskiego, bulwarowa, bujaka, dietla, piastow, zlotyrog};

        for (int i = 0; i < 6; i++) {
            if(distance(Double.valueOf(Latitude), stacje[i].latitude, Double.valueOf(Longitude), stacje[i].longitude) <= distanceToWios){
                // id = Integer.parseInt(sensorId);
                indexWios =  i;
                distanceToWios = distance(Double.valueOf(Latitude), stacje[i].latitude, Double.valueOf(Longitude), stacje[i].longitude);
                // t.append(latitude + " " + longitude + "\n" /* + distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude)) + "\n" */);
            }
        }

        printResult(Double.toString(stacje[indexWios].latitude), Double.toString(stacje[indexWios].longitude), "WIOS", stacje[indexWios].id);

        /* if(distanceToAirly < distanceToWios) {
            JSONObject jsonobject = jsonarray.getJSONObject(indexAirly);
            JSONObject location = jsonobject.getJSONObject("location");
            latitude = location.getString("latitude");
            longitude = location.getString("longitude");
            printResult(latitude, longitude, "airly", 0);
        }else{
            printResult(Double.toString(stacje[indexWios].latitude), Double.toString(stacje[indexWios].longitude), "WIOS", stacje[indexWios].id);
            // t.append(Double.toString(distanceToWios) + " " + Double.toString(stacje[indexWios].latitude) + " " + Double.toString(stacje[indexWios].longitude) + " WIOS " + stacje[indexWios].id); */
        }

        /*printResult(latitude, longitude, "airly");
        JSONObject jsonobject = jsonarray.getJSONObject(index);
        JSONObject location = jsonobject.getJSONObject("location");
        latitude = location.getString("latitude");
        longitude = location.getString("longitude");
        String vendor = jsonobject.getString("vendor");
        printResult(latitude, longitude, "airly");
        // t.append(latitude + " " + longitude + "\n" + distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude) + "\n");
    }*/




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

    void printResult(String Latitude, String Longitude, String vendor, int id) {
        if(vendor == "airly"){
            try{
                //My apikey
                // String apikey = "0d23d883ef6a4689b938fa0dbf21e8f3";
                //Airly apikey
                String apikey = "fae55480ef384880871f8b40e77bbef9";
                // String result = JsonTask("https://airapi.airly.eu/v1/mapPoint/measurements?latitude=" + Latitude + "&longitude=" + Longitude + "&apikey=" + apikey);
                String result = JsonTask("https://airapi.airly.eu/v1/sensor/measurements?sensorId=" + id + "&apikey=" + apikey);
                JSONObject obj = new JSONObject(result);

                // Get info
                Log.d("Response: ", "> Parsing data" );
                Double pm1 = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pm1"));
                Double pm10 = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pm10"));
                Double pm25 = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pm25"));
                Double pressure = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("pressure"));
                Double humidity = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("humidity"));
                Double temperature = Double.valueOf(obj.getJSONObject("currentMeasurements").getString("temperature"));

                // Calculation
                Log.d("Response: ", "> Rounding" );
                pm1 = Math.round(pm1 * 100.0) / 100.0;
                pm10 = Math.round(pm10 * 100.0) / 100.0;
                pm25 = Math.round(pm25 * 100.0) / 100.0;
                pressure = (double)(Math.round(pressure / 100));
                temperature = Math.round(temperature * 10.0) / 10.0;

                // Text update
                Log.d("Response: ", "> Text update" );
                //t.append("\n" + "PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n ");
                t.setText(" PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n " + "Pressure: " + pressure + "hPa" + "\n " + "Humidity: " + humidity + "%" + "\n " + "Temperature: " + temperature + "°C" + "\n ");
            }
            catch (Exception e) {e.printStackTrace();
            }
        }
        if(vendor == "WIOS") {
            try {
                String result = JsonTask("http://powietrze.malopolska.pl/_powietrzeapi/api/dane?act=danemiasta&ci_id=1");
                JSONObject obj = new JSONObject(result);
                for (int i = 0; i < 6; i++) {
                    JSONObject dane = obj.getJSONObject("dane");
                    JSONArray stacje = dane.getJSONArray("actual");
                    JSONObject stacja = stacje.getJSONObject(i);
                    int station_id  = stacja.getInt("station_id");
                    // t.append(station_id + " " + id + "\n");
                    if (station_id == id) {
                        JSONArray details = stacja.getJSONArray("details");
                        JSONObject test = details.getJSONObject(0);
                        int pm10 = test.getInt("o_value");
                        t.append("\n" + "Numer stacji WIOŚ: " + station_id + "\n" + "PM10: " + pm10);
                        // String longitude = location.getString("longitude");
                        // String latitude = location.getString("longitude");
                        // sensorId = jsonobject.getString("id");
                        //t.append(latitude + " " + longitude + "\n" /* + distance(Double.valueOf(Latitude), Double.valueOf(latitude), Double.valueOf(Longitude), Double.valueOf(longitude)) + "\n" );
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();

            }
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

    public static double distance(double lat1, double lat2, double lon1, double lon2) {

        final int R = 6371; // Radius of the earth

        Double latDistance = Math.toRadians(lat2 - lat1);
        Double lonDistance = Math.toRadians(lon2 - lon1);
        Double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        Double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c * 1000; // convert to meters

        distance = Math.pow(distance, 2);

        return Math.sqrt(distance);
    }
}