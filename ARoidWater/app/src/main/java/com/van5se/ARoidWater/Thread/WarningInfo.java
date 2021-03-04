package com.van5se.ARoidWater.Thread;

public class WarningInfo {

    String latitude;
    String longtitude;
    String warntype;
    String time;
    String deep;
    String area;


    public WarningInfo(String latitude, String longtitude, String warntype, String time ,String deep,String area) {
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.warntype = warntype;
        this.time = time;
        this.deep=deep;
        this.area=area;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public String getWarntype() {
        return warntype;
    }

    public void setWarntype(String warntype) {
        this.warntype = warntype;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getDeep() {
        return deep;
    }

    public void setDeep(String deep) {
        this.deep = deep;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }
}
