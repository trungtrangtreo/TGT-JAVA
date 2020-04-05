package ca.thegreattrail.data.model.other;

import com.google.android.gms.maps.model.LatLng;

public class PointState {

    private LatLng point;
    private String state;

    public PointState(LatLng point, String state ) {
        this.point = point;
        this.state = state;
    }

    public LatLng getPoint(){
        return point;
    }

    public String getState(){
        return state;
    }

    public void setPoint(LatLng point) {
        this.point = point;
    }

}


