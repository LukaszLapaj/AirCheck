package com.air.check;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.air.check.station.StationAirly;
import com.air.check.station.StationWios;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{
    private Button b;
    private TextView t1, t2, t3;

    private LocationManager locationManager;
    private LocationListener listener;

    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;

    public Handler mHandler;
    public Handler mHandler2;
    public Handler mHandler3;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mHandler = new Handler();
        mHandler2 = new Handler();
        mHandler3 = new Handler();
        MultiDex.install(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        t1 = (TextView) findViewById(R.id.textView1);
        t2 = (TextView) findViewById(R.id.textView2);
        t3 = (TextView) findViewById(R.id.textView3);
        b = (Button) findViewById(R.id.button);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    downloadParsePrintTable(location);
                } catch (Exception e) {
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
                != PackageManager.PERMISSION_GRANTED)) {
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
        runServices(25,1);
        b.setOnClickListener((View view) -> {
            Log.d("Response: ", "> Button Pressed" );
            runServices(25,1);
        });
    }

    static ExecutorService updateT = Executors.newSingleThreadExecutor();
    public class updaterThread implements Runnable {
        private Location userLocation;

        public updaterThread(Location userLocation) {
            this.userLocation = userLocation;
        }

        @Override
        public void run() {
            StationAirly Airly = new StationAirly();
            try {
                Airly = new StationAirly().FindStation(userLocation);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            StationWios Wios = new StationWios();
            try {
                Wios = new StationWios().FindStation(userLocation);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            final StationAirly Airlyy = Airly;
            final StationWios Wioss = Wios;
            mHandler.post(() -> t1.setText(Airlyy.toString()));
            mHandler2.post(() -> t2.setText(Wioss.toString()));
            mHandler3.post(() -> {
                t3.setText(Airlyy.distanceTo(Wioss));
                DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
                Date currentTime = new Date();
                t3.append("Ostatnia aktualizacja: " + dateFormat.format(currentTime));
            });
        }
    }

    void runServices(long time, float distance){
        // Time convert from milliseconds to seconds
        time *= 1000;
        Log.d("Response: ", "> Last Localisation Check" );
        int statusCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
            buildGoogleApiClient();
            if (mGoogleApiClient != null)
                mGoogleApiClient.connect();
        }else {
            Log.d("Response: ", "> No Google Play Services!");
        }
        if (statusCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
            Log.d("Response: ", "> Google Play Services outdated!" );
        Log.d("Response: ", "> GPS Check" );
        //noinspection ResourceType
        locationManager.requestLocationUpdates("gps", time, distance, listener);
    }

    void downloadParsePrintTable(Location location) throws JSONException, ExecutionException, InterruptedException, Exception {
//        Latitude =  50.05767;
//        Longitude = 19.926189;
        updateT.execute(new updaterThread(location));
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
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("Response: ", "> Getting data from last location" );
            try {
                downloadParsePrintTable(mLastLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {}
}