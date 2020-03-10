package ca.thegreattrail.data.model.db;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dev1 on 3/2/2017.
 */

public class TrailWarning {

    private String message;
    private String location;
    private LatLng geometry;

    public TrailWarning(String message, String location, LatLng geometry) {
        this.message = message;
        this.location = location;
        this.geometry = geometry;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public LatLng getGeometry() {
        return geometry;
    }

    public void setGeometry(LatLng geometry) {
        this.geometry = geometry;
    }
}
