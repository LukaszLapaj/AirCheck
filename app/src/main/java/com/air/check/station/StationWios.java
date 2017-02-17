package com.air.check.station;

import android.util.Log;

import com.air.check.utils.Distance;
import com.air.check.utils.JsonTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by Lukasz on 16.02.2017.
 */

public class StationWios {
    public static String name;
    public Double latitude;
    public Double longitude;
    public static int stationId;
    public static int cityId;
    public static int index;
    public static Double distanceTo;
    public static int pm10, pm25, so2, no2, co, c6h6, o3;

    public StationWios(Double lat, Double lon, int sId, int cId, String nazwa){
        Double latitude = lat;
        Double longitude = lon;
        int stationId = sId;
        int cityId = cId;
        String name = nazwa;
        distanceTo = Double.MAX_VALUE;
    }

    public StationWios(Double lat, Double lon){
        Double latitude = lat;
        Double longitude = lon;
        distanceTo = Double.MAX_VALUE;
    }

    public StationWios(){
        distanceTo = Double.MAX_VALUE;
    }

    public StationWios FindStation(Double lat, Double lon) throws ExecutionException, InterruptedException, JSONException {
        StationWios krasinskiego = new StationWios(50.05767, 19.926189, 1, 1, "Aleja Krasińskiego");
        StationWios bulwarowa = new StationWios(50.069308, 20.053492, 2, 1, "Bulwarowa");
        StationWios bujaka = new StationWios(50.010575, 19.949189, 3, 1, "Bujaka");
        StationWios dietla = new StationWios(50.057447, 19.946008, 13, 1, "Dietla");
        StationWios piastow = new StationWios(50.099361, 20.018317, 14, 1,"Os.Piastów");
        StationWios zlotyrog = new StationWios(50.081197, 19.895358, 15, 1, "Złoty róg");
        StationWios stacja[] = {krasinskiego, bulwarowa, bujaka, dietla, piastow, zlotyrog};
        StationWios Station = new StationWios(50.081197, 19.895358, 15, 1, "Złoty róg");
        for (int i = 0; i < stacja.length; i++) {
//            if(Distance.calculate(lat, stacja[i].latitude, lon, stacja[i].longitude) < Station.distanceTo){
//                Station.index = i;
//                Station.latitude = stacja[i].latitude;
//                Station.longitude = stacja[i].longitude;
//                Station.distanceTo = Distance.calculate(lat, stacja[i].latitude, lon, stacja[i].longitude);
//            }
        }
        Station.Update(Station);
        return Station;
    }

    public void Update(StationWios Stacja) throws ExecutionException, InterruptedException, JSONException {
        StationWios krasinskiego = new StationWios(50.05767, 19.926189, 1, 1, "Aleja Krasińskiego");
        StationWios bulwarowa = new StationWios(50.069308, 20.053492, 2, 1, "Bulwarowa");
        StationWios bujaka = new StationWios(50.010575, 19.949189, 3, 1, "Bujaka");
        StationWios dietla = new StationWios(50.057447, 19.946008, 13, 1, "Dietla");
        StationWios piastow = new StationWios(50.099361, 20.018317, 14, 1,"Os.Piastów");
        StationWios zlotyrog = new StationWios(50.081197, 19.895358, 15, 1, "Złoty róg");
        StationWios stacja[] = {krasinskiego, bulwarowa, bujaka, dietla, piastow, zlotyrog};
        String result = new JsonTask().execute("http://powietrze.malopolska.pl/_powietrzeapi/api/dane?act=danemiasta&ci_id=1").get();
        JSONObject obj = new JSONObject(result);
        for (int i = 0; i < stacja.length; i++) {
            JSONArray actual = obj.getJSONObject("dane").getJSONArray("actual");
            JSONObject stacjaa = actual.getJSONObject(i);
            int station_id = stacjaa.getInt("station_id");
            if (station_id == Stacja.stationId) {
                JSONArray details = stacjaa.optJSONArray("details");
                for (int j = 0; j < details.length(); ++j) {
                    while (details.optJSONObject(i) != null) {
                        Stacja.pm10 = checkValue(details, i, "pm10");
                        Stacja.pm25 = checkValue(details, i, "pm2.5");
                        Stacja.no2 = checkValue(details, i, "no2");
                        Stacja.so2 = checkValue(details, i, "so2");
                        Stacja.co = checkValue(details, i, "co");
                        Stacja.o3 = checkValue(details, i, "o3");
                        Stacja.c6h6 = checkValue(details, i, "c6h6");
                    }
                }
                Stacja.pm10 = details.optJSONObject(0).getInt("o_value");
            }
        }
        Stacja.distanceTo = (double)Math.round(Stacja.distanceTo * 100) / 100;
    }

    int checkValue(JSONArray array, int index, String value) throws JSONException{
        if(array.optJSONObject(index).optString(value) == value)
            return array.optJSONObject(index).getInt("o_value");
        return 0;
    }
}
