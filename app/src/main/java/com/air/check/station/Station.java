package com.air.check.station;

/**
 * Created by Lukasz on 03.12.2017.
 */

public class Station {
    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Double latitude, longitude;

}
