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
    private String name;
    private int cityId;
    private int stationId;
    private int index;
    private Double distanceTo;
    private int pm10;
    private int pm25;
    private int so2;
    private int no2;
    private int co;
    private int c6h6;
    private int o3;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (getStationId() != 0) builder.append("Numer stacji WIOŚ: " + getStationId());
        if (getName() != "") builder.append("\n" + "Nazwa stacji: " + getName());
        if (getPm25() != 0) builder.append("\n" + "PM2.5: " + getPm25() + "µg/m³");
        if (getPm10() != 0) builder.append("\n" + "PM10: " + getPm10() + "µg/m³");
        if (getSo2() != 0) builder.append("\n" + "SO2: " + getSo2() + "µg/m³");
        if (getNo2() != 0) builder.append("\n" + "NO2: " + getNo2() + "µg/m³");
        if (getCo() != 0) builder.append("\n" + "CO: " + getCo() + "µg/m³");
        if (getC6h6() != 0) builder.append("\n" + "C6H6: " + getC6h6() + "µg/m³");
        if (getO3() != 0) builder.append("\n" + "O3: " + getO3() + "µg/m³");
        if (getDistanceTo() != 0) builder.append("\n" + "Odleglość: " + getDistanceTo() + "m");
        return builder.toString();
    }

    public StationWios(Double latitude, Double longitude, int stationId, int cityId, String name){
        setLatitude(latitude);
        setLongitude(longitude);
        setStationId(stationId);
        setCityId(cityId);
        setIndex(0);
        setName(name);
    }
    public StationWios(Double latitude, Double longitude){
        this.setLatitude(latitude);
        this.setLongitude(longitude);
        setDistanceTo(Double.MAX_VALUE);
    }

    public StationWios(){
        setLatitude(0.0);
        setLongitude(0.0);
        setStationId(0);
        setCityId(0);
        setIndex(0);
        setDistanceTo(Double.MAX_VALUE);
        setName("");
    }

    public StationWios FindStation(Double userLatitude, Double userLongitude) throws ExecutionException, InterruptedException, JSONException {
        StationWios krasinskiego = new StationWios(50.05767, 19.926189, 1, 1, "Aleja Krasińskiego");
        StationWios bulwarowa = new StationWios(50.069308, 20.053492, 2, 1, "Bulwarowa");
        StationWios bujaka = new StationWios(50.010575, 19.949189, 3, 1, "Bujaka");
        StationWios dietla = new StationWios(50.057447, 19.946008, 13, 1, "Dietla");
        StationWios piastow = new StationWios(50.099361, 20.018317, 14, 1,"Os.Piastów");
        StationWios zlotyrog = new StationWios(50.081197, 19.895358, 15, 1, "Złoty róg");
        StationWios stacja[] = {krasinskiego, bulwarowa, bujaka, dietla, piastow, zlotyrog};
        StationWios Station = new StationWios();
        for (int i = 0; i < stacja.length; i++) {
            if(Distance.calculate(userLatitude, stacja[i].getLatitude(), userLongitude, stacja[i].getLongitude()) < Station.getDistanceTo()){
                Station.setIndex(i);
                // Station.name = stacja[i].name;
                Station.setStationId(stacja[i].getStationId());
                Station.setLatitude(stacja[i].getLatitude());
                Station.setLongitude(stacja[i].getLongitude());
                Station.setCityId(stacja[i].getCityId());
                Station.setDistanceTo(Distance.calculate(userLatitude, stacja[i].getLatitude(), userLongitude, stacja[i].getLongitude()));
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
        String result = new JsonTask().execute("http://powietrze.malopolska.pl/_powietrzeapi/api/dane?act=danemiasta&ci_id=" + String.valueOf(Stacja.getCityId())).get();
        JSONObject obj = new JSONObject(result);
        for (int i = 0; i < stacja.length; i++) {
            JSONArray actual = obj.getJSONObject("dane").getJSONArray("actual");
            JSONObject stacjaa = actual.getJSONObject(i);
            if (i == Stacja.getIndex()) {
                Stacja.setName(stacjaa.getString("station_name"));
                JSONArray details = stacjaa.getJSONArray("details");
                for (int j = 0; j < details.length(); ++j) {
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("pm10"))
                        Stacja.setPm10((details.optJSONObject(j).getInt("o_value")));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("pm2.5"))
                        Stacja.setPm25((details.optJSONObject(j).getInt("o_value")));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("no2"))
                        Stacja.setNo2((details.optJSONObject(j).getInt("o_value")));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("so2"))
                        Stacja.setO3((details.optJSONObject(j).getInt("o_value")));
                    if(details.optJSONObject(j).optString("o_wskaznik").equals("o3"))
                        Stacja.setC6h6((details.optJSONObject(j).getInt("o_value")));
                }
            }
        }
        roundDistanceTo();
    }

    void roundDistanceTo(){
        distanceTo = (double)Math.round(distanceTo) * 100 / 100;
    }

    public Double getDistanceTo() {
        return distanceTo;
    }

    public void setDistanceTo(Double distanceTo) {
        this.distanceTo = distanceTo;
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getStationId() {
        return stationId;
    }

    public void setStationId(int stationId) {
        this.stationId = stationId;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getPm10() {
        return pm10;
    }

    public void setPm10(int pm10) {
        this.pm10 = pm10;
    }

    public int getPm25() {
        return pm25;
    }

    public void setPm25(int pm25) {
        this.pm25 = pm25;
    }

    public int getSo2() {
        return so2;
    }

    public void setSo2(int so2) {
        this.so2 = so2;
    }

    public int getNo2() {
        return no2;
    }

    public void setNo2(int no2) {
        this.no2 = no2;
    }

    public int getCo() {
        return co;
    }

    public void setCo(int co) {
        this.co = co;
    }

    public int getC6h6() {
        return c6h6;
    }

    public void setC6h6(int c6h6) {
        this.c6h6 = c6h6;
    }

    public int getO3() {
        return o3;
    }

    public void setO3(int o3) {
        this.o3 = o3;
    }
}

