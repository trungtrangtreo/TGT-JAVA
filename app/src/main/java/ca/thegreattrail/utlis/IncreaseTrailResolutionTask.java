package ca.thegreattrail.utlis;

import android.os.AsyncTask;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polyline;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import ca.thegreattrail.data.model.db.TrailSegmentLight;
import ca.thegreattrail.ui.main.MainActivity;

public class IncreaseTrailResolutionTask extends AsyncTask<Void, Void, List<Polyline>> {
    private LatLngBounds bounds;
    private HashMap<Integer, Polyline> drawnPolylinesMap;

    public IncreaseTrailResolutionTask(LatLngBounds bounds) {
        this.bounds = bounds;
        drawnPolylinesMap = new HashMap<>();
    }

    @Override
    protected List<Polyline> doInBackground(Void... params) {
        List<TrailSegmentLight> visibleSegments = visibleSegments(bounds);
        List<Polyline> visiblePolylines = new ArrayList<>();

        for (TrailSegmentLight segment : visibleSegments) {
            Polyline polyline = drawnPolylinesMap.get(segment.objectId);
            visiblePolylines.add(polyline);
        }
        return visiblePolylines;
    }

    @Override
    protected void onPostExecute(List<Polyline> currentVisiblePolylines) {

        if (currentVisiblePolylines == null)
            return;

        for (Polyline polyline : currentVisiblePolylines) {
            Object polylineTag = polyline.getTag();

            if (polylineTag == null)
                continue;
            int segmentId = (int) polylineTag;
            // TODO: to be refactored when remove this static references from main activity
            ArrayList<LatLng> segmentAllPoints = MainActivity.listPoints.get(segmentId);

            if (segmentAllPoints == null || segmentAllPoints.size() == 0)
                return;
            int currentVisibleSegmentPointsNumber = polyline.getPoints().size();

            if (currentVisibleSegmentPointsNumber < segmentAllPoints.size()) {
                // it is not zoomed in and not drawn using all points
                polyline.setPoints(segmentAllPoints);
            }
        }
    }

    private List<TrailSegmentLight> visibleSegments(LatLngBounds bounds) {
        LatLng northEast = bounds.northeast;
        LatLng southWest = bounds.southwest;

        double centerLatitude = southWest.latitude + (northEast.latitude - southWest.latitude) / 2;
        double centerLongitude = southWest.longitude + (northEast.longitude - southWest.longitude) / 2;
        LatLng center = new LatLng(centerLatitude, centerLongitude);

        return TrailUtility.nearbySegments(center);
    }
}

