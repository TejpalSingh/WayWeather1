package com.oakland.wayweather;

/**
 * Created by waheguru on 21/01/17.
 */

public class Waypoint
{

    Double Latitude;
    Double Longitude;
    int time;

    public Double getLongitude() {
        return Longitude;
    }

    public void setLongitude(Double longitude) {
        Longitude = longitude;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    public Double getLatitude() {
        return Latitude;
    }

    public void setLatitude(Double latitude) {
        Latitude = latitude;
    }
}
