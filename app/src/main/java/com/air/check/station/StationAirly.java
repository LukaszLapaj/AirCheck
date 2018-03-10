package com.air.check.station;

import android.annotation.SuppressLint;
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

public class StationAirly extends Station{
    public static int stationId;
    public static int index;
    public static Double distanceTo;

    public static Double pm1, pm10, pm25, pressure, humidity, temperature;

    public StationAirly(){
        distanceTo = Double.MAX_VALUE;
    }

    public StationAirly(Double latitude, Double longitude){
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        distanceTo = Double.MAX_VALUE;
    }

    public StationAirly FindStation(Double lat, Double lon) throws ExecutionException, InterruptedException, JSONException {
        StationAirly Station = new StationAirly(lat, lon);
        String result = new JsonTask().execute("https://airapi.airly.eu/v1//sensors/current?southwestLat=-89.999999999999&southwestLong=-180&northeastLat=89.999999999999&northeastLong=180&apikey=" + ApiKey.get()).get();
        JSONArray stationsTable = new JSONArray(result);
        for (int i = 0; i < stationsTable.length(); ++i) {
            JSONObject jsonobject = stationsTable.getJSONObject(i);
            JSONObject location = jsonobject.getJSONObject("location");
            Double latitude = location.getDouble("latitude");
            Double longitude = location.getDouble("longitude");
            int sensorId = jsonobject.getInt("id");
            if(Distance.calculate(lat, latitude, lon, longitude) <= distanceTo){
                index = i;
                stationId = sensorId;
                Station.setLatitude(latitude);
                Station.setLongitude(longitude);
                distanceTo = Distance.calculate(lat, latitude, lon, longitude);
            }
        }
        Station.Update(Station);
        return Station;
    }

    void Update(StationAirly Stacja) throws ExecutionException, InterruptedException, JSONException {
        String result = new JsonTask().execute("https://airapi.airly.eu/v1/sensor/measurements?sensorId=" + stationId + "&apikey=" + ApiKey.get()).get();
        JSONObject obj = new JSONObject(result).getJSONObject("currentMeasurements");

        // Set info
        Log.d("Response: ", "> Parsing data" );
        setPm1(hasDoubleValue(obj, "pm1"));
        setPm10(hasDoubleValue(obj, "pm10"));
        setPm25(hasDoubleValue(obj, "pm25"));
        setPressure(hasDoubleValue(obj, "pressure"));
        setHumidity(hasDoubleValue(obj, "humidity"));
        setTemperature(hasDoubleValue(obj, "temperature"));

        // Rounding
        Log.d("Response: ", "> Rounding" );
        roundPm();
        roundPressure();
        roundHumidity();
        roundTemperature();

        roundDistanceTo();
    }

    private Double hasDoubleValue(JSONObject obj, String key) throws JSONException{
        if(obj.has(key))
            return obj.optDouble(key);
        else
            return 0.0;
    }

    private void roundPm(){
        this.pm1 = Math.round(pm1 * 100.0) / 100.0;
        this.pm10 = Math.round(pm10 * 100.0) / 100.0;
        this.pm25 = Math.round(pm25 * 100.0) / 100.0;
    }

    void roundPressure(){
        this.pressure = Math.round(pressure / 100.0) / 1.0;
    }

    void roundHumidity(){
        this.humidity = Math.round(humidity * 100.0) / 100.0;
    }

    void roundTemperature(){
        this.temperature = Math.round(temperature * 10.0) / 10.0;
    }
    void roundDistanceTo(){
        this.distanceTo = Math.round(distanceTo * 100.0) / 100.0 / 100 * 100;
    }

    @SuppressLint("DefaultLocale")
    public String toString(){
        return " PM1: " + getPm1() + "\n " + "PM2.5: " + getPm25() + "\n " + "PM10: " + getPm10() + "\n " + "Ciśnienie: " + getPressure() + "hPa" + "\n " + "Wilgotność: " + getHumidity() + "%" + "\n " + "Temperatura: " + getTemperature() + "°C" + "\n " + "Odleglość: " + getDistanceTo();
    }

    public static Double getDistanceTo() {
        return distanceTo;
    }

    public static void setDistanceTo(Double distanceTo) {
        StationAirly.distanceTo = distanceTo;
    }

    public static Double getPm1() {
        return pm1;
    }

    public static void setPm1(Double pm1) {
        StationAirly.pm1 = pm1;
    }

    public static Double getPm10() {
        return pm10;
    }

    public static void setPm10(Double pm10) {
        StationAirly.pm10 = pm10;
    }

    public static Double getPm25() {
        return pm25;
    }

    public static void setPm25(Double pm25) {
        StationAirly.pm25 = pm25;
    }

    public static Double getPressure() {
        return pressure;
    }

    public static void setPressure(Double pressure) {
        StationAirly.pressure = pressure;
    }

    public static Double getHumidity() {
        return humidity;
    }

    public static void setHumidity(Double humidity) {
        StationAirly.humidity = humidity;
    }

    public static Double getTemperature() {
        return temperature;
    }

    public static void setTemperature(Double temperature) {
        StationAirly.temperature = temperature;
    }

}