package com.air.check.google;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import org.json.JSONException;

import java.util.concurrent.ExecutionException;

/**
 * Created by Lukasz on 16.02.2017.
 */

public class GoogleAPI {

    /* private final mGoogleApiClient.ConnectionCallbacks connectionCallbacks;

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

    private final GoogleApiClient.ConnectionCallbacks connectionCallbacks;

    public GoogleModule(GoogleApiClient.ConnectionCallbacks connectionCallbacks) {
        this.connectionCallbacks = connectionCallbacks;
    }

    public GoogleApiClient provideGoogleApiClient(Context context) {

        return new GoogleApiClient.Builder(context)
                .addConnectionCallbacks(connectionCallbacks)
                .addApi(LocationServices.API)
                .build();
    } */
}
