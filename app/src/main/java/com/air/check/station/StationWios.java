package com.air.check.station;

import com.air.check.utils.Distance;
import com.air.check.utils.JsonTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.ExecutionException;

/**
 * Created by Lukasz on 16.02.2017.
 */

public class StationWios extends Station{
    public String name;
    public int cityId, stationId;
    public int index;
    public Double distanceTo;
    public int pm10, pm25, so2, no2, co, c6h6, o3;

    public StationWios(Double lat, Double lon, int sId, int cId, String nazwa){
        latitude = lat;
        longitude = lon;
        stationId = sId;
        cityId = cId;
        index = 0;
        name = nazwa;
    }
    public StationWios(Double latitude, Double longitude){
        this.latitude = latitude;
        this.longitude = longitude;
        distanceTo = Double.MAX_VALUE;
    }

    public StationWios(){
        latitude = 0.0;
        longitude = 0.0;
        stationId = 0;
        cityId = 0;
        index = 0;
        distanceTo = Double.MAX_VALUE;
        name = "";
    }

    public StationWios FindStation(Double lat, Double lon) throws ExecutionException, InterruptedException, JSONException {
        StationWios krasinskiego = new StationWios(50.05767, 19.926189, 1, 1, "Aleja Krasińskiego");
        StationWios bulwarowa = new StationWios(50.069308, 20.053492, 2, 1, "Bulwarowa");
        StationWios bujaka = new StationWios(50.010575, 19.949189, 3, 1, "Bujaka");
        StationWios dietla = new StationWios(50.057447, 19.946008, 13, 1, "Dietla");
        StationWios piastow = new StationWios(50.099361, 20.018317, 14, 1,"Os.Piastów");
        StationWios zlotyrog = new StationWios(50.081197, 19.895358, 15, 1, "Złoty róg");
        StationWios stacja[] = {krasinskiego, bulwarowa, bujaka, dietla, piastow, zlotyrog};
        StationWios Station = new StationWios();
        for (int i = 0; i < stacja.length; i++) {
            if(Distance.calculate(lat, stacja[i].latitude, lon, stacja[i].longitude) < Station.distanceTo){
                Station.index = i;
                // Station.name = stacja[i].name;
                Station.stationId = stacja[i].stationId;
                Station.latitude = stacja[i].latitude;
                Station.longitude = stacja[i].longitude;
                Station.cityId = stacja[i].cityId;
                Station.distanceTo = Distance.calculate(lat, stacja[i].latitude, lon, stacja[i].longitude);
            }
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
        String result = new JsonTask().execute("http://powietrze.malopolska.pl/_powietrzeapi/api/dane?act=danemiasta&ci_id=" + String.valueOf(Stacja.cityId)).get();
        JSONObject obj = new JSONObject(result);
        for (int i = 0; i < stacja.length; i++) {
            JSONArray actual = obj.getJSONObject("dane").getJSONArray("actual");
            JSONObject stacjaa = actual.getJSONObject(i);
            if (i == Stacja.index) {
                Stacja.name = stacjaa.getString("station_name");
                JSONArray details = stacjaa.getJSONArray("details");
                for (int j = 0; j < details.length(); ++j) {
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("pm10"))
                        Stacja.pm10 = (details.optJSONObject(j).getInt("o_value"));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("pm2.5"))
                        Stacja.pm25 = (details.optJSONObject(j).getInt("o_value"));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("no2"))
                        Stacja.no2 = (details.optJSONObject(j).getInt("o_value"));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("so2"))
                        Stacja.o3 = (details.optJSONObject(j).getInt("o_value"));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("o3"))
                        Stacja.c6h6 = (details.optJSONObject(j).getInt("o_value"));
                }
            }
        }
    }
}

