package ca.thegreattrail.ui.measure;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.Objects;
import butterknife.BindDimen;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.thegreattrail.R;
import ca.thegreattrail.data.model.db.ElevationClass;
import ca.thegreattrail.ui.base.HomeTabMapFragment;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.utlis.Constants;
import ca.thegreattrail.utlis.MyYAxisValueFormatter;
import ca.thegreattrail.utlis.TrailUtility;
import ca.thegreattrail.utlis.Utility;

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

    private LatLng startMarkerPoint;
    private LatLng endMarkerPoint;
    private boolean putFirstMarker = true;
    private boolean putSecondMarker = true;
    private boolean layoutVisible = false;
    private Marker startMarker, endMarker;
    private int TIMEOUT = 20000;

    @BindDimen(R.dimen.animated_camera_padding)
    int animatedCameraPadding;

    @BindView(R.id.drop_another_pin_tv)
    TextView dropAnotherPinTv;
    @BindView(R.id.drop_first_pin_tv)
    TextView dropFirstPinTv;
    private FloatingActionButton floatingActionButton;
    private ImageView upBtn;
    private ImageView deleteBtn;
    private TextView distanceTxt;
    private TextView elevationTxt;
    private LineChart lineChart;
    private LinearLayout layoutMeasure;

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
        dropAnotherPinTv.setVisibility(View.GONE);

        resumeMapView(true);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                    .addApi(LocationServices.API)
                    .build();
        }

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showMyFusedLocation();
            }
        });

        upBtn = (ImageView) view.findViewById(R.id.upBtn);
        upBtn.setImageResource(R.drawable.ic_arrow_measure_down);
        measureSearchLayout = (FrameLayout) view.findViewById(R.id.measureSearchLayout);

        upBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (layoutVisible == false) {

                    lineChart.setVisibility(View.VISIBLE);


                    layoutVisible = true;


                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getPixelValue(getActivity(), 95)
                    );
                    params.setMargins(0, 0, 0, getPixelValue(getActivity(), 15));
                    layoutMeasure.setLayoutParams(params);
                    upBtn.setImageResource(R.drawable.ic_arrow_measure_up);
                } else {
                    lineChart.setVisibility(View.GONE);

                    layoutVisible = false;
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            getPixelValue(getActivity(), 95)
                    );
                    layoutMeasure.setLayoutParams(params);
                    upBtn.setImageResource(R.drawable.ic_arrow_measure_down);
                }

            }
        });

        deleteBtn = (ImageView) view.findViewById(R.id.deleteBtn);

        deleteBtn.setImageResource(R.drawable.ic_delete_yellow);

        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteConfirmationDialog();
            }
        });
        distanceTxt = view.findViewById(R.id.txtDistanceMeasure);
        elevationTxt = view.findViewById(R.id.txtElevationMeasure);
        layoutMeasure = view.findViewById(R.id.layoutMeasure);
        lineChart = view.findViewById(R.id.chart);

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
        myMap.getUiSettings().setRotateGesturesEnabled(false);
        myMap.getUiSettings().setMapToolbarEnabled(false);

        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener3());
        }
        myMap.setOnMapClickListener(onMapClick());

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

        myMap.setMyLocationEnabled(true);
    }

    private boolean withinReasonableTolerance(double distance) {
        int divisor = 1;
        double zoomLevel = myMap.getCameraPosition().zoom;

        if (zoomLevel <= LOW_ZOOM_LEVEL) {
            divisor = 1;
        } else if (zoomLevel <= MID_ZOOM_LEVEL) {
            divisor = 2;
        } else if (zoomLevel <= HIGH_ZOOM_LEVEL) {
            divisor = 4;
        } else {
            divisor = 8;
        }

        return distance < DROP_PIN_TOLERANCE_DISTANCE_COEFFICIENT / zoomLevel / divisor;
    }

    public GoogleMap.OnMapClickListener onMapClick() {
        return new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                //FIXME should be check here, maker won't showing if distance too long
                if (startMarkerPoint == null && putFirstMarker && putSecondMarker) {
                    startMarkerPoint = Utility.nearestPoint(latLng);
                    if (startMarkerPoint != null
                            && withinReasonableTolerance(TrailUtility.distanceTo(startMarkerPoint, latLng))) {
                        startMarker = myMap.addMarker(new MarkerOptions()
                                .position(startMarkerPoint)
                                .icon(BitmapDescriptorFactory
                                        .fromResource(R.drawable.ic_marker_yellow)));
                        myMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                            @Override
                            public boolean onMarkerClick(Marker marker) {
                                return true;
                            }
                        });

                        dropFirstPinTv.setVisibility(View.GONE);
                        dropAnotherPinTv.setVisibility(View.VISIBLE);

                        putFirstMarker = false;
                    } else {
                        startMarkerPoint = null;
                        Toast.makeText(getActivity(), R.string.label_drop_pin_note, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    endMarkerPoint = Utility.nearestPoint(latLng);
                    if (endMarkerPoint == null
                            || !withinReasonableTolerance(TrailUtility.distanceTo(endMarkerPoint, latLng))) {
                        Toast.makeText(getActivity(), R.string.label_drop_pin_note, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (putSecondMarker) {
                        myMap.setOnMapClickListener(null);
                        endMarker = myMap.addMarker(new MarkerOptions()
                                .position(endMarkerPoint)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_marker_yellow)));
                        endMarker.hideInfoWindow();

                        dropFirstPinTv.setVisibility(View.GONE);
                        dropAnotherPinTv.setVisibility(View.GONE);

                        layoutMeasure.setVisibility(View.VISIBLE);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                LinearLayout.LayoutParams.MATCH_PARENT,
                                getPixelValue(getActivity(), 95));
                        layoutMeasure.setLayoutParams(params);

                        calculateDistanceFromAPI(startMarkerPoint, endMarkerPoint);

                        layoutMeasure.setVisibility(View.VISIBLE);

                        putFirstMarker = false;
                        putSecondMarker = false;
                        moveCameraToMeasuredArea();
                    }
                }
            }
        };
    }

    private void calculateDistanceFromAPI(final LatLng startDistance, final LatLng endDistance) {

        String toleranceEncodeUrl = "%7B%22distance%22:100.0,%22units%22:%22esriMeters%22%7D&" +
                "Stops=%7B%22geometryType%22:%22esriGeometryPoint%22,%22features%22:%5B%7B%22" +
                "geometry%22:%7B%22x%22:" + startDistance.longitude + ",%22y%22:" + startDistance.latitude + ",%22spatialReference%22:%7B%22wkid%22:" +
                "4326%7D%7D%7D,%7B%22geometry%22:%7B%22x%22:" + endDistance.longitude + ",%22y%22:" + endDistance.latitude + ",%22spatialReference%22" +
                ":%7B%22wkid%22:4326%7D%7D%7D%5D,%22sr%22:%7B%22wkid%22:4326%7D%7D";

        String urlAPI = "https://devmap.thegreattrail.ca/arcgis/rest/services/TCT/Tools/GPServer/GetRouteGen/execute?f=json&env:outSR=4326&Tolerance=" + toleranceEncodeUrl;

        RequestQueue queue = Volley.newRequestQueue(getActivity());  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.GET, urlAPI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("LocationService", " Reponse api calculateDistanceFromAPI ........................................................................  = " + response.toString());

                        try {

                            JSONObject jsonObj = new JSONObject(response);
                            JSONArray results = jsonObj.getJSONArray("results");
                            JSONObject value = results.getJSONObject(0).getJSONObject("value");
                            JSONArray features = value.getJSONArray("features");
                            JSONObject attributes = features.getJSONObject(0).getJSONObject("attributes");

                            double totalLength = attributes.getDouble("Total_Length");
                            String distance = String.format("%.2f", totalLength / 1000);
                            distanceTxt.setText(distance.toString() + " km");
                            JSONObject geometry = features.getJSONObject(0).getJSONObject("geometry");


                            ArrayList<LatLng> coordinatesList = new ArrayList<LatLng>();

                            for (int j = 0; j < geometry.getJSONArray("paths").length(); j++) {

                                JSONArray paths = geometry.getJSONArray("paths").getJSONArray(j);

                                for (int i = 0; i < paths.length(); i++) {
                                    JSONArray tempCoordinate = paths.getJSONArray(i);
                                    double laltitude = tempCoordinate.getDouble(1);
                                    double longitude = tempCoordinate.getDouble(0);
                                    LatLng coordinate = new LatLng(laltitude, longitude);
                                    coordinatesList.add(coordinate);
                                }

                            }

                            int coordinatesListSize = coordinatesList.size();
                            String pathString = coordinatesList.get(0).latitude + "," + coordinatesList.get(0).longitude;

                            if (coordinatesListSize > Constants.maxElevationCoordinatePairs) {
                                int bound = calculateBound(coordinatesListSize);

                                for (int i = 1; i < coordinatesListSize - 1; i = i + bound) {
                                    pathString += "|" + coordinatesList.get(i).latitude + "," + coordinatesList.get(i).longitude;
                                }

                            } else {
                                for (int i = 1; i < coordinatesListSize - 1; i++) {
                                    pathString += "|" + coordinatesList.get(i).latitude + "," + coordinatesList.get(i).longitude;
                                }
                            }

                            pathString += "|" + coordinatesList.get(coordinatesListSize - 1).latitude + "," + coordinatesList.get(coordinatesListSize - 1).longitude;


                            int samples = calculateSamples(coordinatesList.size());


                            calculateElevationFromAPI(pathString, samples, totalLength);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
//                        Log.d("Error.Response", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();
                    }
                }
        );

        postRequest.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(postRequest);
    }

    private void calculateElevationFromAPI(String path, int samples, final double totalDistance) {

        Log.i("LocationService", " Reponse api ........................................................................ path = " + path.toString());

        String urlAPI = "https://maps.googleapis.com/maps/api/elevation/json?path=" + path + "&samples=" + samples + "&key=" + "AIzaSyDZL37pIbCDiGjHdPQv2pWNQKOIunX8WWA";  // "AIzaSyDZL37pIbCDiGjHdPQv2pWNQKOIunX8WWA"

        int tempLenght = urlAPI.length();

        RequestQueue queue = Volley.newRequestQueue(getActivity());  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.GET, urlAPI,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        Log.i("LocationService", " Reponse api calculateElevationFromAPI  ........................................................................  = " + response.toString());


                        ArrayList<ElevationClass> elevationsList = new ArrayList<ElevationClass>();

                        try {

                            JSONObject jsonObj = new JSONObject(response);

                            JSONArray results = jsonObj.getJSONArray("results");

                            float boundInMeter = (float) totalDistance / (results.length() - 1);

                            float distance = 0;

                            for (int i = 0; i < results.length(); i++) {

                                double elevation = results.getJSONObject(i).getDouble("elevation");
                                // double  resolution = results.getJSONObject(i).getDouble("resolution");
                                JSONObject location = results.getJSONObject(i).getJSONObject("location");
                                double latitude = location.getDouble("lat");
                                double longitude = location.getDouble("lng");

                                distance = i * boundInMeter;


                                ElevationClass elevationClass = new ElevationClass((float) elevation, latitude, longitude, distance);
                                elevationsList.add(elevationClass);
                            }

                            double elevationDisplayed = 0;
                            for (int i = 0; i < elevationsList.size() - 1; i++) {
                                if (elevationsList.get(i).getElevation() < elevationsList.get(i + 1).getElevation()) {
                                    elevationDisplayed = elevationDisplayed + elevationsList.get(i + 1).getElevation() - elevationsList.get(i).getElevation();
                                }
                            }
                            String distanceString = String.format("%.2f", elevationDisplayed);

                            elevationTxt.setText(distanceString + " m");

                            ArrayList<Entry> listPointsDiagramme = new ArrayList<Entry>();


                            for (int i = 0; i < elevationsList.size(); i++) {
                                listPointsDiagramme.add(new Entry((float) elevationsList.get(i).getDistance() / 1000, (float) elevationsList.get(i).getElevation()));
                            }

                            drawChart(listPointsDiagramme);


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                       /* Log.d("Error.Response", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();*/
                    }
                }
        );
        queue.add(postRequest);
    }

    private void deleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setTitle(R.string.alert_clear_measurement_title);
        builder.setMessage(R.string.alert_clear_measurement_message);

        builder.setPositiveButton(getResources().getText(R.string.yes), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {

                putFirstMarker = true;
                putSecondMarker = true;

                startMarkerPoint = null;
                endMarkerPoint = null;


                if (startMarker != null) {
                    startMarker.remove();
                }

                if (endMarker != null) {
                    endMarker.remove();
                }

                myMap.setOnMapClickListener(onMapClick());
                layoutVisible = false;
                layoutMeasure.setVisibility(View.GONE);
                lineChart.setVisibility(View.GONE);
                dropFirstPinTv.setVisibility(View.VISIBLE);
                dropAnotherPinTv.setVisibility(View.GONE);
                upBtn.setImageResource(R.drawable.ic_arrow_measure_down);

                distanceTxt.setText("0 Km");
                elevationTxt.setText("0 m");
                deleteMeasure = true;

                dialog.dismiss();
            }
        });

        builder.setNegativeButton(getResources().getText(R.string.no), new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {

                // Do nothing
                dialog.dismiss();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    private void drawChart(ArrayList<Entry> listPointsDiagramme) {


        lineChart.setOnChartGestureListener(this);
        lineChart.setOnChartValueSelectedListener(this);
        lineChart.setDrawGridBackground(false);
        lineChart.setBackgroundColor(Color.rgb(55, 100, 125));
        Description desc = new Description();
        desc.setText("");
        lineChart.setDescription(desc);

        // add data
        setData(listPointsDiagramme);


        Legend legend = lineChart.getLegend();
        legend.setEnabled(false);

        lineChart.setTouchEnabled(true);

        // enable scaling and dragging
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(true);


        lineChart.setExtraBottomOffset(dpToPx(4));

        lineChart.setClipToPadding(true);

        YAxis leftAxis = lineChart.getAxisLeft();
        leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines

        leftAxis.enableGridDashedLine(10f, 10f, 0f);
        leftAxis.setDrawZeroLine(false);

        // limit lines are drawn behind data (and not on top)
        leftAxis.setDrawLimitLinesBehindData(true);

        lineChart.getAxisRight().setEnabled(false);


        //********************************

        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(11f);
        xAxis.setTextColor(Color.YELLOW);
        xAxis.setDrawAxisLine(true);
        xAxis.setDrawGridLines(false);  //MyYAxisValueFormatter
        // set a custom value formatter
        xAxis.setValueFormatter(new MyYAxisValueFormatter(true));


        YAxis yAxis = lineChart.getAxisLeft();
        yAxis.setDrawAxisLine(true);
        yAxis.setDrawGridLines(false);
        yAxis.setTextSize(11f); // set the text size
        yAxis.setTextColor(Color.YELLOW);
        yAxis.setValueFormatter(new MyYAxisValueFormatter(false));

        //  dont forget to refresh the drawing
        lineChart.invalidate();
    }

    private void setData(ArrayList<Entry> listPointsDiagramme) {
        ArrayList<Entry> yVals = listPointsDiagramme; // setYAxisValues();

        LineDataSet set1;

        // create a dataset and give it a type
        set1 = new LineDataSet(yVals, "Distance/Elevation");

        set1.setFillColor(Color.rgb(55, 100, 125));


        set1.setColor(Color.YELLOW);
        set1.setCircleColor(Color.YELLOW);
        set1.setLineWidth(3f);
        set1.setCircleRadius(7f);
//        set1.setCircleColorHole(Color.LTGRAY);
        set1.setCircleHoleRadius(4f);
        set1.setDrawCircleHole(true);
        set1.setValueTextSize(5f);
        set1.setValueTextColor(Color.YELLOW);
        set1.setColors(Color.YELLOW);
        set1.setDrawFilled(true);


        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(set1); // add the datasets

        // create a data object with the datasets
        LineData data = new LineData(dataSets);

        // set data
        lineChart.setData(data);

    }

    public int dpToPx(float dp) {
        Resources resources = Objects.requireNonNull(getActivity()).getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }

    private int calculateSamples(int size) {
        int sample = size / 10;
        if (sample <= Constants.minElevationSamples) {
            sample = Constants.minElevationSamples;
        } else if (sample > Constants.maxElevationSamples) {
            sample = Constants.maxElevationSamples;

        }


        return sample;
    }

    private int calculateBound(int size) {
        int bound = 1;
        if (size > Constants.maxElevationCoordinatePairs) {
            bound = size / Constants.maxElevationCoordinatePairs;
            if (size % Constants.maxElevationCoordinatePairs != 0) {
                bound++;
            }
        }

        return bound;
    }


    public void moveCameraToMeasuredArea() {
        if (startMarker == null || endMarker == null) {
            return;
        }

        LatLngBounds measuredBounds = new LatLngBounds.Builder()
                .include(startMarker.getPosition())
                .include(endMarker.getPosition())
                .build();

        myMap.animateCamera(CameraUpdateFactory.newLatLngBounds(measuredBounds, animatedCameraPadding));
    }

    public static int getPixelValue(Context context, int dimenId) {
        Resources resources = context.getResources();
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dimenId,
                resources.getDisplayMetrics()
        );
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
