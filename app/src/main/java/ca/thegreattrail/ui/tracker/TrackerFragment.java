package ca.thegreattrail.ui.tracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Layout;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Stack;
import butterknife.BindView;
import butterknife.ButterKnife;
import ca.thegreattrail.MvvmApp;
import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelper;
import ca.thegreattrail.data.model.db.ElevationClass;
import ca.thegreattrail.data.model.other.PointState;
import ca.thegreattrail.data.service.TrackService;
import ca.thegreattrail.ui.base.HomeTabMapFragment;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.ui.trackerlog.LogTrackerAcitivty;
import ca.thegreattrail.utlis.Constants;
import static android.content.Context.LOCATION_SERVICE;

public class TrackerFragment extends HomeTabMapFragment implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {


    public static final int MAP_VIEW_CAMERA_STREET_ZOOM_LEVEL = 16;

    private final static String TAG = "ActivityTrackerFragment";

    public static ArrayList<LatLng> coordinatesList = new ArrayList<LatLng>();
    public static HashMap<String, Fragment> trackerfragStack = new HashMap<String, Fragment>();
    public static Stack<String> trackerfragTagStack = new Stack<String>();

    public SearchView searchView;
    public int isSearchOpened = 0;
    @BindView(R.id.trackerSearchLayout)
    public FrameLayout trackerSearchLayout;

    @BindView(R.id.txtDistance)
    TextView txtDistance;
    @BindView(R.id.txtElevation)
    TextView txtElevation;
    @BindView(R.id.startBtn)
    Button startBtn;
    @BindView(R.id.ivPlay)
    ImageView ivPlay;
    @BindView(R.id.activityBtn)
    Button activityBtn;
    @BindView(R.id.chronometer)
    Chronometer chrono;

    @BindView(R.id.ivSave)
    ImageView ivSave;

    @BindView(R.id.ivDelete)
    ImageView ivDelete;

    MenuItem searchItem;
    ReceiverManager receiverManager;
    double diffElevation = 0;
    Menu menu;

    private long distance = 0;
    private String state = "Stop";
    private boolean serviceIsStarted = false;
    private double firstAltitude = Double.MIN_VALUE;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private BroadcastReceiver mReceiver;
    private LatLng latLngOld = new LatLng(0, 0);
    private long activityId = 0;
    private int colorLine = Color.BLACK;
    private Boolean displayMenu = false;
    private long timeWhenStopped = 0;
    private Tracker mTracker;
    private long timeWhenFragmentPaused;
    private List<Polyline> activityTrackingPolylines;
    private float prevElevation = Integer.MAX_VALUE;
    private boolean isPlay = true;

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
        MvvmApp application = (MvvmApp) Objects.requireNonNull(getActivity()).getApplication();
        mTracker = application.getDefaultTracker();
        activityTrackingPolylines = new ArrayList<>();
        // [END shared_tracker]
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tracker, container, false);
        ButterKnife.bind(this, view);

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        resumeMapView(true);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

//        floatingActionButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (Build.VERSION.SDK_INT >= 23) {
//                    int accessCoarsePermission
//                            = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
//                    int accessFinePermission
//                            = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);
//
//                    if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
//                            || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
//                        // The Permissions to ask user.
//                        String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                                android.Manifest.permission.ACCESS_FINE_LOCATION};
//                        // Show a dialog asking the user to allow the above permissions.
//                        ActivityCompat.requestPermissions(activity, permissions,
//                                REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
//
//                        return;
//                    }
//                }
//
//                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
//
//                if (mLastLocation != null) {
//
//                    LatLng firstPoint = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//                    LatLng secondePoint = Utility.nearestPoint(firstPoint);
//                    if (secondePoint == null) {
//                        return;
//                    }
//                    LatLngBounds.Builder builder = new LatLngBounds.Builder();
//
//                    builder.include(firstPoint);
//                    builder.include(secondePoint);
//
//                    LatLngBounds bounds = builder.build();
//
//                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, mMapView.getWidth(), mMapView.getHeight(), mMapView.getWidth() * 15 / 100);
//                    myMap.moveCamera(cu);
//                    // float zoom = myMap.getCameraPosition().zoom - 0.5f;
//                    cu = CameraUpdateFactory.newLatLngZoom(firstPoint, MAP_VIEW_CAMERA_STREET_ZOOM_LEVEL);     //newLatLngBounds(bounds, mMapView.getWidth(), mMapView.getHeight(), mMapView.getWidth()*45 / 100);
//                    myMap.moveCamera(cu);
//                }
//
//
//            }
//        });


        receiverManager = new ReceiverManager(getContext());
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        editor = preferences.edit();

        if (preferences.contains("WriteOnBD") && preferences.contains("State") && preferences.contains("Service") && preferences.contains("Time")) {

            serviceIsStarted = preferences.getBoolean("Service", false);
            state = preferences.getString("State", "Stop");
            long startTime = preferences.getLong("Time", SystemClock.elapsedRealtime());
            diffElevation = preferences.getFloat("elevation", 0);
            String elevationString = String.format("%.2f", diffElevation);
            txtElevation.setText(elevationString + " m");

            TrackingManager trackingManager = TrackingManager.getInstance();
            trackingManager.loadTrackingInfo(getActivity());
            trackingManager.startTrackingListeners(getActivity());

            if (serviceIsStarted) {
                startBtn.setText(R.string.label_menu); //  "Menu"
                displayMenu = true;

                if (state.equals("Run")) {
                    chrono.setBase(startTime);
                    timeWhenStopped = 0;
                    chrono.start();

                    Intent intent = new Intent(getActivity(), TrackService.class);
                    getContext().startService(intent);

                    colorLine = Color.BLACK;
                } else if (state.equals("Pause")) {
                    colorLine = Color.GRAY;
                } else if (state.equals("Offline")) {
                    colorLine = Color.RED;
                }


            } else {

                editor.putBoolean("WriteOnBD", false); // value to store
                editor.putString("State", "Stop"); // value to store
                editor.putBoolean("Service", false); // value to store
                editor.putLong("Time", SystemClock.elapsedRealtime()); // value to store
                editor.putFloat("elevation", 0);
                editor.commit();

                timeWhenStopped = 0;
                chrono.setText("00:00:00");

                state = "Stop";
            }

        } else {
            editor.putBoolean("WriteOnBD", false); // value to store
            editor.putString("State", "Stop"); // value to store
            editor.putBoolean("Service", false); // value to store
            editor.putLong("Time", SystemClock.elapsedRealtime()); // value to store
            editor.putFloat("elevation", 0);
            editor.commit();

            timeWhenStopped = 0;
            chrono.setText("00:00:00");

            state = "Stop";
        }


        // Chronometer in 00:00:00  format
        chrono.setOnChronometerTickListener(new Chronometer.OnChronometerTickListener() {
            @Override
            public void onChronometerTick(Chronometer cArg) {
                long time = SystemClock.elapsedRealtime() - cArg.getBase();
                int h = (int) (time / 3600000);
                int m = (int) (time - h * 3600000) / 60000;
                int s = (int) (time - h * 3600000 - m * 60000) / 1000;
                String hh = h < 10 ? "0" + h : h + "";
                String mm = m < 10 ? "0" + m : m + "";
                String ss = s < 10 ? "0" + s : s + "";
                cArg.setText(hh + ":" + mm + ":" + ss);

                // every 1 minute
                if (time % 60000 == 0) {
                    // TODO: Ensure that we don't need that as we're calculating it directly at the service
                    // TrackingManager.getInstance().addTrackedTime(60); // in secs
                }
            }
        });


        registerForContextMenu(startBtn);
        registerForContextMenu(activityBtn);

        // Start Button   ---------------
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (displayMenu) {
                    showPopupMenu(v);
                } else {

                    // Start and trigger a service

                    if (state.equals("Stop")) {

                        editor.putBoolean("WriteOnBD", true); // value to store
                        editor.putLong("Time", SystemClock.elapsedRealtime() + timeWhenStopped); // value to store Pause to state
                        editor.putBoolean("Service", true); // value to store
                        editor.putFloat("elevation", 0);
                        editor.commit();

                        // Start Tracking
                        TrackingManager trackingManager = TrackingManager.getInstance();
                        trackingManager.loadTrackingInfo(getActivity());
                        trackingManager.startTrackingListeners(getActivity());

                        Intent intent = new Intent(getActivity(), TrackService.class);
                        getActivity().startService(intent);

                        distance = 0;
                        firstAltitude = Double.MIN_VALUE; // the min value
                    }
                    // In case it is returned from pause state
                    TrackingManager.getInstance().setTracking(true);
                    chrono.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chrono.start();

                    startBtn.setText(R.string.label_menu);
                    displayMenu = true;
                    colorLine = Color.BLACK;


                    ActivityDBHelper db = new ActivityDBHelper(getActivity());
                    db.writeEmptyActivity();
                    activityId = db.giveMeLastId();
                    db.close();

                    editor.putString("State", "Run"); // value to store Pause to state
                    editor.commit();

                    state = "Run";
                }
            }
        });

        ivPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isPlay) {
                    isPlay = false;
                    ivPlay.setImageResource(R.drawable.ic_play);
                    editor.putBoolean("WriteOnBD", true); // value to store
                    editor.putLong("Time", SystemClock.elapsedRealtime() + timeWhenStopped); // value to store Pause to state
                    editor.putBoolean("Service", true); // value to store
                    editor.putFloat("elevation", 0);
                    editor.commit();

                    // Start Tracking
                    TrackingManager trackingManager = TrackingManager.getInstance();
                    trackingManager.loadTrackingInfo(getActivity());
                    trackingManager.startTrackingListeners(getActivity());

                    Intent intent = new Intent(getActivity(), TrackService.class);
                    getActivity().startService(intent);

                    distance = 0;
                    firstAltitude = Double.MIN_VALUE; // the min value

                    // In case it is returned from pause state
                    TrackingManager.getInstance().setTracking(true);
                    chrono.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                    chrono.start();

                    startBtn.setText(R.string.label_menu);
                    displayMenu = true;
                    colorLine = Color.BLACK;


                    ActivityDBHelper db = new ActivityDBHelper(getActivity());
                    db.writeEmptyActivity();
                    activityId = db.giveMeLastId();
                    db.close();

                    editor.putString("State", "Run"); // value to store Pause to state
                    editor.commit();

                    state = "Run";
                } else {
                    isPlay = true;
                    ivPlay.setImageResource(R.drawable.ic_pause);
                    TrackingManager.getInstance().setTracking(false);
                    editor.putBoolean("WriteOnBD", true); // value to store
                    editor.putString("State", "Pause"); // value to store Pause to state
                    editor.putFloat("elevation", (float) diffElevation);
                    editor.commit();

                    timeWhenStopped = chrono.getBase() - SystemClock.elapsedRealtime();
                    chrono.stop();
                    Log.i("Stopppppppp", "service is stopppppppppppppppped");

                    colorLine = Color.GRAY;
                    startBtn.setText(R.string.label_start);
                    displayMenu = false;

                    state = "Pause";
                }

            }
        });

        ivSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showInputDialog();
            }
        });

        ivDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //  A supprimer de la table
                ActivityDBHelper db = ActivityDBHelper.getInstance(getActivity());
                db.deleteActivity(String.valueOf(activityId));
                db.close();

                resetFields();
            }
        });


        activityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                pushTrackerFragmentToStack();
//
//                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//                LogTrackerFragment activityLogFragment = (LogTrackerFragment) fragmentManager.findFragmentByTag("ActivityLogFragment");
//                if (activityLogFragment == null) {
//                    activityLogFragment = new LogTrackerFragment();
//                } else {
////                    activityLogFragment.loadActivities();
//                }
//
//                ((MainActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//               ((MainActivity)getActivity()).set(R.drawable.ic_arrow_back);

//             replaceFragment(R.id.trackerSearchLayout, new LogTrackerFragment());
                getContext().startActivity(new Intent(getContext(), LogTrackerAcitivty.class));
            }
        });

        IntentFilter intentFilter = new IntentFilter(TrackService.TAG);
        mReceiver = new BroadcastReceiver() {
            private LocationResult mLocationResult;

            @Override
            public void onReceive(Context context, Intent intent) {
                double latitude = intent.getDoubleExtra("latitude", 0);
                double longitude = intent.getDoubleExtra("longitude", 0);
                double altitude = intent.getDoubleExtra("altitude", 0);

                if (state.equals("Run")) {

                    if (firstAltitude == Double.MIN_VALUE) {
                        firstAltitude = altitude;
                    }

                    LatLng latLngNew = new LatLng(latitude, longitude);

                    Log.i(TrackService.TAG, "Record saved Ouiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii latitude =" + latitude + " and longitude =" + longitude);
                    if (latLngOld.latitude != 0 && latLngOld.longitude != 0) {
                        addLines(myMap, latLngOld, latLngNew);

                        int coordinatesListSize = coordinatesList.size();
                        String pathString = coordinatesList.get(0).latitude + "," + coordinatesList.get(0).longitude;
                        for (int i = 1; i < coordinatesListSize - 1; i++) {
                            pathString += "|" + coordinatesList.get(i).latitude + "," + coordinatesList.get(i).longitude;
                        }
                        pathString += "|" + coordinatesList.get(coordinatesListSize - 1).latitude + "," + coordinatesList.get(coordinatesListSize - 1).longitude;

                        calculateDistanceOnLine(latLngOld, latLngNew);
                        String distanceTxt = ConvertMToKm(distance);
                        float totalLength = Float.valueOf(ConvertMToKmFloat(distance));
                        Location location = new Location("");
                        location.setLatitude(latitude);
                        location.setLongitude(longitude);
                        calculateElevationFromAPI(location, pathString, coordinatesListSize, totalLength);
                        //  Update distance and TextView 's distance
                        txtDistance.setText(distanceTxt);

                    }

                    latLngOld = latLngNew;
                } else if (state.equals("Pause")) {
                    if (firstAltitude == Double.MIN_VALUE) {
                        firstAltitude = altitude;
                    }
                    LatLng latLngNew = new LatLng(latitude, longitude);
                    if (latLngOld.latitude != 0 && latLngOld.longitude != 0) {
                        addLines(myMap, latLngOld, latLngNew);
                    }
                    latLngOld = latLngNew;
                }

            }
        };
        receiverManager.registerReceiver(mReceiver, intentFilter);

        isViewCreated = true;
        loadUi();

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        if (timeWhenFragmentPaused != 0 && state.equals("Run")) {
            // TODO: Ensure that we don't need that as we're calculating it directly at the service
            // TrackingManager.getInstance().addTrackedTime((System.currentTimeMillis() - timeWhenFragmentPaused) / 1000);
        }
        if (mMapView != null) {
            // [START screen_view_hit]
            mTracker.setScreenName("Activity Tracker");
            mTracker.send(new HitBuilders.ScreenViewBuilder().build());
            // [END screen_view_hit]
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        timeWhenFragmentPaused = System.currentTimeMillis();
    }

    @Override
    public void onDestroyView() {
        // TODO: ensure that it is working properly
        TrackingManager trackingManager = TrackingManager.getInstance();
        trackingManager.stopTrackingListeners(getActivity());
        super.onDestroyView();
    }

    @Override
    public void onDestroy() {
        if (mMapView != null) {
            try {
                mMapView.onDestroy();
            } catch (NullPointerException e) {
                Log.e("TAG", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        if (receiverManager != null)
            receiverManager.unregisterReceivers();
        super.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.getUiSettings().setRotateGesturesEnabled(false);

        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener());
        }

        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(true);

        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }

        if (serviceIsStarted) {
            ActivityDBHelper db = new ActivityDBHelper(getActivity());
            activityId = db.giveMeLastId();
            List<PointState> points = db.giveMeAllPoints(activityId);
            db.close();
            addAllLines(myMap, points);
        }
    }

    public void showPopupMenu(View v) {
        PopupMenu popup = new PopupMenu(getActivity(), v);
        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, popup.getMenu());

        // Center items in popup menu
        MenuItem item = popup.getMenu().getItem(0);
        SpannableString s = new SpannableString(getResources().getString(R.string.pause));
        s.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#37647D")), 0, s.length(), 0);
        item.setTitle(s);  // #37647D

        item = popup.getMenu().getItem(1);
        s = new SpannableString(getResources().getString(R.string.save));
        s.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#37647D")), 0, s.length(), 0);
        item.setTitle(s);

        item = popup.getMenu().getItem(2);
        s = new SpannableString(getResources().getString(R.string.delete));
        s.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, s.length(), 0);
        s.setSpan(new ForegroundColorSpan(Color.RED), 0, s.length(), 0);
        item.setTitle(s);

        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                onOptionsItemSelected(item);
                return false;
            }
        });

        popup.show();

    }

    public void showSearchIcon() {
        if (menu != null)
            onCreateOptionsMenu(menu, getActivity().getMenuInflater());
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener() {
        return new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                TrackerFragment.super.onCameraIdle();
            }
        };
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.item_pause:
                TrackingManager.getInstance().setTracking(false);
                editor.putBoolean("WriteOnBD", true); // value to store
                editor.putString("State", "Pause"); // value to store Pause to state
                editor.putFloat("elevation", (float) diffElevation);
                editor.commit();

                timeWhenStopped = chrono.getBase() - SystemClock.elapsedRealtime();
                chrono.stop();
                Log.i("Stopppppppp", "service is stopppppppppppppppped");

                colorLine = Color.GRAY;
                startBtn.setText(R.string.label_start);
                displayMenu = false;

                state = "Pause";
                return true;

            case R.id.item_save:
                showInputDialog();
                return true;


            case R.id.item_delete:

                //   A supprimer de la table
                ActivityDBHelper db = ActivityDBHelper.getInstance(getActivity());
                db.deleteActivity(String.valueOf(activityId));
                db.close();


                resetFields();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
    }

    public void replaceFragment(int resourceID, Fragment tFragment) {
        if (tFragment != null) {
            trackerSearchLayout.setVisibility(View.VISIBLE);
            FragmentTransaction mFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            mFragmentTransaction
                    .replace(resourceID, tFragment)
                    .commit();
        }

    }

    public void showFragment(Fragment f) {
        replaceFragment(R.id.trackerSearchLayout, f);
        searchView.setIconified(true);
    }

    protected void showInputDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
        View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setView(promptView);

        final EditText editText = (EditText) promptView.findViewById(R.id.edittext);
        // setup a dialog window

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


        builder.setView(promptView);

        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                ActivityDBHelper db = new ActivityDBHelper(getActivity());
                db.endSaveActivity(activityId, editText.getText().toString(), translateSecondsToHours(chrono.getBase()), ConvertMToKm(distance), round(diffElevation, 2));
                db.close();
                resetFields();
                dialog.dismiss();

            }
        });
        builder.setNegativeButton(R.string.button_cancel, null);


        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        Button nbutton = alertDialog.getButton(alertDialog.BUTTON_NEGATIVE);
        nbutton.setTextColor(Color.parseColor("#37647D"));

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() >= 1) {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                    Button pbutton = alertDialog.getButton(alertDialog.BUTTON_POSITIVE);
                    pbutton.setTextColor(Color.parseColor("#37647D"));
                } else {
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    Button pbutton = alertDialog.getButton(alertDialog.BUTTON_POSITIVE);
                    pbutton.setTextColor(Color.LTGRAY);

                }

            }
        });


    }

    private void resetFields() {
        timeWhenStopped = 0;
        chrono.stop();
        chrono.setText("00:00:00");

        txtElevation.setText("0 m");
        txtDistance.setText("0 km");

        startBtn.setText(R.string.label_start);  // "Start"
        displayMenu = false;

        colorLine = Color.BLACK;
        distance = 0;
        firstAltitude = Double.MIN_VALUE;
        diffElevation = 0;
        serviceIsStarted = false;

        latLngOld = new LatLng(0, 0);

        editor.putBoolean("WriteOnBD", false); // value to store
        editor.putString("State", "Stop"); // value to store Pause to state
        editor.putBoolean("Service", false); // value to store
        editor.putFloat("elevation", 0);
        editor.commit();

        Intent intent = new Intent(getActivity(), TrackService.class);
        Objects.requireNonNull(getActivity()).stopService(intent);
        TrackingManager trackingManager = TrackingManager.getInstance();
        trackingManager.stopTrackingListeners(getActivity());

        state = "Stop";

        for (Polyline polyline : activityTrackingPolylines) {
            polyline.remove();
        }
        activityTrackingPolylines = new ArrayList<>();
    }

    private long calculateDistanceOnLine(LatLng LatlngOld, LatLng LatlngNew) {

        if (LatlngOld.latitude == 0 && LatlngOld.longitude == 0) {
            distance = 0;
        } else {
            Location locationNew = new Location("");
            locationNew.setLatitude(LatlngNew.latitude);
            locationNew.setLongitude(LatlngNew.longitude);

            Location locationOld = new Location("");
            locationOld.setLatitude(LatlngOld.latitude);
            locationOld.setLongitude(LatlngOld.longitude);

            distance += (long) locationOld.distanceTo(locationNew);
        }

        return distance;
    }

    private String ConvertMToKm(long distance) {
        String distanceTxt = "0 km";
        long kilometer = 0;
        if (distance > 1000) {
            kilometer = distance / 1000;
            long reste = distance % 1000;
            reste = (long) Math.round(reste / 10.0);
            if (reste > 10) {
                distanceTxt = kilometer + "." + reste;
            } else {
                distanceTxt = kilometer + ".0" + reste;
            }
        } else {

            distance = (long) Math.round(distance / 10.0);
            if (distance > 10) {
                distanceTxt = "0." + distance;
            } else {
                distanceTxt = "0.0" + distance;
            }

        }
        return distanceTxt + " km";
    }

    private float ConvertMToKmFloat(long distance) {
        float totalDistance = 0;
        long kilometer = 0;
        if (distance > 1000) {
            kilometer = distance / 1000;
            long reste = distance % 1000;
            reste = (long) Math.round(reste / 10.0);
            if (reste > 10) {
                totalDistance = kilometer + reste;
            } else {
                totalDistance = kilometer + reste;
            }
        } else {

            distance = (long) Math.round(distance / 10.0);
            if (distance > 10) {
                totalDistance = distance / 10;
            } else {
                totalDistance = distance / 100;
            }

        }
        return totalDistance;
    }

    private String translateSecondsToHours(long seconds) {
        long time = SystemClock.elapsedRealtime() - seconds;
        int h = (int) (time / 3600000);
        int m = (int) (time - h * 3600000) / 60000;
        int s = (int) (time - h * 3600000 - m * 60000) / 1000;
        String hh = h < 10 ? "0" + h : h + "";
        String mm = m < 10 ? "0" + m : m + "";
        String ss = s < 10 ? "0" + s : s + "";


        return hh + ":" + mm + ":" + ss;
    }

    private void ShowSearchMenu() {
        pushTrackerFragmentToStack();

        if (!trackerfragStack.containsKey("TrackerSearchFragment")) {
            searchListFragment = createSearchListFragment();
            searchListFragment.resourceID = R.id.trackerSearchLayout;
            searchListFragment.fragTagStack = trackerfragTagStack;
            searchListFragment.fragStack = trackerfragStack;
        } else {
//            searchListFragment = (SearchListFragment) trackerfragStack.get("TrackerSearchFragment");
        }
        replaceFragment(R.id.trackerSearchLayout, searchListFragment);
    }

    private void pushTrackerFragmentToStack() {
//        String segmentTag = "ActivityTrackerFragment";
//        TrackerFragment trackerFragment = ((MainActivity) getActivity()).getTr();
//
//        if (trackerFragment instanceof ActivityTrackerFragment) {
//            trackerfragStack.put(segmentTag, trackerFragment);
//            trackerfragTagStack.push(segmentTag);
//        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void calculateElevationFromAPI(final Location location, String path, int samples, final double totalDistance) {

        Log.i("LocationService", " Reponse api ........................................................................ path = " + path.toString());

        String urlAPI = "https://maps.googleapis.com/maps/api/elevation/json?path=" + path + "&samples=" + samples + "&key=" + "AIzaSyDZL37pIbCDiGjHdPQv2pWNQKOIunX8WWA";  // "AIzaSyDZL37pIbCDiGjHdPQv2pWNQKOIunX8WWA"
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

                            txtElevation.setText(distanceString + " m");

                            diffElevation = elevationDisplayed;

                            if (prevElevation != Integer.MAX_VALUE) {
                                TrackingManager trackingManager = TrackingManager.getInstance();
                                if (trackingManager.isTracking()) {
                                    trackingManager.addAchievedElevation(location, (float) (elevationDisplayed - prevElevation));
                                }
                            }
                            prevElevation = (float) elevationDisplayed;
                            editor.putFloat("elevation", (float) diffElevation);
                            editor.commit();


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

//    private void askPermissionsAndShowMyLocation() {
//        // With API> = 23, you have to ask the user for permission to view their location.
//        if (Build.VERSION.SDK_INT >= 23) {
//            int accessCoarsePermission
//                    = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
//            int accessFinePermission
//                    = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);
//
//
//            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
//                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
//                // The Permissions to ask user.
//                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
//                        android.Manifest.permission.ACCESS_FINE_LOCATION};
//                // Show a dialog asking the user to allow the above permissions.
//                ActivityCompat.requestPermissions(activity, permissions,
//                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);
//
//                return;
//            }
//        }
//
//        // Show current location on Map.
//        this.showMyLocation();
//    }

    // Call this method only when you have the permissions to view a user's location.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) {
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.DEFAULTORIGIN, 3));
            return;
        }

        Location myLocation = null;
        try {
            // This code need permissions (Asked above ***)
            // Getting Location.
            myLocation = locationManager
                    .getLastKnownLocation(locationProvider);

        }
        // With Android API >= 23, need to catch SecurityException.
        catch (SecurityException e) {

            Log.e("ActivityTackeSegment", "Show My Location Error:" + e.getMessage());
            e.printStackTrace();
            return;
        }

        if (myLocation != null) {
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, MAP_VIEW_CAMERA_STREET_ZOOM_LEVEL));

        } else {
            Log.i("ActivityTrackerFragment", "Location not found");
            myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Constants.DEFAULTORIGIN, 3));
        }


    }

    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

        // Criteria to find location provider.
        Criteria criteria = new Criteria();

        // Returns the name of the provider that best meets the given criteria.
        // ==> "gps", "network",...
        String bestProvider = locationManager.getBestProvider(criteria, true);

        boolean enabled = locationManager.isProviderEnabled(bestProvider);

        if (!enabled) {

            Log.i("ActivityTrackerFragment", "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    private void addLines(GoogleMap googleMap, LatLng latLngOld, LatLng latLngNew) {
        if (googleMap == null)
            return;
        Polyline polyline = googleMap.addPolyline((new PolylineOptions())
                .add(latLngOld, latLngNew)
                .width(5).color(colorLine)
                .geodesic(true));

        if (activityTrackingPolylines != null)
            activityTrackingPolylines.add(polyline);
    }

    private void addAllLines(GoogleMap googleMap, List<PointState> points) {

        int colorLine = Color.BLACK;
        distance = 0;

        googleMap.clear();
        for (int i = 0; i < points.size() - 1; i++) {
            switch (points.get(i).getState()) {
                case "Run":
                    colorLine = Color.BLACK;
                    break;
                case "Pause":
                    colorLine = Color.GRAY;
                    break;
                case "Offline":
                    colorLine = Color.RED;
                    break;
                case "Stop":
                    colorLine = Color.WHITE;
                    break;
                default:
                    colorLine = Color.WHITE;
            }
            Polyline polyline = googleMap.addPolyline((new PolylineOptions())
                    // .add(TIMES_SQUARE, BROOKLYN_BRIDGE, LOWER_MANHATTAN,TIMES_SQUARE)
                    .add(points.get(i).getPoint(), points.get(i + 1).getPoint())
                    .width(5).color(colorLine)
                    .geodesic(true));

            if (activityTrackingPolylines != null)
                activityTrackingPolylines.add(polyline);
            calculateDistanceOnLine(points.get(i).getPoint(), points.get(i + 1).getPoint());
            Log.i(TrackService.TAG, "addAllLines    iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii addAllLines =  ");

        }
        txtDistance.setText(ConvertMToKm(distance));
    }

    public class ReceiverManager {

        private List<BroadcastReceiver> receivers = new ArrayList<BroadcastReceiver>();
        private ReceiverManager ref;
        private Context context;

        private ReceiverManager(Context context) {
            this.context = context;
        }

        public synchronized ReceiverManager init(Context context) {
            if (ref == null) ref = new ReceiverManager(context);
            return ref;
        }

        public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter intentFilter) {
            receivers.add(receiver);
            Intent intent = context.registerReceiver(receiver, intentFilter);
            Log.i(getClass().getSimpleName(), "registered receiver: " + receiver + "  with filter: " + intentFilter);
            Log.i(getClass().getSimpleName(), "receiver Intent: " + intent);
            return intent;
        }

        public boolean isReceiverRegistered(BroadcastReceiver receiver) {
            boolean registered = receivers.contains(receiver);
            Log.i(getClass().getSimpleName(), "is receiver " + receiver + " registered? " + registered);
            return registered;
        }

        public void unregisterReceivers() {
            for (BroadcastReceiver broadcastReceiver : receivers) {
                context.unregisterReceiver(broadcastReceiver);
            }
        }
    }

}
