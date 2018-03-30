package com.air.check;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
//import android.support.multidex.MultiDex;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    public Button b;
    public TextView t1, t2, t3;

    private LocationManager locationManager;
    private LocationListener listener;

    public GoogleApiClient mGoogleApiClient;
    public Location mLastLocation;

    public Handler mHandler;
    public Handler mHandler2;
    public Handler mHandler3;

    private Vibrator mVibrator;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        mHandler = new Handler();
        mHandler2 = new Handler();
        mHandler3 = new Handler();
        mVibrator = (Vibrator) this.getSystemService(Context.VIBRATOR_SERVICE);
        //MultiDex.install(this);
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
                    runUpdaterService(location);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}

            @Override
            public void onProviderEnabled(String provider) {}

            @Override
            public void onProviderDisabled(String provider) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };
        checkPermission();
    }

    void checkPermission() {
        // Permission check
        if ((ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}, 10);
            return;
        }
        buttonListener();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 10)
            checkPermission();
    }

    void buttonListener() {
        runServices(25, 1);
        b.setOnClickListener((View view) -> {
            Log.d("Response: ", "> Button Pressed");
            mVibrator.vibrate(60);
            runServices(25, 1);
        });
    }

    static ExecutorService updaterService = Executors.newSingleThreadExecutor();

    public class updaterThread implements Runnable {
        private Location userLocation;
        private StationAirly Airly = new StationAirly();
        private StationWios Wios = new StationWios();
        private DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

        updaterThread(Location userLocation) {
            this.userLocation = userLocation;
        }

        @Override
        public void run() {
            try {
                this.Airly = new StationAirly().FindStation(userLocation);
                this.Wios = new StationWios().FindStation(userLocation);
            } catch (Exception e) {
                e.printStackTrace();
            }
            StationAirly finalAirly = Airly;
            StationWios finalWios = Wios;
            mHandler.post(() -> t1.setText(finalAirly.toString()));
            mHandler2.post(() -> t2.setText(finalWios.toString()));
            mHandler3.post(() -> {
                t3.setText(finalAirly.distanceTo(finalWios));
                Date currentTime = new Date();
                t3.append("Ostatnia aktualizacja: " + dateFormat.format(currentTime));
            });
        }
    }

    void runServices(long timeInMilliseconds, float distance) {
        long timeInSeconds = timeInMilliseconds * 1000;
        Log.d("Response: ", "> Last Localisation Check");
        int statusCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
        if (statusCode == ConnectionResult.SUCCESS) {
            buildGoogleApiClient();
            if (mGoogleApiClient != null) {
                mGoogleApiClient.connect();
            }
        } else {
            Log.d("Response: ", "> No Google Play Services!");
        }
        if (statusCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED)
            Log.d("Response: ", "> Google Play Services outdated!");
        Log.d("Response: ", "> GPS Check");
        //noinspection ResourceType
        locationManager.requestLocationUpdates("gps", timeInSeconds, distance, listener);
    }

    void runUpdaterService(Location location) {
        // location.setLatitude(50.05767);
        // location.setLongitude(19.926189);
        updaterService.execute(new updaterThread(location));
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @Override
    public void onConnectionFailed(ConnectionResult arg0) {
    }

    @Override
    public void onConnected(Bundle arg0) {
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("Response: ", "> Getting data from last location");
                runUpdaterService(mLastLocation);
        }
    }

    @Override
    public void onConnectionSuspended(int arg0) {}
}