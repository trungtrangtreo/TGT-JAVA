package ca.thegreattrail.data.model.db;

/**
 * Created by houari on 30/11/2016.
 */

public class ElevationClass {
    private float elevation;
    private double latitude ;
    private double longitude;
    private float distance;

    public ElevationClass(float elevation, double latitude, double longitude, float distance ) {
        this.elevation = elevation ;
        this.latitude = latitude ;
        this.longitude = longitude ;
        this.distance = distance ;

    }

    public double getElevation() {
        return elevation;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getDistance() {
        return distance;
    }

    public void setElevation(float elevation) {
        this.elevation = elevation;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setResolution(float distance) {
        this.distance = distance;
    }
}
