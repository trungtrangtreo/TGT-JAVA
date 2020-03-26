package ca.thegreattrail.ui.measure;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.LatLng;
import butterknife.ButterKnife;
import ca.thegreattrail.R;
import ca.thegreattrail.ui.base.HomeTabMapFragment;

public class MeasureFragment extends HomeTabMapFragment implements OnChartGestureListener, OnChartValueSelectedListener {

    private static final String EXTRA_CENTER_KEY = "center";
    private static final String EXTRA_ZOOM_KEY = "zoom";
    private static final String EXTRA_DELETE_MEASURE = "delete-measure";
    private static final String TAG = "MeasureFragment";
    private static final int TRAIL_MEASUREMENT_DROPPED_POINTS_COUNT = 25;
    private static final double LOW_ZOOM_LEVEL = 7.5;
    private static final double MID_ZOOM_LEVEL = 10;
    private static final double HIGH_ZOOM_LEVEL = 12.5;
    private static final double DROP_PIN_TOLERANCE_DISTANCE_COEFFICIENT = 100 * 1000;   // an emprical values means that zoom * tolerance = 100,000 meters

    public LatLng center;
    public float zoom;
    public boolean deleteMeasure;
    public SearchView searchView;
    public int isSearchOpened = 0;
    public FrameLayout measureSearchLayout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_measure, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {

            if (savedInstanceState.containsKey(EXTRA_DELETE_MEASURE)) {
                deleteMeasure = savedInstanceState.getBoolean(EXTRA_DELETE_MEASURE);
            }

            if (savedInstanceState.containsKey(EXTRA_CENTER_KEY)) {
                center = (LatLng) savedInstanceState.get(EXTRA_CENTER_KEY);
            }

            if (savedInstanceState.containsKey(EXTRA_ZOOM_KEY)) {
                zoom = savedInstanceState.getFloat(EXTRA_ZOOM_KEY);
            }
        }
        setUiValues(savedInstanceState, getView());
    }

    private void setUiValues(Bundle savedInstanceState, View view) {
        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
//        dropAnotherPinTv.setVisibility(View.GONE);

        resumeMapView(true);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .build();
        }
//
//        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                showMyFusedLocation();
//            }
//        });

//        upBtn = (ImageView) view.findViewById(R.id.upBtn);
//        upBtn.setImageResource(R.drawable.arrow_up);
//        measureSearchLayout = (FrameLayout) view.findViewById(R.id.measureSearchLayout);
//
//        upBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                if (layoutVisible == false) {
//
//                    lineChart.setVisibility(View.VISIBLE);
//
//
//                    layoutVisible = true;
//
//
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            getPixelValue(activity, 95)
//                    );
//                    params.setMargins(0, 0, 0, getPixelValue(activity, 15));
//                    layoutMeasure.setLayoutParams(params);
//                    upBtn.setImageResource(R.drawable.arrow_down);
//                } else {
//                    lineChart.setVisibility(View.GONE);
//
//                    layoutVisible = false;
//                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
//                            LinearLayout.LayoutParams.MATCH_PARENT,
//                            getPixelValue(activity, 95)
//                    );
//                    layoutMeasure.setLayoutParams(params);
//                    upBtn.setImageResource(R.drawable.arrow_up);
//                }
//
//            }
//        });
//
//        deleteBtn = (ImageView) view.findViewById(R.id.deleteBtn);
//
//        deleteBtn.setImageResource(R.drawable.trash);
//
//        deleteBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                DeleteConfirmationDialog();
//
//            }
//        });
//        distanceTxt = (TextView) view.findViewById(R.id.txtDistanceMeasure);
//        elevationTxt = (TextView) view.findViewById(R.id.txtElevationMeasure);
//        layoutMeasure = (LinearLayout) view.findViewById(R.id.layoutMeasure);
//        lineChart = (LineChart) view.findViewById(R.id.chart);

        isViewCreated = true;
        loadUi();
    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    @Override
    protected void onMapReady() {
        super.onMapReady();
    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;
//        myMap.getUiSettings().setRotateGesturesEnabled(false);

//        myMap.getUiSettings().setMapToolbarEnabled(false);
//        if (MainActivity.listSegments == null) {
//            myMap.setOnCameraIdleListener(null);
//        } else {
//            myMap.setOnCameraIdleListener(getCameraChangeListener3());
//        }
//      myMap.setOnMapClickListener(onMapClick());

        // Set OnMapLoadedCallback Listener.
//        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
//
//            @Override
//            public void onMapLoaded() {
//
////                askPermissionsAndShowMyLocation();
//
//            }
//        });
//        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//        myMap.getUiSettings().setZoomControlsEnabled(false);
//        if (Build.VERSION.SDK_INT >= 23) {
//            if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                return;
//            }
//        }
//        myMap.setMyLocationEnabled(true);
//        myMap.getUiSettings().setMyLocationButtonEnabled(false);

//        myMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//
//            @Override
//            public View getInfoWindow(Marker arg0) {
//                return null;
//            }
//
//            @Override
//            public View getInfoContents(Marker marker) {
//
//                Context context = getActivity(); //or getActivity(), YourActivity.this, etc.
//
//                LinearLayout info = new LinearLayout(context);
//                info.setOrientation(LinearLayout.VERTICAL);
//
//
//                TextView title = new TextView(context);
//                title.setTextColor(Color.BLACK);
//                title.setGravity(Gravity.CENTER);
//                title.setTypeface(null, Typeface.BOLD);
//                title.setText(marker.getTitle());
//
//                TextView snippet = new TextView(context);
//                snippet.setTextColor(Color.GRAY);
//                snippet.setText(marker.getSnippet());
//
//                info.addView(title);
//                info.addView(snippet);
//
//                return info;
//            }
//        });

//        myMap.setMyLocationEnabled(true);
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener3() {
        return new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                MeasureFragment.super.onCameraIdle();
                center = myMap.getCameraPosition().target;
                zoom = myMap.getCameraPosition().zoom;
            }
        };
    }

    /*Chart SDK*/
    @Override
    public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {

    }

    @Override
    public void onChartLongPressed(MotionEvent me) {

    }

    @Override
    public void onChartDoubleTapped(MotionEvent me) {

    }

    @Override
    public void onChartSingleTapped(MotionEvent me) {

    }

    @Override
    public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {

    }

    @Override
    public void onChartScale(MotionEvent me, float scaleX, float scaleY) {

    }

    @Override
    public void onChartTranslate(MotionEvent me, float dX, float dY) {

    }

    @Override
    public void onValueSelected(Entry e, Highlight h) {

    }

    @Override
    public void onNothingSelected() {

    }
}
