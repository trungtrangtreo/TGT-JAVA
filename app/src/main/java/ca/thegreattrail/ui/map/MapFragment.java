package ca.thegreattrail.ui.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;

import butterknife.ButterKnife;
import ca.thegreattrail.R;
import ca.thegreattrail.data.model.db.TrailSegment;
import ca.thegreattrail.ui.base.HomeTabMapFragment;

public class MapFragment extends HomeTabMapFragment implements View.OnClickListener {

    private GoogleMap googleMap;
    private Polyline lastSelectedPolyline;
    private HashMap<Integer, Polyline> drawnPolylinesMap = new HashMap<>();

    private int land = -0xc75800; //  56-168-0
    private int water = -0xff6224;//    0, 157, 220
    private int gap = -0x1692e1;   //   233 , 109, 31
    private int PATTERN_DASH_LENGTH_PX = 40;
    private int PATTERN_GAP_LENGTH_PX = 20;
    private int TRAIL_LOW_RESOLUTION_DROPPED_POINTS_COUNT = 50;
    public static final int TRAIL_HIGH_RESOLUTION_MINIMUM_ZOOM_LEVEL = 10;
    private String TAG = MapFragment.class.getSimpleName();

    private final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DASH);

    protected int unSelectedPolylineWidth = 5;
    protected int selectedPolylineWidth = 15;
    public int selectedSegmentId = 0;
    private int lastSelectedSegmentId = 0;
    private int lastObjectIdMeasureTool = 0;

    private TrailSegment selectedTrail;

    public static HashMap<String, Fragment> mapfragStack = new HashMap<>();
    public static Stack<String> mapfragTagStack = new Stack<>();

    private TextView tvSuggest;
    private TextView tvTrailName;
    private TextView tvDistance;
    private RelativeLayout rlBottom;

    private FrameLayout searchLayout;

    public static final String TRAIL_ID = "trailId";
    public static final String TRAIL_NAME = "trail_name";
    public static final String OBJECT_ID = "object_id";

    public LatLng center;
    public float zoom;

    private static final int INITIAL_ZOOM_LEVEL = 13;
    private static final String EXTRA_CENTER_KEY = "center";
    private static final String EXTRA_ZOOM_KEY = "zoom";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        initView(view);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(EXTRA_CENTER_KEY)) {
                center = (LatLng) savedInstanceState.get(EXTRA_CENTER_KEY);
            }

            if (savedInstanceState.containsKey(EXTRA_ZOOM_KEY)) {
                zoom = savedInstanceState.getFloat(EXTRA_ZOOM_KEY);
            }
        }

        mMapView = Objects.requireNonNull(getView()).findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        resumeMapView(true);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(Objects.requireNonNull(getActivity()))
                    .addApi(LocationServices.API)
                    .build();
        }
        isViewCreated = true;
        loadUi();
    }

    private void initView(View view) {
        tvSuggest = view.findViewById(R.id.tvSuggest);
        tvTrailName = view.findViewById(R.id.tvTrailName);
        tvDistance = view.findViewById(R.id.tvDistance);
        rlBottom = view.findViewById(R.id.rlBottom);
        searchLayout = view.findViewById(R.id.searchLayout);
        rlBottom.setOnClickListener(this);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(EXTRA_CENTER_KEY, center);
        outState.putFloat(EXTRA_ZOOM_KEY, zoom);

        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    //    @Override
//    public void onMapReady(GoogleMap googleMap) {
//
//        this.googleMap = googleMap;
//
//        googleMap.setMyLocationEnabled(true);
//        googleMap.getUiSettings().setMyLocationButtonEnabled(true);
//
//        try {
//            // Customise the styling of the base map using a JSON object defined
//            // in a raw resource file.
//            boolean success = googleMap.setMapStyle(
//                    MapStyleOptions.loadRawResourceStyle(
//                            Objects.requireNonNull(getContext()), R.raw.style_json));
//
//            if (!success) {
//                Logger.e(TAG, "Style parsing failed.");
//            }
//        } catch (Resources.NotFoundException e) {
//            Logger.e(TAG, "Can't find style. Error: ", e);
//        }
//
//        if (MainActivity.listSegments == null) {
//            googleMap.setOnCameraIdleListener(null);
//        } else {
//            googleMap.setOnCameraIdleListener(getCameraChangeListener());
//        }
//
//        // Add a marker in Sydney and move the camera
//        LatLng canada = new LatLng(55.508930, -96.654281);
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(canada));
//
//        this.googleMap.setOnPolylineClickListener(polyLineClickListner());
//
//        drawGreatTrail();
//
//    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;
    }

    @Override
    protected void onMapReady() {
        super.onMapReady();
    }

    @Override
    public void onClick(View v) {
    }

    //    private void drawGreatTrail() {
//
//        if (MainActivity.listSegments != null) {
//
//            for (TrailSegmentLight segment : MainActivity.listSegments) {
//                drawTrailSegment(segment);
//            }
//        }
////        Logger.e("listSegments" + MainActivity.listSegments.size());
////        Logger.e("listPoints= " + MainActivity.listPoints.size());
//    }
//
//    private void drawTrailSegment(TrailSegmentLight segment) {
//
////        if (segment == null)
////            return
//
//        List<LatLng> points = MainActivity.listPoints.get(segment.objectId);
//
//        List<PatternItem> patternItems = null;
//
//        int segmentColor = findSegmentColor(segment);
//
//        if (segment.categoryCode == 5) {
//            patternItems = PATTERN_POLYLINE_DOTTED;
//        } else {
//            points = TrailUtility.compressedSegment(points, TRAIL_LOW_RESOLUTION_DROPPED_POINTS_COUNT);
//        }
//
//        Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(points));
//        polyline.setClickable(true);
//        polyline.setTag(segment.objectId);
//        stylePolyline(polyline, segmentColor, patternItems);
//        drawnPolylinesMap.put(segment.objectId, polyline);
//    }
//
//    private int findSegmentColor(TrailSegmentLight segment) {
//        int color = Constants.land;
//        int statusCode = segment.statusCode;
//        int categoryCode = segment.categoryCode;
//
//        if (statusCode == 1) {
//            if (categoryCode == 2) {
//                color = Constants.water; // water
//            } else {
//                color = Constants.land; // land
//            }
//        } else if (statusCode == 2) {
//            color = Constants.gap;   // gap
//        }
//
//        if (categoryCode == 5) {
//            color = Constants.water; // water
//        }
//
//        return color;
//    }
//
//    protected void onCameraIdle1() {
//        if (googleMap == null)
//            return;
//
//        float currentMapCameraZoom = googleMap.getCameraPosition().zoom;
//
//        if (currentMapCameraZoom < TRAIL_HIGH_RESOLUTION_MINIMUM_ZOOM_LEVEL)
//            return;
//        LatLngBounds bounds = googleMap.getProjection().getVisibleRegion().latLngBounds;
//        increaseTrailResolutionTask = new IncreaseTrailResolutionTask(bounds);
//        increaseTrailResolutionTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//    }
//
//    public GoogleMap.OnCameraIdleListener getCameraChangeListener() {
//        return new GoogleMap.OnCameraIdleListener() {
//
//            @Override
//            public void onCameraIdle() {
//                onCameraIdle1();
//
////              center = myMap.getCameraPosition().target;
////              zoom = myMap.getCameraPosition().zoom;
//            }
//        };
//    }
//
//    private void stylePolyline(Polyline polyline, int color, List<PatternItem> patternItems) {
//        // Get the data object stored with the polyline.
//        // Use a round cap at the start of the line.
//
//        polyline.setStartCap(new RoundCap());
//
//        polyline.setEndCap(new RoundCap());
//        polyline.setWidth(unSelectedPolylineWidth);
//        polyline.setColor(color);
//        polyline.setJointType(JointType.ROUND);
//        polyline.setPattern(patternItems);
//    }
//
//    private GoogleMap.OnPolylineClickListener polyLineClickListner() {
//        return new GoogleMap.OnPolylineClickListener() {
//            @Override
//            public void onPolylineClick(Polyline polyline) {
//                if (lastSelectedPolyline != null)
//                    lastSelectedPolyline.setWidth(unSelectedPolylineWidth);
//
//                lastSelectedPolyline = polyline;
//                polyline.setWidth(selectedPolylineWidth);
//
//                List<LatLng> points = polyline.getPoints();
//                selectedSegmentId = findSegmentIdWithPoints(points);
//
//                lastObjectIdMeasureTool = lastSelectedSegmentId;
//                lastSelectedSegmentId = selectedSegmentId;
//
//                ActivityDBHelperTrail db = ActivityDBHelperTrail.getInstance(getContext());
//                Cursor cursor = db.getSpecificSegments(selectedSegmentId);
//                if (cursor != null && cursor.moveToFirst()) {
//                    TrailSegment segment = new TrailSegment(cursor);
//                    selectedTrail = segment;
//
//                    showSegmentInfo(segment);
//                }
//                cursor.close();
//            }
//        };
//
//    }
//
//    protected void showSegmentInfo(TrailSegment segment) {
//        tvSuggest.setText("");
//        tvTrailName.setText(segment.getTrailName());
//        tvDistance.setText(segment.getSumLengthKm() + " km");
//    }
//
//    private int findSegmentIdWithPoints(List<LatLng> points) {
//
//        if (points == null || points.size() < 2)
//            return -1;
//
//        LatLng firstPoint = points.get(0);
//        LatLng lastPoint = points.get(points.size() - 1);
//
//        ArrayList<LatLng> segmentPoints;
//        for (int id : MainActivity.listPoints.keySet()) {
//            segmentPoints = MainActivity.listPoints.get(id);
//            if (segmentPoints.get(0).equals(firstPoint) &&
//                    segmentPoints.get(segmentPoints.size() - 1).equals(lastPoint))
//                return id;
//        }
//
//        return -1;                      // Not found
//    }
//
//    private List<TrailSegmentLight> visibleSegments(LatLngBounds bounds) {
//        LatLng northEast = bounds.northeast;
//        LatLng southWest = bounds.southwest;
//
//        double centerLatitude = southWest.latitude + (northEast.latitude - southWest.latitude) / 2;
//        double centerLongitude = southWest.longitude + (northEast.longitude - southWest.longitude) / 2;
//        LatLng center = new LatLng(centerLatitude, centerLongitude);
//
//        return TrailUtility.nearbySegments(center);
//    }
//
//
//    private class IncreaseTrailResolutionTask extends AsyncTask<Void, Void, List<Polyline>> {
//
//        private LatLngBounds bounds;
//
//        public IncreaseTrailResolutionTask(LatLngBounds bounds) {
//            this.bounds = bounds;
//        }
//
//        @Override
//        protected List<Polyline> doInBackground(Void... params) {
//            List<TrailSegmentLight> visibleSegments = visibleSegments(bounds);
//            List<Polyline> visiblePolylines = new ArrayList<>();
//
//            for (TrailSegmentLight segment : visibleSegments) {
//                Polyline polyline = drawnPolylinesMap.get(segment.objectId);
//                visiblePolylines.add(polyline);
//            }
//            return visiblePolylines;
//        }
//
//        @Override
//        protected void onPostExecute(List<Polyline> currentVisiblePolylines) {
//
//            if (currentVisiblePolylines == null)
//                return;
//
//            for (Polyline polyline : currentVisiblePolylines) {
//                Object polylineTag = polyline.getTag();
//
//                if (polylineTag == null)
//                    continue;
//                int segmentId = (int) polylineTag;
//                // TODO: to be refactored when remove this static references from main activity
//                ArrayList<LatLng> segmentAllPoints = MainActivity.listPoints.get(segmentId);
//
//                if (segmentAllPoints == null || segmentAllPoints.size() == 0)
//                    return;
//                int currentVisibleSegmentPointsNumber = polyline.getPoints().size();
//
//                if (currentVisibleSegmentPointsNumber < segmentAllPoints.size()) {
//                    // it is not zoomed in and not drawn using all points
//                    polyline.setPoints(segmentAllPoints);
//                }
//            }
//        }
//    }
//
//    @Override
//    public void onClick(View v) {
//        if (v.getId() == R.id.rlBottom) {
//            if (selectedTrail != null) {
//                getDetailTrail();
//            }
//        }
//    }
//
//    private void getDetailTrail() {
//        if (!NetworkUtils.isNetworkConnected(getContext())) {
//            //Toast.makeText(AreaSelectionActivity.this, "Check please your Internet connection", Toast.LENGTH_SHORT).show();
//            new AlertDialog.Builder(getActivity())
//                    .setTitle(R.string.alert_no_internet)
//                    .setMessage(R.string.alert_must_online_trail)
//                    .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                        public void onClick(DialogInterface dialog, int which) {
//                            dialog.dismiss();
//                        }
//                    })
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .show();
//            return;
//        }
////        pushMapFragmentToStack();
//
////        SegmentDetailsFragment segmentDetailsFragment = new SegmentDetailsFragment();
////        segmentDetailsFragment.setObjectId(lastSelectedSegmentId);
////        Bundle args = new Bundle();
////        args.putString("trailId", selectedTrail.getTrailId());
////        args.putString("trail_name", selectedTrail.getTrailName());
////        segmentDetailsFragment.setArguments(args);
////
////        replaceFragment(R.id.searchLayout, segmentDetailsFragment);
//
////        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
////        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
////        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//
//        Intent intent = new Intent(getContext(), DetailTrailActivity.class);
//        intent.putExtra(TRAIL_ID, selectedTrail.getTrailId());
//        intent.putExtra(TRAIL_NAME, selectedTrail.getTrailName());
//        intent.putExtra(OBJECT_ID, lastSelectedSegmentId);
//        startActivity(intent);
//    }
//
//    private void pushMapFragmentToStack() {
//        String segmentTag = "MapFragment";
//        MapFragment mapFragment = (MapFragment) MainActivity.fragment;
//
//        if (mapFragment == null)
//            return;
//
//        mapfragStack.put(segmentTag, mapFragment);
//        mapfragTagStack.push(segmentTag);
//
//    }
//
    public int getSelectedSegmentId() {
        return selectedSegmentId;
    }
//
//
//    public void replaceFragment(int resourceID, Fragment rFragment) {
//        if (rFragment != null) {
//            searchLayout.setVisibility(View.VISIBLE);
//            FragmentTransaction mFragmentTransaction = Objects.requireNonNull(getActivity()).getSupportFragmentManager().beginTransaction();
//            mFragmentTransaction
//                    .replace(resourceID, rFragment)
//                    .addToBackStack(null)
//                    .commit();
//        }
//
//    }


}
