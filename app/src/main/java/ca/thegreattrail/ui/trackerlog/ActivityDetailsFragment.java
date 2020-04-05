package ca.thegreattrail.ui.trackerlog;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelper;
import ca.thegreattrail.data.model.other.PointState;
import ca.thegreattrail.data.service.TrackService;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.ui.tracker.TrackerFragment;
import static android.content.Context.LOCATION_SERVICE;


public class ActivityDetailsFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static ActivityDetailsFragment instance = null;
    private long activityId = 0;
    protected static MapView mMapView;
    private GoogleMap myMap;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton floatingActionButton;
    private Bitmap myBitmap = null;

    private ImageView shareBtn;
    private ImageView deleteBtn;
    private ImageView backBtn;
    public RelativeLayout main_toolbar_layout;

    //private int step = 50 ;
    private int currentZoom = 12;
    private String title = "";
    private Menu myMenu;
    MainActivity activity;

    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;

    public static ActivityDetailsFragment getInstance() {
        if (instance == null) {
            Log.i("Instance AT", "New Creation");
            instance = new ActivityDetailsFragment();
        }
        Log.i("Instance AT", "No Creation");
        return instance;
    }


    public static ActivityDetailsFragment newInstance() {
        ActivityDetailsFragment activityDetailsFragment = getInstance();

        return activityDetailsFragment;
    }

    public ActivityDetailsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity=(MainActivity)context;
    }

    public void setActivityId(long activityId) {
        this.activityId = activityId;
    }

   /* @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        myMenu = menu;
        menu.clear();
        *//*menu.add(Menu.NONE, 0, Menu.NONE, "Share").setIcon(R.drawable.ic_upload)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
        menu.add(Menu.NONE, 0, Menu.NONE, "Delete").setIcon(R.drawable.ic_trash_white)
                .setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);*//*

        inflater.inflate(R.menu.activity_tracker_details, menu);



    }*/


    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }*/


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
//        if(currentTab.equals("MapFragment") && mapfragStack.containsKey("MapSearchFragment")){
//            menu.clear();
//        }
//        else if(currentTab.equals("MeasureFragment") && measurefragStack.containsKey("MeasureSearchFragment")){
//            menu.clear();
//        }
//        else  if(currentTab.equals("ActivityTrackerFragment")){
//                    if(trackerfragStack.containsKey("ActivityLogFragment") ){
//                        menu.clear();
//                        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();
//                    }
//                    else if(trackerfragStack.containsKey("TrackerSearchFragment") ||  trackerfragStack.containsKey("ActivityTrackerFragment")){
//                        menu.clear();
//                    }
//        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_details_fragment, container, false);
        main_toolbar_layout = (RelativeLayout) view.findViewById(R.id.main_toolbar_layout);
        mMapView = (MapView) view.findViewById(R.id.mapview1);
        mMapView.onCreate(savedInstanceState);


       /* Toolbar toolbar = (Toolbar) view.findViewById(R.id.activityDetailFragmentToolbar);
        toolbar.setTitle("");
        ((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(false);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(false);
*/
        setHasOptionsMenu(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().hide();


        shareBtn = (ImageView) view.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.System.canWrite(activity)) {
                        requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                    } else {
                        screenShot(); //shareMap(); //screenShot(); //captureScreen(); //shareMap();
                    }
                } else {
                    screenShot(); //shareMap(); //screenShot(); //captureScreen(); //shareMap();
                }

            }
        });


        backBtn = (ImageView) view.findViewById(R.id.backBtn);
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

              //  activity.onBackPressed();
                back();

            }
        });



        deleteBtn = (ImageView) view.findViewById(R.id.deleteBtn);
        deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                builder.setMessage(getResources().getText(R.string.title_delete_activity_message));
                builder.setTitle(getResources().getText(R.string.title_delete_activity_title));
                builder.setCancelable(true);

                builder.setPositiveButton(
                        getResources().getText(R.string.yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                ActivityDBHelper db = ActivityDBHelper.getInstance(activity);
                                db.deleteActivity(String.valueOf(activityId));
                                db.close();
                                deleteBtn.setEnabled(false);
                                // myMenu.add(Menu.NONE, 0, Menu.NONE, "Delete").setIcon(R.drawable.ic_trash_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                                dialog.cancel();

                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle(getResources().getText(R.string.alert_delete_activity_confirmation_title));
                                builder.setMessage(getResources().getText(R.string.alert_delete_activity_confirmation_message));
                                builder.setCancelable(true);
                                builder.setPositiveButton(android.R.string.ok,
                                        new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int id) {
                                                activity.onBackPressed();
                                                dialog.cancel();
                                            }
                                        });

                                AlertDialog alert = builder.create();
                                alert.show();



                            /*FragmentManager fragmentManager = activity.getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                            ActivityLogFragment activityLogFragment = ActivityLogFragment.getInstance();
                            //ActivityDetailsFragment activityDetailsFragment =  (ActivityDetailsFragment) fragmentManager.findFragmentByTag("ActivityDetailsFragment");
                            if (instance != null) {

                                fragmentTransaction
                                .show(activityLogFragment)
                                .hide(instance);
                                fragmentTransaction.commit();

                            }*/
                            }
                        });

                builder.setNegativeButton(
                        getResources().getText(R.string.no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // resetToolbar();
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();

            }
        });



        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (Build.VERSION.SDK_INT >= 23) {
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
                }

                Location mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (mLastLocation != null) {
                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
                }
            }
        });



        TextView txtDistance =  (TextView) view.findViewById(R.id.txtDistance2);
        TextView txtElevation =  (TextView) view.findViewById(R.id.txtElevation2);
        //  txtTime = (TextView) view.findViewById(R.id.txtTime);
        Chronometer chrono = (Chronometer) view.findViewById(R.id.chronometer2);

        ActivityDBHelper db = new ActivityDBHelper(activity);
        List<String> dataToReceive = db.giveMeActivity(activityId);
        db.close();

        txtDistance.setText(dataToReceive.get(0)+" km");
        chrono.setText(dataToReceive.get(1));
        txtElevation.setText(dataToReceive.get(2)+" m");
        title = dataToReceive.get(3);

        /*((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);*/

        TextView main_toolbar_title = (TextView) view.findViewById(R.id.main_toolbar_title);

        main_toolbar_title.setText(title);



//        view.setFocusableInTouchMode(true);
//        view.requestFocus();
//        view.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event)   {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//
//
//                    ActivityLogFragment actLogger= ActivityLogFragment.getInstance();
//                    FragmentTransaction transaction=getFragmentManager().beginTransaction();
//                    transaction.replace(,actLogger);
//
//
//                    transaction.commit();
//
//
//
//
//                    return true;
//                }
//                return false;
//
//
//            }
//        });


        return view;

    }


    private void back(){
        if(TrackerFragment.trackerfragTagStack.size()>1) {
            int beforLast = TrackerFragment.trackerfragTagStack.size() - 1;
            String lastTag = TrackerFragment.trackerfragTagStack.get(beforLast);
            TrackerFragment.trackerfragTagStack.pop();
            TrackerFragment.trackerfragStack.remove(lastTag);
        }

        main_toolbar_layout.setVisibility(View.GONE);
        ((AppCompatActivity) getActivity()).getSupportActionBar().show();

        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        LogTrackerFragment activityLogFragment =  (LogTrackerFragment) fragmentManager.findFragmentByTag("ActivityLogFragment");
        if (activityLogFragment== null) {
            activityLogFragment=LogTrackerFragment.newInstance();
        }
        fragmentTransaction
                .replace(R.id.trackerSearchLayout, activityLogFragment, "ActivityLogFragment")
                //  .addToBackStack(null)
                .commit();
    }

    @Override
    public void onStart() {

        mGoogleApiClient.connect();
        // Set callback listener, on Google Map ready.
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);

                myMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {


                    }
                });
            }
        });


        super.onStart();

    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
      //  myMenu.clear();
//        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
//        activity.getSupportActionBar().setTitle("");

        super.onStop();


    }



    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }

//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(title);
    }




    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
      //  myMenu.clear();
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        super.onPause();
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
       // myMenu.clear();
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
//        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");

        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        if (mMapView != null) {
            mMapView.onLowMemory();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }
    }


    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = activity.getMenuInflater();
        inflater.inflate(R.menu.contextual_menu, menu);

    }



    private void onMyMapReady(GoogleMap googleMap) {

        // Get Google Map from Fragment.
        myMap = googleMap;
        myMap.getUiSettings().setRotateGesturesEnabled(false);
        myMap.getUiSettings().setCompassEnabled(false);

        // Set OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {

                //
                ActivityDBHelper db = new ActivityDBHelper(activity);
                LatLng point = db.CalculateMedianePoint(activityId);

                // LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
                // LatLng latLng = new LatLng(-34, 151);
                //myMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
                if(point.latitude == 0 && point.longitude ==0) {
                    askPermissionsAndShowMyLocation();
                } else {
                    // myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 14));
                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, currentZoom));

                }


            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        if (ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(false);

        ActivityDBHelper db = new ActivityDBHelper(activity);
        List<PointState> points = db.giveMeAllPoints(activityId);
        db.close();
        addAllLines(myMap,points);
    }


    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(activity, android.Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(activity, permissions,
                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.
        this.showMyLocation();
    }


    // Call this method only when you have the permissions to view a user's location.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

        String locationProvider = this.getEnabledLocationProvider();

        if (locationProvider == null) {
            return;
        }

        // Millisecond
        final long MIN_TIME_BW_UPDATES = 1000;
        // Met
        final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 1;

        Location myLocation = null;
        try {
            // This code need permissions (Asked above ***)

          /*  locationManager.requestLocationUpdates(
                    locationProvider,
                    MIN_TIME_BW_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES, (android.location.LocationListener) this); */


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

//             // Add a marker in Sydney and move the camera
//            LatLng sydney = new LatLng(-34, 151);
//            myMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
//            myMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
//            Toast.makeText(getActivity(),"Je suis a Sydney",Toast.LENGTH_SHORT);


            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            // LatLng latLng = new LatLng(-34, 151);
            //myMap.addMarker(new MarkerOptions().position(latLng).title("Marker in Sydney"));
            //  myMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 14));

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(latLng)             // Sets the center of the map to location user
                    .zoom(currentZoom)                   // Sets the zoom
                    .bearing(0)                // Sets the orientation of the camera to east
                    .tilt(40)                   // Sets the tilt of the camera to 30 degrees
                    .build();                   // Creates a CameraPosition from the builder
            myMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            /*
            // Add Marker to Map
            MarkerOptions option = new MarkerOptions();
            option.title("My Location");
            option.snippet("....");
            option.position(latLng);
            Marker currentMarker = myMap.addMarker(option);
            currentMarker.showInfoWindow();
            */
        } else {

            Log.i("ActivityTrackerFragment", "Location not found");
        }

    }

    private String getEnabledLocationProvider() {
        LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);

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




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }


    @Override
    public void onLocationChanged(Location location) {
    }

    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    private void addAllLines(GoogleMap googleMap, List<PointState> points ) {

        Log.i(TrackService.TAG, "addAllLines    iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii size =  "+points.size());
        int colorLine = Color.BLACK ;

        /*LatLng LOWER_MANHATTAN = new LatLng(40.722543, -73.998585);
        LatLng TIMES_SQUARE = new LatLng(40.7577, -73.9857);
        LatLng BROOKLYN_BRIDGE = new LatLng(40.7057, -73.9964);*/
        googleMap.clear();
        for(int i=0; i<points.size()-1; i++) {
            //     int j = (i+step < points.size()-1) ? i+step : points.size()-1;
            switch(points.get(i).getState()){
                case "Run" : colorLine = Color.BLACK;
                    break;
                case "Pause" : colorLine = Color.GRAY;
                    break;
                case "Offline" : colorLine = Color.RED;
                    break;
                case "Stop" : colorLine = Color.WHITE;
                    break;
                default: colorLine = Color.WHITE;
            }
            googleMap.addPolyline((new PolylineOptions())
                    // .add(TIMES_SQUARE, BROOKLYN_BRIDGE, LOWER_MANHATTAN,TIMES_SQUARE)
                    .add(points.get(i).getPoint(), points.get(i+1).getPoint())
                    .width(5).color(colorLine)
                    .geodesic(true));

            Log.i(TrackService.TAG, "addAllLines    iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii addAllLines =  ");

        }

        if(points.size()>0){

           /* Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            Bitmap bmp = Bitmap.createBitmap(200, 50, conf);
            Canvas canvas = new Canvas(bmp);

            Paint paint = new Paint();
            paint.setStyle(Paint.Style.FILL);
            paint.setColor(Color.WHITE);
          //  paint.setTypeface(tf);
            paint.setTextAlign(Paint.Align.CENTER);
            paint.setTextSize(convertToPixels(context, 11));

            canvas.drawText("TEXT", 0, 50, paint); // paint defines the text color, stroke width, size
            Marker mFirst =  googleMap.addMarker(new MarkerOptions()
                    .position(points.get(0).getPoint())
                    .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_menu_about))
                    .icon(BitmapDescriptorFactory.fromBitmap(bmp))
                    .anchor(0.5f, 1)
            );
                                  //  this is test for badge
            mFirst.setTag(0);*/

            /*//  http://stackoverflow.com/questions/13763545/android-maps-api-v2-with-custom-markers
            Marker myLocMarker = googleMap.addMarker(new MarkerOptions()
                    .position(points.get(0).getPoint())
                    .icon(BitmapDescriptorFactory.fromBitmap(writeTextOnDrawable(R.drawable.ic_map, "2"))));*/


            Marker mFirst;
            mFirst = googleMap.addMarker(new MarkerOptions()
                    .position(points.get(0).getPoint())
                    .title("Start"));
            mFirst.setTag(0);
        }

        if(points.size()>1){
            Marker mEnd;
            mEnd = googleMap.addMarker(new MarkerOptions()
                    .position(points.get(points.size()-1).getPoint())
                    .title("End"));
            mEnd.setTag(0);
        }




        // move camera to zoom on map
        //  googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLngNew,13));
    }


    private Bitmap writeTextOnDrawable(int drawableId, String text) {

        Bitmap bm = BitmapFactory.decodeResource(getResources(), drawableId)
                .copy(Bitmap.Config.ARGB_8888, true);

        Typeface tf = Typeface.create("Helvetica", Typeface.BOLD);

        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL);
        paint.setColor(Color.WHITE);
        paint.setTypeface(tf);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setTextSize(dpToPx(11));

        Rect textRect = new Rect();
        paint.getTextBounds(text, 0, text.length(), textRect);

        Canvas canvas = new Canvas(bm);

        //If the text is bigger than the canvas , reduce the font size
        if(textRect.width() >= (canvas.getWidth() - 4))     //the padding on either sides is considered as 4, so as to appropriately fit in the text
            paint.setTextSize(dpToPx(7));        //Scaling needs to be used for different dpi's

        //Calculate the positions
        int xPos = (canvas.getWidth() / 2) - 2;     //-2 is for regulating the x position offset

        //"- ((paint.descent() + paint.ascent()) / 2)" is the distance from the baseline to the center.
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;

        canvas.drawText(text, xPos, yPos, paint);

        return  bm;
    }


    public int dpToPx(int dp) {
        //Context context = MainActivity.context;
        Resources resources = activity.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT)) ;
        return px;
    }


    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {


        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.share) {
            resetToolbar();
            return true;
        }



        //noinspection SimplifiableIfStatement
        if (id == R.id.delete) {
            resetToolbar();
            return true;
        }
        if(item.getTitle().equals("Share")) {

            //Toast.makeText(activity,"share", Toast.LENGTH_LONG).show();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.System.canWrite(activity)) {
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE}, 2909);
                } else {
                    screenShot(); //shareMap(); //screenShot(); //captureScreen(); //shareMap();
                }
            } else {
                screenShot(); //shareMap(); //screenShot(); //captureScreen(); //shareMap();
            }

        }
        else if(item.getTitle().equals("Delete")) {
         //  Toast.makeText(activity,"Delete", Toast.LENGTH_LONG).show();

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Are you sure you want to delete this activity ?");
            builder.setTitle("Delete Activity");
            builder.setCancelable(true);

            builder.setPositiveButton(
                    "Yes",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            ActivityDBHelper  db = ActivityDBHelper.getInstance(activity);
                            db.deleteActivity(String.valueOf(activityId));
                            item.setEnabled(false);
                           // myMenu.add(Menu.NONE, 0, Menu.NONE, "Delete").setIcon(R.drawable.ic_trash_white).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);
                            dialog.cancel();

                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            builder.setTitle("Delete Activity");
                            builder.setMessage("The activity was  deleted successfully.");
                            builder.setCancelable(true);
                            builder.setPositiveButton(android.R.string.ok,
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int id) {
                                            back();
                                            dialog.cancel();
                                        }
                                    });

                            AlertDialog alert = builder.create();
                            alert.show();



                            /*FragmentManager fragmentManager = activity.getSupportFragmentManager();
                            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

                            ActivityLogFragment activityLogFragment = ActivityLogFragment.getInstance();
                            //ActivityDetailsFragment activityDetailsFragment =  (ActivityDetailsFragment) fragmentManager.findFragmentByTag("ActivityDetailsFragment");
                            if (instance != null) {

                                fragmentTransaction
                                .show(activityLogFragment)
                                .hide(instance);
                                fragmentTransaction.commit();

                            }*/
                        }
                    });

            builder.setNegativeButton(
                    "No",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                           // resetToolbar();
                            dialog.cancel();
                        }
                    });
            AlertDialog alert = builder.create();
            alert.show();

        }

        return true;
    }


    private void shareMap() {

        Bitmap screenshot = takeScreenShot(activity);

        String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), screenshot, "title", null);
        Uri screenshotUri = Uri.parse(path);

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        emailIntent.setType("image/png");

        startActivity(Intent.createChooser(emailIntent, "Share saved activity"));

    }

    private void shareMap(Bitmap screenshot) {

        String path = MediaStore.Images.Media.insertImage(activity.getContentResolver(), screenshot, "title", null);
        Uri screenshotUri = Uri.parse(path);

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        emailIntent.putExtra(Intent.EXTRA_STREAM, screenshotUri);
        emailIntent.setType("image/png");

        startActivity(Intent.createChooser(emailIntent, "Share saved activity"));

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 2909: {
                if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    screenShot(); //shareMap(); // screenShot(); //captureScreen(); //shareMap();
                } else {
                    Log.e("Permission", "Denied");
                    Toast.makeText(activity,"To share the map you must give permission", Toast.LENGTH_LONG).show();
                }
                return;
            }
        }
    }


    private Bitmap takeScreenShot(Activity activity)
    {
        View view = activity.getWindow().getDecorView();
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap b1 = view.getDrawingCache();
        Rect frame = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
        int statusBarHeight = frame.top;
        int width = activity.getWindowManager().getDefaultDisplay().getWidth();
        int height = activity.getWindowManager().getDefaultDisplay().getHeight();

        Bitmap b = Bitmap.createBitmap(b1, 0, dpToPx(70)+mMapView.getHeight(), width, height  - dpToPx(70)-mMapView.getHeight()-dpToPx(65));
        view.destroyDrawingCache();
        return b;
    }


    private Bitmap takeScreenshot2() {
        /*Date now = new Date();
        android.text.format.DateFormat.format("yyyy-MM-dd_hh:mm:ss", now);*/
        Bitmap bitmap = null;
        try {
            // image naming and path  to include sd card  appending name you choose for file
            String mPath = Environment.getExternalStorageDirectory().toString() + "/Activity" + ".jpg";

            // create bitmap screen capture
            View v1 = activity.getWindow().getDecorView().getRootView();
            v1.setDrawingCacheEnabled(true);
            bitmap = Bitmap.createBitmap(v1.getDrawingCache());
            v1.setDrawingCacheEnabled(false);

            File imageFile = new File(mPath);

            FileOutputStream outputStream = new FileOutputStream(imageFile);
            int quality = 100;
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream);
            outputStream.flush();
            outputStream.close();
            return bitmap;

        } catch (Throwable e) {
            // Several error may come out with file handling or OOM
            e.printStackTrace();
        }
        return bitmap;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    public Bitmap takeScreenshot3(){
        mMapView.setDrawingCacheEnabled(true);
        Bitmap bm = mMapView.getDrawingCache();
        return bm;
    }


    public Bitmap CaptureMapScreen()
    {

        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap bitmap  ;

            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                // TODO Auto-generated method stub
                bitmap = snapshot;
                try {
                    FileOutputStream out = new FileOutputStream("/mnt/sdcard/"
                            + "MyActivity"
                            + ".png");

                    // above "/mnt ..... png" => is a storage path (where image will be stored) + name of image you can customize as per your Requirement

                    bitmap.compress(Bitmap.CompressFormat.PNG, 90, out);
                    myBitmap = bitmap;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        myMap.snapshot(callback);
        return myBitmap;



        // myMap is object of GoogleMap +> GoogleMap myMap;
        // which is initialized in onCreate() =>
        // myMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_pass_home_call)).getMap();
    }



    public void captureScreen()
    {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback()
        {


            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                try {

                    View view = activity.getWindow().getDecorView();
                    view.setDrawingCacheEnabled(true);
                    view.buildDrawingCache();
                    Bitmap b1 = view.getDrawingCache();
                    Rect frame = new Rect();
                    activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
                    int statusBarHeight = frame.top;
                    int width = activity.getWindowManager().getDefaultDisplay().getWidth();
                    int height = activity.getWindowManager().getDefaultDisplay().getHeight();


                    Bitmap bmOverlay = Bitmap.createBitmap(
                            b1.getWidth(), b1.getHeight(),
                            b1.getConfig());
                    Canvas canvas = new Canvas(bmOverlay);
                    canvas.drawBitmap(snapshot, new Matrix(), null);
                    canvas.drawBitmap(b1, 0, 0, null);

                    Bitmap b = Bitmap.createBitmap(b1, 0, statusBarHeight, width, height  - statusBarHeight);
                    view.destroyDrawingCache();

                    shareMap(bmOverlay);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };


        myMap.snapshot(callback);
    }

    public void openShareImageDialog(String filePath)
    {
        File file = activity.getFileStreamPath(filePath);

        if(!filePath.equals(""))
        {
            final ContentValues values = new ContentValues(2);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
            final Uri contentUriFile = activity.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            final Intent intent = new Intent(android.content.Intent.ACTION_SEND);
            intent.setType("image/jpeg");
            intent.putExtra(android.content.Intent.EXTRA_STREAM, contentUriFile);
            startActivity(Intent.createChooser(intent, "Share Image"));
        }
        else
        {
            //This is a custom class I use to show dialogs...simply replace this with whatever you want to show an error message, Toast, etc.
            // DialogUtilities.showOkDialogWithText(this, R.string.shareImageFailed);

           // Toast.makeText(activity.getBaseContext(),"Try again",Toast.LENGTH_SHORT).show();
        }
    }


    private void screenShot() {
        GoogleMap.SnapshotReadyCallback callback = new GoogleMap.SnapshotReadyCallback() {
            Bitmap map;


            @Override
            public void onSnapshotReady(Bitmap snapshot) {
                map = snapshot;
                try {
                    FileOutputStream out = new FileOutputStream("/mnt/sdcard/MyActivity.png");
                    map.compress(Bitmap.CompressFormat.PNG, 90, out);
                    Bitmap toolbar = takeScreenShot(activity);

                    Bitmap result = Bitmap.createBitmap(map.getWidth(), map.getHeight()+toolbar.getHeight(), map.getConfig());
                    Canvas canvas = new Canvas(result);
                    canvas.drawBitmap(map, 0f, 0f, null);
                    canvas.drawBitmap(toolbar, 0f, map.getHeight(), null);


//0f and 0f refers to coordinates of drawing, you may want to do some calculation here.
                  //  canvas.drawBitmap(toolbar, 0f, map.getHeight(), null);

// At this point base will have the mascot drawn, you may want to display it or save it somewhere else.

                    shareMap(result);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };

        myMap.snapshot(callback);
    }

    public void resetToolbar() {
      //   myMenu.clear();
        /*activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        activity.getSupportActionBar().setTitle("");

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
*/
       ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
        myMenu.findItem(R.id.share).setVisible(false);
        myMenu.findItem(R.id.delete).setVisible(false);
    }

}

