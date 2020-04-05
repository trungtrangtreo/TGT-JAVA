package ca.thegreattrail.ui.trackerlog;

import android.content.Intent;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelper;
import ca.thegreattrail.data.model.other.Item;
import ca.thegreattrail.ui.base.BaseActivity;

public class LogTrackerAcitivty extends BaseActivity implements LocationListener, AdapterView.OnItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public static LogTrackerFragment instance = null;
    private MapView mMapView;
    private GoogleMap myMap;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton floatingActionButton;
    private ArrayList<Item> items = new ArrayList<Item>();
    private ListView listView;
    private TextView informationMessage;
    private ImageView backBtn;

    private EntryAdapter adapter;

    public static LogTrackerFragment getInstance() {
        if (instance == null) {
            Log.i("Instance AT", "New Creation");
            instance = new LogTrackerFragment();
        }
        Log.i("Instance AT", "No Creation");
        return instance;
    }


    public static LogTrackerFragment newInstance() {
        LogTrackerFragment activityLogFragment = getInstance();
        return activityLogFragment;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tracker_log_activity);

        mMapView = findViewById(R.id.mapview1);
        backBtn = findViewById(R.id.backBtn);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume();

        listView = findViewById(R.id.list_Log);
        informationMessage = findViewById(R.id.informationMessage);
        informationMessage.setText(getResources().getText(R.string.activity_log_message));

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        adapter = new EntryAdapter(this, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadActivities();
    }

    @Override
    protected void onStart() {
        mGoogleApiClient.connect();
        // Set callback listener, on Google Map ready.
        mMapView.getMapAsync(new OnMapReadyCallback() {

            @Override
            public void onMapReady(GoogleMap googleMap) {
                onMyMapReady(googleMap);
            }
        });
        super.onStart();
    }

    @Override
    protected void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (!items.get(position).isSection()) {
//
            EntryItem item = (EntryItem) items.get(position);

//            pushActivityLogFragmentToStack();
//
//            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
//
//            ActivityDetailsFragment activityDetailsFragment =  (ActivityDetailsFragment) fragmentManager.findFragmentByTag("ActivityDetailsFragment");
//            if (activityDetailsFragment== null) {
//                activityDetailsFragment=ActivityDetailsFragment.newInstance();
//            }
//
//            activityDetailsFragment.setActivityId(item._id);
//
//            replaceFragment(R.id.trackerSearchLayout, activityDetailsFragment);


            DetailTrackerActivity.setActivityId(item._id);
            Intent intent = new Intent(this, DetailTrackerActivity.class);
            startActivity(intent);


            Log.d("added to stack", "ActivityDetailsFragment");

//            fragmentTransaction
//                    .show(activityDetailsFragment)
//                    .hide(activityLogFragment)
//                    .hide(mapFragment)
//                    .hide(activityTrackerFragment)
//                    .hide(measureFragment);
//
//            if (fragmentManager.findFragmentByTag("UploadFlickrFragment")!= null){
//                UploadFlickrFragment uploadFlickrFragment = UploadFlickrFragment.getInstance();
//                fragmentTransaction.hide(uploadFlickrFragment);
//            }
//
//            if (fragmentManager.findFragmentByTag("SegmentDetailsFragment")!= null){
//                SegmentDetailsFragment segmentDetailsFragment = SegmentDetailsFragment.getInstance();
//                fragmentTransaction.hide(segmentDetailsFragment);
//            }
        }
    }

    public void loadActivities() {

        HashMap<Integer, ArrayList<Item>>[] itemsMonth = new HashMap[12];

        ArrayList<Item> items0 = new ArrayList<Item>();
        ArrayList<Item> items1 = new ArrayList<Item>();
        ArrayList<Item> items2 = new ArrayList<Item>();
        ArrayList<Item> items3 = new ArrayList<Item>();
        ArrayList<Item> items4 = new ArrayList<Item>();
        ArrayList<Item> items5 = new ArrayList<Item>();
        ArrayList<Item> items6 = new ArrayList<Item>();
        ArrayList<Item> items7 = new ArrayList<Item>();
        ArrayList<Item> items8 = new ArrayList<Item>();
        ArrayList<Item> items9 = new ArrayList<Item>();
        ArrayList<Item> items10 = new ArrayList<Item>();
        ArrayList<Item> items11 = new ArrayList<Item>();
        ArrayList<Item> items12 = new ArrayList<Item>();

        int month0 = -1;
        int month1 = -1;
        int month2 = -1;
        int month3 = -1;
        int month4 = -1;
        int month5 = -1;
        int month6 = -1;
        int month7 = -1;
        int month8 = -1;
        int month9 = -1;
        int month10 = -1;
        int month11 = -1;


        items.clear();
        Cursor cursor = null;
        ActivityDBHelper db = new ActivityDBHelper(this);
        cursor = db.getAllActivities();

        if (cursor != null && cursor.moveToFirst()) {

            informationMessage.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            try {

                int newMonth = -1;
                //  int oldMonth = -1;
                int year = -1;

                do {
                    long _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String start_time = cursor.getString(cursor.getColumnIndex("Start_time"));


                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    final Calendar c = Calendar.getInstance();
                    try {
                        c.setTime(df.parse(start_time));
                        year = c.get(Calendar.YEAR);
                        newMonth = c.get(Calendar.MONTH);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    int month = year * 100 + newMonth;

                    int region = cursor.getInt(cursor.getColumnIndex("Region"));
                    String acivityName = cursor.getString(cursor.getColumnIndex("Activity_name"));
                    float distance = cursor.getFloat(cursor.getColumnIndex("Distance"));
                    float elevation = cursor.getFloat(cursor.getColumnIndex("Elevation"));
                    String time = cursor.getString(cursor.getColumnIndex("Time"));

                    String subTitle = start_time + "     " + distance + " km     " + elevation + " m     " + time + " min";


                    if (acivityName.equalsIgnoreCase("No Name") && distance == 0.0 && elevation == 0.0 && region == 0) {
                    } else {

                        switch (newMonth) {
                            case 0:
                                if (month0 < newMonth) {
                                    items0 = new ArrayList<Item>();
                                    items0.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[0] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[0].put(newMonth, items0);
                                } else {
                                    items0.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[0].put(newMonth, items0);
                                }
                                month0 = newMonth;
                                break;

                            case 1:
                                if (month1 < newMonth) {
                                    items1 = new ArrayList<Item>();
                                    items1.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[1] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[1].put(newMonth, items1);
                                } else {
                                    items1.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[1].put(newMonth, items1);
                                }
                                month1 = newMonth;
                                break;


                            case 2:
                                if (month2 < newMonth) {
                                    items2 = new ArrayList<Item>();
                                    items2.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[2] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[2].put(newMonth, items2);

                                } else {
                                    items2.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[2].put(newMonth, items2);
                                }
                                month2 = newMonth;

                                break;

                            case 3:
                                if (month3 < newMonth) {
                                    items3 = new ArrayList<Item>();
                                    items3.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[3] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[3].put(newMonth, items3);

                                } else {
                                    items3.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[3].put(newMonth, items3);
                                }
                                month3 = newMonth;

                                break;

                            case 4:
                                if (month4 < newMonth) {
                                    items4 = new ArrayList<Item>();
                                    items4.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[4] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[4].put(newMonth, items4);

                                } else {
                                    items4.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[4].put(newMonth, items4);
                                }
                                month4 = newMonth;

                                break;

                            case 5:
                                if (month5 < newMonth) {
                                    items5 = new ArrayList<Item>();
                                    items5.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[5] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[5].put(newMonth, items5);

                                } else {
                                    items5.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[5].put(newMonth, items5);
                                }
                                month5 = newMonth;

                                break;

                            case 6:
                                if (month6 < newMonth) {
                                    items6 = new ArrayList<Item>();
                                    items6.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[6] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[6].put(newMonth, items6);

                                } else {
                                    items6.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[6].put(newMonth, items6);
                                }
                                month6 = newMonth;

                                break;

                            case 7:
                                if (month7 < newMonth) {
                                    items7 = new ArrayList<Item>();
                                    items7.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[7] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[7].put(newMonth, items7);
                                } else {
                                    items7.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[7].put(newMonth, items7);
                                }
                                month7 = newMonth;

                                break;

                            case 8:
                                if (month8 < newMonth) {
                                    items8 = new ArrayList<Item>();
                                    items8.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[8] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[8].put(newMonth, items8);

                                } else {
                                    items8.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[8].put(newMonth, items8);
                                }
                                month8 = newMonth;

                                break;

                            case 9:
                                if (month9 < newMonth) {
                                    items9 = new ArrayList<Item>();
                                    items9.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[9] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[9].put(newMonth, items9);
                                } else {
                                    items9.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[9].put(newMonth, items9);
                                }
                                month9 = newMonth;

                                break;

                            case 10:
                                if (month10 < newMonth) {
                                    items10 = new ArrayList<Item>();
                                    items10.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[10] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[10].put(newMonth, items10);

                                } else {
                                    items10.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[10].put(newMonth, items10);
                                }
                                month10 = newMonth;

                                break;

                            case 11:

                                // HashMap <Integer,ArrayList<Item>>[] itemsMonth

                                if (month11 < newMonth) {
                                    items11 = new ArrayList<Item>();
                                    items11.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[11] = new HashMap<Integer, ArrayList<Item>>();
                                    itemsMonth[11].put(month, items11);

                                } else {
                                    items11.add((Item) new EntryItem(acivityName, subTitle, _id, month, region));
                                    itemsMonth[11].put(month, items11);
                                }
                                month11 = newMonth;

                                break;
                        }


                    }

                }
                while (cursor.moveToNext());
            } finally {
                cursor.close();
            }

            /*Arrays.sort(itemsMonth, new Comparator <HashMap <Integer,ArrayList<Item>>>() {
                @Override
                public int compare(HashMap <Integer,ArrayList<Item>> entry1, HashMap <Integer,ArrayList<Item>> entry2) {
                    Integer month1 = (Integer) entry1.keySet().toArray()[0];
                    Integer month2 = (Integer) entry2.keySet().toArray()[0];
                    return month1.compareTo(month2);
                }
            });  //  // Map<String, Float> map = new TreeMap<String, Float>(yourMap);
*/

            for (int i = 0; i < 12; i++) {
                if (itemsMonth[i] != null) {

                    // int month = year*100+newMonth ;

                    int newMonth = (int) itemsMonth[i].keySet().toArray()[0] % 100;

                    String monthName = new DateFormatSymbols().getMonths()[newMonth];
                    String upperMonthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);
                    items.add(new SectionItem(upperMonthName, newMonth));

                    for (int j = 0; j < itemsMonth[i].get(itemsMonth[i].keySet().toArray()[0]).size(); j++) {
                        items.add(itemsMonth[i].get(itemsMonth[i].keySet().toArray()[0]).get(j));
                    }


                }
            }

            if (items.size() == 0) {
                informationMessage.setText(getResources().getText(R.string.activity_log_message));
                informationMessage.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        } else {
            informationMessage.setText(getResources().getText(R.string.activity_log_message));
            informationMessage.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }
    }

    private void onMyMapReady(GoogleMap googleMap) {

        myMap = googleMap;
        myMap.getUiSettings().setRotateGesturesEnabled(false);
        myMap.getUiSettings().setCompassEnabled(false);

        // Set OnMapLoadedCallback Listener.
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {
                showMyLocation();
            }
        });

        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        myMap.setMyLocationEnabled(true);
        myMap.getUiSettings().setMyLocationButtonEnabled(false);
    }


    // Call this method only when you have the permissions to view a user's location.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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
                    .zoom(11)                   // Sets the zoom
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
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }
}
