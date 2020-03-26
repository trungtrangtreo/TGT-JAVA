package ca.thegreattrail.ui.base;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import androidx.annotation.CallSuper;
import androidx.fragment.app.Fragment;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.orhanobut.logger.Logger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import butterknife.BindDimen;
import ca.thegreattrail.R;
import ca.thegreattrail.data.model.db.TrailSegmentLight;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.utlis.Constants;
import ca.thegreattrail.utlis.TrailUtility;

public abstract class BaseTrailDrawingFragment extends Fragment {

    public static final int TRAIL_HIGH_RESOLUTION_MINIMUM_ZOOM_LEVEL = 10;

    private static final int PATTERN_DASH_LENGTH_PX = 40;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final int TRAIL_LOW_RESOLUTION_DROPPED_POINTS_COUNT = 50;
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DASH);

    public GoogleMap myMap;
    public boolean mapChanged;

    protected MapView mMapView;
    protected GoogleApiClient mGoogleApiClient;
    // Butterknife must be bound in child fragment.
    @BindDimen(R.dimen.fragment_map_unselected_polyline_width)
    protected int unSelectedPolylineWidth;
    @BindDimen(R.dimen.fragment_map_selected_polyline_width)
    protected int selectedPolylineWidth;

    private HashMap<Integer, Polyline> drawnPolylinesMap;
    private IncreaseTrailResolutionTask increaseTrailResolutionTask;

    public BaseTrailDrawingFragment() {
        drawnPolylinesMap = new HashMap<>();
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (increaseTrailResolutionTask != null && increaseTrailResolutionTask.getStatus() != AsyncTask.Status.FINISHED)
            increaseTrailResolutionTask.cancel(true);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        resumeMapView(isVisibleToUser);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    public void resumeMapView(boolean visible) {
        if (mMapView == null)
            return;

        if (visible) {
            mMapView.onResume();
        } else {
            mMapView.onPause();
        }
    }

    @CallSuper
    protected void onCameraIdle() {
        if (myMap == null)
            return;

        float currentMapCameraZoom = myMap.getCameraPosition().zoom;

        if (currentMapCameraZoom < TRAIL_HIGH_RESOLUTION_MINIMUM_ZOOM_LEVEL)
            return;
        LatLngBounds bounds = myMap.getProjection().getVisibleRegion().latLngBounds;
        increaseTrailResolutionTask = new IncreaseTrailResolutionTask(bounds);
        increaseTrailResolutionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    protected abstract boolean hasClickableSegments();    // used to enable/disable OnPolylineClickListener

    abstract protected void initializeMap(GoogleMap googleMap);

    @CallSuper
    protected void onMapReady() {
        drawGreatTrail();
        addTrailWarnings();
    }

    protected void setupMapView() {
        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                initializeMap(googleMap);
                BaseTrailDrawingFragment.this.onMapReady();
            }
        });
    }

    private void addTrailWarnings() {

        if (MainActivity.trailWarnings != null) {
            for (int i = 0; i < MainActivity.trailWarnings.size(); i++) {
                myMap.addMarker(new MarkerOptions()
                        .position(MainActivity.trailWarnings.get(i).getGeometry())
                        .title(MainActivity.trailWarnings.get(i).getLocation())
                        .snippet(MainActivity.trailWarnings.get(i).getMessage())
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.warning_marker)));
            }
        }

    }

    private void drawGreatTrail() {
        if (this.myMap == null || MainActivity.listSegments == null)
            return;

        mapChanged = true;
        int count = 0;
        for (TrailSegmentLight segment : MainActivity.listSegments) {
//            if (segment.trailId.equals("0084")) {
//                count++;
//            }
            drawTrailSegment(segment);
        }

        Logger.e("listSegments" + MainActivity.listSegments.size() + "   count = " + count);
        Logger.e("listPoints= " + MainActivity.listPoints.size() + "   count = " + count);
    }

    protected void highLightSelectedPolyline(int segmentId) {
        Polyline polyline = drawnPolylinesMap.get(segmentId);
        polyline.setWidth(selectedPolylineWidth);
    }

    private void drawTrailSegment(TrailSegmentLight segment) {
        if (segment == null)
            return;

        List<LatLng> points = MainActivity.listPoints.get(segment.objectId);

        if (points == null)
            return;

        int segmentColor = findSegmentColor(segment);
        List<PatternItem> patternItems;

        if (segment.categoryCode == 5) {
            patternItems = PATTERN_POLYLINE_DOTTED;
        } else {
            patternItems = null;
            points = TrailUtility.compressedSegment(points, TRAIL_LOW_RESOLUTION_DROPPED_POINTS_COUNT);
        }
        Polyline polyline = myMap.addPolyline(new PolylineOptions().addAll(points));
        polyline.setTag(segment.objectId);
        stylePolyline(polyline, segmentColor, patternItems);
        drawnPolylinesMap.put(segment.objectId, polyline);
    }

    private int findSegmentColor(TrailSegmentLight segment) {
        int color = Constants.land;
        int statusCode = segment.statusCode;
        int categoryCode = segment.categoryCode;

        if (statusCode == 1) {
            if (categoryCode == 2) {
                color = Constants.water; // water
            } else {
                color = Constants.land; // land
            }
        } else if (statusCode == 2) {
            color = Constants.gap;   // gap
        }

        if (categoryCode == 5) {
            color = Constants.water; // water
        }

        return color;
    }

    private void stylePolyline(Polyline polyline, int color, List<PatternItem> patternItems) {
        // Get the data object stored with the polyline.
        // Use a round cap at the start of the line.

        polyline.setStartCap(new RoundCap());

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(unSelectedPolylineWidth);
        polyline.setColor(color);
        polyline.setJointType(JointType.ROUND);
        polyline.setClickable(hasClickableSegments());
        polyline.setPattern(patternItems);
    }

    private List<TrailSegmentLight> visibleSegments(LatLngBounds bounds) {
        LatLng northEast = bounds.northeast;
        LatLng southWest = bounds.southwest;

        double centerLatitude = southWest.latitude + (northEast.latitude - southWest.latitude) / 2;
        double centerLongitude = southWest.longitude + (northEast.longitude - southWest.longitude) / 2;
        LatLng center = new LatLng(centerLatitude, centerLongitude);

        return TrailUtility.nearbySegments(center);
    }

    @SuppressLint("StaticFieldLeak")
    private class IncreaseTrailResolutionTask extends AsyncTask<Void, Void, List<Polyline>> {

        private LatLngBounds bounds;

        public IncreaseTrailResolutionTask(LatLngBounds bounds) {
            this.bounds = bounds;
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
    }

}