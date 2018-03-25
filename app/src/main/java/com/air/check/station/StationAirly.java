package com.air.check.station;

import android.location.Location;
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

public class StationAirly extends Station {
    private static int stationId;
    private static Double distanceTo;
    private static Double pm1;
    private static Double pm10;
    private static Double pm25;
    private static Double pressure;
    private static Double humidity;
    private static Double temperature;
    private static Double airQualityIndex;
    private static String country;
    private static String locality;
    private static String route;
    private static String streetnumber;
    private static String measurementTime;

    public StationAirly() {
        distanceTo = Double.MAX_VALUE;
    }

    public StationAirly FindStation(Location userLocation) throws ExecutionException, InterruptedException, JSONException {
        double userLatitude = userLocation.getLatitude();
        double userLongitude = userLocation.getLongitude();
        String result = new JsonTask().execute("https://airapi.airly.eu/v1//sensors/current?southwestLat=-89.999999999999&southwestLong=-180&northeastLat=89.999999999999&northeastLong=180&apikey=" + ApiKey.get()).get();
        JSONArray stationsTable = new JSONArray(result);
        if (result != null) {
            for (int i = 0; i < stationsTable.length(); ++i) {
                JSONObject stationsTableJSONObject = stationsTable.getJSONObject(i);
                JSONObject location = stationsTableJSONObject.getJSONObject("location");
                Double testLatitude = location.getDouble("latitude");
                Double testLongitude = location.getDouble("longitude");
                int sensorId = stationsTableJSONObject.getInt("id");
                if (Distance.calculate(userLatitude, testLatitude, userLongitude, testLongitude) <= getDistanceTo()) {
                    setStationId(sensorId);
                    setLatitude(testLatitude);
                    setLongitude(testLongitude);
                    JSONObject address = stationsTableJSONObject.getJSONObject("address");
                    setCountry(address.optString("country"));
                    setLocality(address.optString("locality"));
                    setRoute(address.optString("route"));
                    setStreetnumber(address.optString("streetNumber"));
                    setDistanceTo(Distance.calculate(userLatitude, testLatitude, userLongitude, testLongitude));
                }
            }
            Update();
        }
        return this;
    }

    void Update() throws ExecutionException, InterruptedException, JSONException {
        String result = new JsonTask().execute("https://airapi.airly.eu/v1/sensor/measurements?sensorId=" + getStationId() + "&apikey=" + ApiKey.get()).get();
        JSONObject obj = new JSONObject(result).getJSONObject("currentMeasurements");

        // Set info
        //Log.d("Response: ", "> Parsing data" );
        setPm1(hasDoubleValue(obj, "pm1"));
        setPm10(hasDoubleValue(obj, "pm10"));
        setPm25(hasDoubleValue(obj, "pm25"));
        setPressure(hasDoubleValue(obj, "pressure"));
        setHumidity(hasDoubleValue(obj, "humidity"));
        setTemperature(hasDoubleValue(obj, "temperature"));
        setAirQualityIndex(hasDoubleValue(obj, "airQualityIndex"));

        // Rounding
        //Log.d("Response: ", "> Rounding" );
        roundPm();
        roundPressure();
        roundHumidity();
        roundTemperature();
        roundAirQualityIndex();

        roundDistanceTo();
    }

    private void roundPm() {
        pm1 = Math.round(pm1 * 100.0) / 100.0;
        pm10 = Math.round(pm10 * 100.0) / 100.0;
        pm25 = Math.round(pm25 * 100.0) / 100.0;
    }

    void roundPressure() {
        pressure = Math.round(pressure / 100.0) / 1.0;
    }

    void roundHumidity() {
        humidity = Math.round(humidity * 100.0) / 100.0;
    }

    void roundTemperature() {
        temperature = Math.round(temperature * 10.0) / 10.0;
    }

    void roundDistanceTo() {
        distanceTo = (double) Math.round(distanceTo) * 100 / 100;
    }

    void roundAirQualityIndex() {
        airQualityIndex = Math.round(airQualityIndex * 100.0) / 100.0;
    }

    //@SuppressLint("DefaultLocale")
    public String toString() {
        StringBuilder builder = new StringBuilder("");
        if (getLocality() != "") builder.append("Lokalizacja: " + getLocality());
        if (!getRoute().equals("")) builder.append("\n" + "Adres: " + getRoute() + " " + getStreetnumber());
        if (getAirQualityIndex() != 0) builder.append("\n" + "AQI: " + getAirQualityIndex());
        if (getPm1() != 0) builder.append("\n" + "PM1: " + getPm1() + "µg/m³");
        if (getPm25() != 0) builder.append("\n" + "PM2.5: " + getPm25() + "µg/m³");
        if (getPm10() != 0) builder.append("\n" + "PM10: " + getPm10() + "µg/m³");
        if (getPressure() != 0) builder.append("\n" + "Ciśnienie: " + getPressure() + "hPa");
        if (getHumidity() != 0) builder.append("\n" + "Wilgotność: " + getHumidity() + "%");
        if (getTemperature() != 0) builder.append("\n" + "Temperatura: " + getTemperature() + "°C");
        if (getDistanceTo() != 0) builder.append("\n" + "Odleglość: " + getDistanceTo() + "m");
        return builder.toString();
    }

    public static int getStationId() {
        return stationId;
    }

    public static void setStationId(int stationId) {
        StationAirly.stationId = stationId;
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

    public static Double getAirQualityIndex() {
        return airQualityIndex;
    }

    public static void setAirQualityIndex(Double airQualityIndex) {
        StationAirly.airQualityIndex = airQualityIndex;
    }

    public static String getCountry() {
        return country;
    }

    public static void setCountry(String country) {
        StationAirly.country = country;
    }

    public static String getLocality() {
        return locality;
    }

    public static void setLocality(String locality) {
        StationAirly.locality = locality;
    }

    public static String getRoute() {
        return route;
    }

    public static void setRoute(String route) {
        StationAirly.route = route;
    }

    public static String getStreetnumber() {
        return streetnumber;
    }

    public static void setStreetnumber(String streetnumber) {
        StationAirly.streetnumber = streetnumber;
    }

    public static String getMeasurementTime() {
        return measurementTime;
    }

    public static void setMeasurementTime(String measurementTime) {
        StationAirly.measurementTime = measurementTime;
    }
}