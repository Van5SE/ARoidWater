
public class DeviceLocInfo {

    String latitude;
    String longtitude;
    String distance;
    String time;

    public DeviceLocInfo(String latitude, String longtitude, String distance, String time) {
        this.latitude = latitude;
        this.longtitude = longtitude;
        this.distance = distance;
        this.time = time;
        //最小限制距离为100m
        if(Double.parseDouble(distance)<100)
        {
            distance="100";
        }
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

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
