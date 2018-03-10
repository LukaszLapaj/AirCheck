package com.air.check.station;

import com.air.check.utils.Distance;

/**
 * Created by Lukasz on 03.12.2017.
 */

public class Station {
    private Double latitude;
    private Double longitude;

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

    public String distanceTo(Station s) {
        return "Odleglosc miedzy stacjami: " + Math.round(Distance.calculate(s.getLatitude(), this.getLatitude(), s.getLongitude(), this.getLongitude()) * 100) / 100 + "\n";
    }
}
