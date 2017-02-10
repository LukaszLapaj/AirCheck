package com.air.check;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.util.Log;
import android.os.*;
import org.json.*;
import java.net.*;
import java.io.*;


public class MainActivity extends AppCompatActivity {

    private Button b;
    private TextView t;
    private LocationManager locationManager;
    private LocationListener listener;

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
                new JsonTask().execute("https://airapi.airly.eu/v1/mapPoint/measurements?latitude=" + location.getLatitude() + "&longitude=" + location.getLongitude() + "&apikey=0d23d883ef6a4689b938fa0dbf21e8f3");
                //t.append( + "\n ");
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        configure_button();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case 10:
                configure_button();
                break;
            default:
                break;
        }
    }

    void configure_button(){
        // Permission check
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.INTERNET}
                        ,10);
            }
            return;
        }
        // this code won't execute IF permissions are not allowed, because in the line above there is return statement.
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //noinspection MissingPermission
                Log.d("Response: ", "> Button Pressed" );
                locationManager.requestLocationUpdates("gps", 60000, 10, listener);
                Log.d("Response: ", "> GPS Service Running" );
            }
        });
    }

    private class JsonTask extends AsyncTask<String, String, String> {

        protected String doInBackground(String... params) {

            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                Log.d("Response: ", "> Establishing Connection" );
                URL url = new URL(params[0]);
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
        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try{
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

                // Text update
                Log.d("Response: ", "> Text update" );
                t.setText("PM1: " + pm1 + "\n " + "PM2.5: " + pm25 + "\n " + "PM10: " + pm10 + "\n " + "Pressure: " + pressure + "\n " + "Humidity: " + humidity + "%" + "\n " + "Temperature: " + temperature + "°C" + "\n ");
            }
            catch (Exception e) {e.printStackTrace();
            }
        }
    }
}