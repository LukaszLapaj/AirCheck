package com.air.check.station;

import android.util.Log;

import com.air.check.airly.ApiKey;
import com.air.check.utils.Distance;
import com.air.check.utils.JsonTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by Lukasz on 16.02.2017.
 */

public class StationAirly {
    public Double latitude;
    public Double longitude;
    public static int stationId;
    public static int index;
    public static Double distanceTo = Double.MAX_VALUE;
    public static Double pm1, pm10, pm25, pressure, humidity, temperature;

    public StationAirly(){
        distanceTo = Double.MAX_VALUE;
    }

    public StationAirly(Double lat, Double lon){
        Double latitude = lat;
        Double longitude = lon;
        distanceTo = Double.MAX_VALUE;
    }

    public StationAirly FindStation(Double lat, Double lon) throws ExecutionException, InterruptedException, JSONException {
        StationAirly Station = new StationAirly(lat, lon);
        String result = new JsonTask().execute("https://airapi.airly.eu/v1//sensors/current?southwestLat=0&southwestLong=0&northeastLat=89&northeastLong=180&apikey=" + ApiKey.get()).get();
        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); i++) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            JSONObject location = jsonobject.getJSONObject("location");
            Double latitude = location.getDouble("latitude");
            Double longitude = location.getDouble("longitude");
            int sensorId = jsonobject.getInt("id");
            if(Distance.calculate(lat, latitude, lon, longitude) <= Station.distanceTo){
                Station.index = i;
                Station.stationId = sensorId;
                Station.latitude = latitude;
                Station.longitude = longitude;
                Station.distanceTo = Distance.calculate(lat, latitude, lon, longitude);
            }
        }
        Station.Update(Station);
        return Station;
    }

    void Update(StationAirly Stacja) throws ExecutionException, InterruptedException, JSONException {
        String result = new JsonTask().execute("https://airapi.airly.eu/v1/sensor/measurements?sensorId=" + Stacja.stationId + "&apikey=" + ApiKey.get()).get();
        JSONObject obj = new JSONObject(result).getJSONObject("currentMeasurements");

        // Get info
        Log.d("Response: ", "> Parsing data" );
        Stacja.pm1 = hasDoubleValue(obj, "pm1");
        Stacja.pm10 = hasDoubleValue(obj, "pm10");
        Stacja.pm25 = hasDoubleValue(obj, "pm25");
        Stacja.pressure = hasDoubleValue(obj, "pressure");
        Stacja.humidity = hasDoubleValue(obj, "humidity");
        Stacja.temperature = hasDoubleValue(obj, "temperature");

        // Calculation
        Log.d("Response: ", "> Rounding" );
        Stacja.pm1 = Math.round(pm1 * 100.0) / 100.0;
        Stacja.pm10 = Math.round(pm10 * 100.0) / 100.0;
        Stacja.pm25 = Math.round(pm25 * 100.0) / 100.0;
        Stacja.pressure = (Math.round(pressure / 100.0)) / 1.0;
        Stacja.humidity = Math.round(humidity * 100.0) / 100.0;
        Stacja.temperature = Math.round(temperature * 10.0) / 10.0;

        Stacja.distanceTo = Math.round(Stacja.distanceTo * 100.0) / 100.0;
    }

    private Double hasDoubleValue(JSONObject obj, String key) throws JSONException{
        if(obj.has(key))
            return obj.optDouble(key);
        else
            return 0.0;
    }
}
