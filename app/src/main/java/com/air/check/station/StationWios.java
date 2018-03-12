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
    private int cityId;
    private int stationId;
    private int index;
    private Double distanceTo;
    private Double pm10;
    private Double pm25;
    private Double so2;
    private Double no2;
    private Double co;
    private Double c6h6;
    private Double o3;
    private String name;

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (getStationId() != 0) builder.append("Numer stacji WIOŚ: " + getStationId());
        if (!getName().equals("")) builder.append("\n" + "Nazwa stacji: " + getName());
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

    public StationWios(){
        setPm10(0.0);
        setPm25(0.0);
        setNo2(0.0);
        setSo2(0.0);
        setO3(0.0);
        setCo(0.0);
        setC6h6(0.0);
        setLatitude(0.0);
        setLongitude(0.0);
        setStationId(0);
        setDistanceTo(Double.MAX_VALUE);
        setName("");
    }

    public StationWios FindStation(Double userLatitide, Double userLongtitude) throws ExecutionException, InterruptedException, JSONException {
        String result = new JsonTask().execute("http://api.gios.gov.pl/pjp-api/rest/station/findAll").get();
        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); ++i) {
            JSONObject jsonobject = jsonarray.getJSONObject(i);
            Double stationLatitude = jsonobject.getDouble("gegrLat");
            Double stationLongitude = jsonobject.getDouble("gegrLon");
            int stationId = jsonobject.getInt("id");
            if(Distance.calculate(userLatitide, stationLatitude, userLongtitude, stationLongitude) <= getDistanceTo()){
                setStationId(stationId);
                setName(jsonobject.getString("stationName"));
                setCityId(jsonobject.getJSONObject("city").getInt("id"));
                setLatitude(stationLatitude);
                setLongitude(stationLongitude);
                setDistanceTo(Distance.calculate(userLatitide, getLatitude(), userLongtitude, getLongitude()));
            }
        }
        Update();
        return this;
    }

    public void Update() throws ExecutionException, InterruptedException, JSONException{
        String result = new JsonTask().execute("http://api.gios.gov.pl/pjp-api/rest/station/sensors/" + String.valueOf(getStationId())).get();
        JSONArray jsonarray = new JSONArray(result);
        for (int i = 0; i < jsonarray.length(); ++i) {
            JSONObject stationIterator = jsonarray.getJSONObject(i);
            String test = new JsonTask().execute("http://api.gios.gov.pl/pjp-api/rest/data/getData/" + stationIterator.get("id")).get();
            JSONObject bla = new JSONObject(test);
            JSONArray values = bla.getJSONArray("values");
            String key = bla.optString("key");
            try {
                extractValue(values, key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        roundPm();
        roundDistanceTo();
    }

    private double parseValue(JSONArray param) throws JSONException{
        for(int i = 0; i < param.length(); ++i){
            if(param.optJSONObject(i) != null){
                return (hasDoubleValue(param.optJSONObject(i), "value"));
            }
        }
        return 0.0;
    }

    private void extractValue(JSONArray param, String value) throws Exception{
        if(value.equals("PM10"))
            setPm10(parseValue(param));
        if(value.equals("PM25"))
            setPm25(parseValue(param));
        if(value.equals("NO2"))
            setNo2(parseValue(param));
        if(value.equals("SO2"))
            setSo2(parseValue(param));
        if(value.equals("O3"))
            setO3(parseValue(param));
        if(value.equals("C6H6"))
            setC6h6(parseValue(param));
        if(value.equals("Co"))
            setCo(parseValue(param));
    }

    private void roundPm(){
        setPm10(Math.round(getPm10() * 100.0) / 100.0);
        setPm25(Math.round(getPm25() * 100.0) / 100.0);
    }

    void roundDistanceTo(){
        setDistanceTo((double)Math.round(getDistanceTo()) * 100 / 100);
    }

    public int getCityId() {
        return cityId;
    }

    public void setCityId(int cityId) {
        this.cityId = cityId;
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

    public Double getDistanceTo() {
        return distanceTo;
    }

    public void setDistanceTo(Double distanceTo) {
        this.distanceTo = distanceTo;
    }

    public Double getPm10() {
        return pm10;
    }

    public void setPm10(Double pm10) {
        this.pm10 = pm10;
    }

    public Double getPm25() {
        return pm25;
    }

    public void setPm25(Double pm25) {
        this.pm25 = pm25;
    }

    public Double getSo2() {
        return so2;
    }

    public void setSo2(Double so2) {
        this.so2 = so2;
    }

    public Double getNo2() {
        return no2;
    }

    public void setNo2(Double no2) {
        this.no2 = no2;
    }

    public Double getCo() {
        return co;
    }

    public void setCo(Double co) {
        this.co = co;
    }

    public Double getC6h6() {
        return c6h6;
    }

    public void setC6h6(Double c6h6) {
        this.c6h6 = c6h6;
    }

    public Double getO3() {
        return o3;
    }

    public void setO3(Double o3) {
        this.o3 = o3;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}