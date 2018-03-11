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
        if (!getName().equals("")) builder.append("\n" + "Nazwa stacji: " + getName());
        //if (getPm25() != 0) builder.append("\n" + "PM2.5: " + getPm25() + "µg/m³");
        if (getPm10() != 0) builder.append("\n" + "PM10: " + getPm10() + "µg/m³");
        if (getSo2() != 0) builder.append("\n" + "SO2: " + getSo2() + "µg/m³");
        if (getNo2() != 0) builder.append("\n" + "NO2: " + getNo2() + "µg/m³");
        if (getCo() != 0) builder.append("\n" + "CO: " + getCo() + "µg/m³");
        if (getC6h6() != 0) builder.append("\n" + "C6H6: " + getC6h6() + "µg/m³");
        if (getO3() != 0) builder.append("\n" + "O3: " + getO3() + "µg/m³");
        if (getDistanceTo() != 0) builder.append("\n" + "Odleglość: " + getDistanceTo() + "m");
        return builder.toString();
    }

    public StationWios(){
        setLatitude(0.0);
        setLongitude(0.0);
        setStationId(0);
        setDistanceTo(Double.MAX_VALUE);
        setName("");
    }

    public StationWios FindStation(Double userLatitide, Double userLongtitude) throws ExecutionException, InterruptedException, JSONException {
        StationWios Station = new StationWios();
        String result = new JsonTask().execute("http://api.gios.gov.pl/pjp-api/rest/station/findAll").get();
        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); ++i) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            Double stationLatitude = jsonobject.getDouble("gegrLat");
            Double stationLongitude = jsonobject.getDouble("gegrLon");
            int stationId = jsonobject.getInt("id");
            if(Distance.calculate(userLatitide, stationLatitude, userLongtitude, stationLongitude) <= Station.getDistanceTo()){
                Station.setStationId(stationId);
                Station.setName(jsonobject.getString("stationName"));
                Station.setCityId(jsonobject.getJSONObject("city").getInt("id"));
                Station.setLatitude(stationLatitude);
                Station.setLongitude(stationLongitude);
                Station.setDistanceTo(Distance.calculate(userLatitide, Station.getLatitude(), userLongtitude, Station.getLongitude()));
            }
        }
        Station.Update();
        return Station;
    }

    public void Update() throws ExecutionException, InterruptedException, JSONException {
        String result = new JsonTask().execute("http://api.gios.gov.pl/pjp-api/rest/station/sensors/" + String.valueOf(getStationId())).get();
        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); ++i) {
            JSONObject stationIterator = jsonarray.getJSONObject(i);
            if(stationIterator.getJSONObject("param").optString("paramCode").equals("PM10")) {
                String test = new JsonTask().execute("http://api.gios.gov.pl/pjp-api/rest/data/getData/" + stationIterator.get("id")).get();
                JSONObject bla = new JSONObject(test);
                JSONArray xxx = bla.getJSONArray("values");
                setPm10((xxx.getJSONObject(2).optDouble("value")));
            }
            if(stationIterator.getJSONObject("param").optString("paramCode").equals("PM2.5")) {}
            if(stationIterator.getJSONObject("param").optString("paramCode").equals("NO2")) {}
            if(stationIterator.getJSONObject("param").optString("paramCode").equals("SO2")) {}
            if(stationIterator.getJSONObject("param").optString("paramCode").equals("O3")){}
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

