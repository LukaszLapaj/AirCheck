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

import com.air.check.station.StationWios;
import com.air.check.station.StationAirly;
import com.air.check.google.GoogleAPI;
import com.air.check.airly.ApiKey;
import com.air.check.utils.Distance;
import com.air.check.utils.JsonTask;

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
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

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
                    t.setText("Update...");
                    // new JsonTask().execute("http://188.166.73.207/add/1/" + location.getLatitude() + "/" + location.getLongitude());
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
                // runServices(60,10);
            }
        };

        checkPermission();
    }

    class stacja {
        public double latitude;
        public double longitude;
        public int id;

        public stacja(){
            latitude = 0;
            longitude = 0;
            id = 0;
        }

        public stacja(double lat, double lon, int i){
            latitude = lat;
            longitude = lon;
            id = i;
        }
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
        buildGoogleApiClient();
        if (mGoogleApiClient != null)
            mGoogleApiClient.connect();
        Log.d("Response: ", "> GPS Check" );
        //noinspection ResourceType
        locationManager.requestLocationUpdates("gps", time, distance, listener);
    }

    double hadDoubleValue(JSONObject obj, String key) throws JSONException{
        if(obj.has(key))
            return obj.optDouble(key);
        else
            return 0;
    }
    void downloadParsePrintTable(Double Latitude, Double Longitude) throws JSONException, ExecutionException, InterruptedException {
//        My apikey
//        String apikey = "0d23d883ef6a4689b938fa0dbf21e8f3";
//        My apikey
//        String apikey = "5f5c4d0463fe44829f463e4bf819bc00​";
//        Airly apikey
        String apikey = "fae55480ef384880871f8b40e77bbef9";
        String result = new JsonTask().execute("https://airapi.airly.eu/v1//sensors/current?southwestLat=0&southwestLong=0&northeastLat=89&northeastLong=180&apikey=" + apikey).get();
        int id = 0;
        int index = 0;
        stacja Airly = new stacja();
        double distanceToAirly = Double.MAX_VALUE;
        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            JSONObject location = jsonobject.getJSONObject("location");
            Double latitude = location.getDouble("latitude");
            Double longitude = location.getDouble("longitude");
            int sensorId = jsonobject.getInt("id");
            if(Distance.calculate(Latitude, latitude, Longitude, longitude) <= distanceToAirly){
                index =  i;
                Airly.latitude = latitude;
                Airly.longitude = longitude;
                Airly.id = sensorId;
                distanceToAirly = Distance.calculate(Latitude, latitude, Longitude, longitude);
            }
        }
//        JSONObject jsonobject = jsonarray.getJSONObject(index);
//        JSONObject location = jsonobject.getJSONObject("location");
//        Double latitude = location.getDouble("latitude");
//        Double longitude = location.getDouble("longitude");
//        String vendor = jsonobject.getString("vendor");
        printResult(Double.toString(Airly.latitude), Double.toString(Airly.longitude), "Airly", Airly.id, distanceToAirly);

        int indexWios = 0;
        double distanceToWios = Double.MAX_VALUE;

        stacja krasinskiego = new stacja(50.05767, 19.926189, 1);
        stacja bulwarowa = new stacja(50.069308, 20.053492, 2);
        stacja bujaka = new stacja(50.010575, 19.949189, 3);
        stacja dietla = new stacja(50.057447, 19.946008, 13);
        stacja piastow = new stacja(50.099361, 20.018317, 14);
        stacja zlotyrog = new stacja(50.081197, 19.895358, 15);
        stacja stacje[] = {krasinskiego, bulwarowa, bujaka, dietla, piastow, zlotyrog};

        for (int i = 0; i < stacje.length; i++) {
            if(Distance.calculate(Latitude, stacje[i].latitude, Longitude, stacje[i].longitude) < distanceToWios){
                indexWios =  i;
                distanceToWios = Distance.calculate(Latitude, stacje[i].latitude, Longitude, stacje[i].longitude);
            }
        }

        printResult(Double.toString(stacje[indexWios].latitude), Double.toString(stacje[indexWios].longitude), "WIOS", stacje[indexWios].id, distanceToWios);
        t.append("\n" + "Miedzy stacjami: " + Math.round(Distance.calculate(Airly.latitude, stacje[indexWios].latitude, Airly.longitude, stacje[indexWios].longitude) * 100) / 100);

//        if(distanceToAirly < distanceToWios)
//            printResult(Double.toString(Airly.latitude), Double.toString(Airly.longitude), "airly", Airly.id);
//        else
//            printResult(Double.toString(stacje[indexWios].latitude), Double.toString(stacje[indexWios].longitude), "WIOS", stacje[indexWios].id);
    }


    void printResult(String Latitude, String Longitude, String vendor, int id, double distance/*, stacjaAirly stacjaAirlyClosest, stacjaWios stacjaWiosClosest*/) {
        if(vendor == "Airly"){
            try{
                //My apikey
                // String apikey = "0d23d883ef6a4689b938fa0dbf21e8f3";
                //Airly apikey
                String apikey = "fae55480ef384880871f8b40e77bbef9";
                // String result = JsonTask("https://airapi.airly.eu/v1/mapPoint/measurements?latitude=" + Latitude + "&longitude=" + Longitude + "&apikey=" + apikey);
                String result = new JsonTask().execute("https://airapi.airly.eu/v1/sensor/measurements?sensorId=" + id + "&apikey=" + apikey).get();
                JSONObject obj = new JSONObject(result).getJSONObject("currentMeasurements");

                // Get info
                Log.d("Response: ", "> Parsing data" );
                Double pm1 = hadDoubleValue(obj, "pm1");
                Double pm10 = hadDoubleValue(obj, "pm10");
                Double pm25 = hadDoubleValue(obj, "pm25");
                Double pressure = hadDoubleValue(obj, "pressure");
                Double humidity = hadDoubleValue(obj, "humidity");
                Double temperature = hadDoubleValue(obj, "temperature");

                // Calculation
                Log.d("Response: ", "> Rounding" );
                pm1 = Math.round(pm1 * 100.0) / 100.0;
                pm10 = Math.round(pm10 * 100.0) / 100.0;
                pm25 = Math.round(pm25 * 100.0) / 100.0;
                pressure = (double)(Math.round(pressure / 100));
                humidity = Math.round(humidity * 100.0) / 100.0;
                temperature = Math.round(temperature * 10.0) / 10.0;

                // Text update
                Log.d("Response: ", "> Text update" );
                //t.append("\n" + "PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n ");
                t.setText(" PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n " + "Ciśnienie: " + pressure + "hPa" + "\n " + "Wilgotność: " + humidity + "%" + "\n " + "Temperatura: " + temperature + "°C" + "\n "+ "Odleglość: " + Math.round(distance * 100) / 100 + "\n ");
            }
            catch (Exception e) {e.printStackTrace();
            }
        }
        if(vendor == "WIOS") {
            try {
                String result = new JsonTask().execute("http://powietrze.malopolska.pl/_powietrzeapi/api/dane?act=danemiasta&ci_id=1").get();
                JSONObject obj = new JSONObject(result);
                int pm10 = 0;
                for (int i = 0; i < 6; i++) {
                    JSONObject dane = obj.getJSONObject("dane");
                    JSONArray actual = dane.getJSONArray("actual");
                    JSONObject stacja = actual.getJSONObject(i);
                    int station_id  = stacja.getInt("station_id");
                    if (station_id == id) {
                        JSONArray details = stacja.optJSONArray("details");
                        if(details.optJSONObject(0) != null)
                            pm10 = details.optJSONObject(0).getInt("o_value");
                        t.append("\n" + "Numer stacji WIOŚ: " + station_id + "\n" + "PM10: " + pm10 + "\n" + "Odleglość: " + Math.round(distance * 100) /100 + "\n ");
                        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
                        t.append("\n" + timestamp);
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