package ca.thegreattrail.ui.trackerlog;

import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelper;
import ca.thegreattrail.data.model.other.Item;
import ca.thegreattrail.ui.tracker.TrackerFragment;

import static android.content.Context.LOCATION_SERVICE;
import static ca.thegreattrail.ui.trackerlog.ActivityDetailsFragment.REQUEST_ID_ACCESS_COURSE_FINE_LOCATION;

public class LogTrackerFragment extends Fragment implements NavigationView.OnNavigationItemSelectedListener, LocationListener, AdapterView.OnItemClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{


    public static LogTrackerFragment instance = null;
    private MapView mMapView;
    private GoogleMap myMap;
    private GoogleApiClient mGoogleApiClient;
    private FloatingActionButton floatingActionButton;
    private ArrayList<Item> items = new ArrayList<Item>();
    private ListView listView;
    private TextView informationMessage ;

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

    public LogTrackerFragment() {
        // Required empty public constructor
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        if(currentTab.equals("MapFragment") && mapfragStack.containsKey("MapSearchFragment")){
//            menu.clear();
//        }
//        else if(currentTab.equals("MeasureFragment") && measurefragStack.containsKey("MeasureSearchFragment")){
//            menu.clear();
//        }
//        else  if(currentTab.equals("ActivityTrackerFragment")){
//            if(trackerfragStack.containsKey("TrackerSearchFragment") || trackerfragStack.containsKey("ActivityLogFragment") || trackerfragStack.containsKey("ActivityTrackerFragment")){
//                menu.clear();
//            }
//        }
//    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

/*@Override
public void onPrepareOptionsMenu(Menu menu) {
    super.onPrepareOptionsMenu(menu);
    MenuItem item= menu.findItem(R.id.search);
    item.setVisible(false);
}*/

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.tracker_log_activity, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapview1);
        mMapView.onCreate(savedInstanceState);
        listView = (ListView) view.findViewById(R.id.list_Log);
        informationMessage = (TextView) view.findViewById(R.id.informationMessage);
        informationMessage.setText(getResources().getText(R.string.activity_log_message));

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();
        }

//        floatingActionButton = (FloatingActionButton) view.findViewById(R.id.location_toggle_fab);
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
//                    LatLng latLng = new LatLng(mLastLocation.getLatitude(), mLastLocation.getLongitude());
//                    myMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 11));
//                }
//            }
//        });



//        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        // (AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
//        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
//        activity.getSupportActionBar().setTitle(getResources().getString(R.string.activity_log));


        //setHasOptionsMenu(true);  // hide toolbar*/

        //  loadActivities();
        view.setFocusableInTouchMode(true);
        view.requestFocus();
//        view.setOnKeyListener(new View.OnKeyListener() {
//            @Override
//            public boolean onKey(View v, int keyCode, KeyEvent event)   {
//                if (keyCode == KeyEvent.KEYCODE_BACK) {
//                   // Toast.makeText(getContext(),"ActivityLogFragment back",Toast.LENGTH_SHORT).show();
////                    Log.d("ActivityLogFragment","back");
////                    ActivityTrackerFragment actTracker= ActivityTrackerFragment.getInstance();
////                    FragmentTransaction transaction=getActivity().getSupportFragmentManager().beginTransaction();
////                    transaction.replace(R.id.trackerSearchLayout,actTracker);
////
////                    transaction.commit();
//
//                    return true;
//                }
//                return false;
//            }
//        });

        return view;

    }



    public int compare(Item s1, Item s2) {
        return (s1.month - s2.month) ;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        adapter = new EntryAdapter(getActivity(), items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position,long id) {

        if(!items.get(position).isSection()){

            EntryItem item = (EntryItem)items.get(position);

            pushActivityLogFragmentToStack();

            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

            ActivityDetailsFragment activityDetailsFragment =  (ActivityDetailsFragment) fragmentManager.findFragmentByTag("ActivityDetailsFragment");
            if (activityDetailsFragment== null) {
                activityDetailsFragment=ActivityDetailsFragment.newInstance();
            }

            activityDetailsFragment.setActivityId(item._id);

            replaceFragment(R.id.trackerSearchLayout, activityDetailsFragment);



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

    private void replaceFragment(int resourceID, Fragment actTrackFragment) {
        FragmentTransaction mFragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        mFragmentTransaction
                .replace(resourceID, actTrackFragment)
                .commit();
    }

    private void pushActivityLogFragmentToStack() {
        String actlogtag = "ActivityLogFragment";
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        Fragment actlogfragment = fragmentManager.findFragmentById(R.id.trackerSearchLayout);

        if (actlogfragment instanceof LogTrackerFragment) {
            TrackerFragment.trackerfragStack.put(actlogtag, actlogfragment);
            TrackerFragment.trackerfragTagStack.push(actlogtag);
        }
    }

    public void updateListView(){
        loadActivities();
        listView.invalidateViews(); // update listView with new values

        Log.e("LocationService", "Appelle de Log et la valeur de  ------------------------------------------------------------------------------------*************************** taille = "+items.size() );
    }


//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        menu.clear();
//    }


    @Override
    public void onStart() {
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
    public void onStop() {
        mGoogleApiClient.disconnect();
        //  getActivity().unregisterReceiver(locationReceiver);
        super.onStop();


    }



    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }

        loadActivities();
    }



    @Override
    public void onPause() {
        if (mMapView != null) {
            mMapView.onPause();
        }
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


    public void loadActivities() {

        HashMap<Integer,ArrayList<Item>>[] itemsMonth  = new HashMap[12];

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

        int month0 = -1 ;
        int month1 = -1 ;
        int month2 = -1 ;
        int month3 = -1 ;
        int month4 = -1 ;
        int month5 = -1 ;
        int month6 = -1 ;
        int month7 = -1 ;
        int month8 = -1 ;
        int month9 = -1 ;
        int month10 = -1 ;
        int month11 = -1 ;



        items.clear();
        Cursor cursor = null;
        ActivityDBHelper db = new ActivityDBHelper(getActivity());
        cursor = db.getAllActivities();

        if( cursor != null && cursor.moveToFirst() ) {

            informationMessage.setVisibility(View.GONE);
            listView.setVisibility(View.VISIBLE);

            try {

                int newMonth = -1;
                //  int oldMonth = -1;
                int year = -1;

                do {
                    long _id = cursor.getInt(cursor.getColumnIndex("_id"));
                    String start_time  = cursor.getString(cursor.getColumnIndex("Start_time"));


                    final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                    final Calendar c = Calendar.getInstance();
                    try {
                        c.setTime(df.parse(start_time));
                        year = c.get(Calendar.YEAR);
                        newMonth = c.get(Calendar.MONTH);
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }

                    int month = year*100+newMonth ;

                    int region = cursor.getInt(cursor.getColumnIndex("Region"));
                    String acivityName = cursor.getString(cursor.getColumnIndex("Activity_name"));
                    float distance  = cursor.getFloat(cursor.getColumnIndex("Distance"));
                    float elevation  = cursor.getFloat(cursor.getColumnIndex("Elevation"));
                    String time = cursor.getString(cursor.getColumnIndex("Time"));

                    String subTitle = start_time+"     "+distance+" km     "+elevation+" m     "+time+" min";


                    if (acivityName.equalsIgnoreCase("No Name") && distance == 0.0 && elevation == 0.0 && region == 0){
                    }
                    else{

                        switch (newMonth) {
                            case 0 :
                                if (month0 < newMonth){
                                    items0 = new ArrayList<Item>();
                                    items0.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[0] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[0].put(newMonth,items0);
                                }
                                else {
                                    items0.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[0].put(newMonth,items0);
                                }
                                month0 = newMonth;
                                break;

                            case 1 :
                                if (month1 < newMonth){
                                    items1 = new ArrayList<Item>();
                                    items1.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[1] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[1].put(newMonth,items1);
                                }
                                else {
                                    items1.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[1].put(newMonth,items1);
                                }
                                month1 = newMonth;
                                break;


                            case 2 :
                                if (month2 < newMonth){
                                    items2 = new ArrayList<Item>();
                                    items2.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[2] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[2].put(newMonth,items2);

                                }
                                else {
                                    items2.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[2].put(newMonth,items2);
                                }
                                month2 = newMonth;

                                break;

                            case 3 :
                                if (month3 < newMonth){
                                    items3 = new ArrayList<Item>();
                                    items3.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[3] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[3].put(newMonth,items3);

                                }
                                else {
                                    items3.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[3].put(newMonth,items3);
                                }
                                month3 = newMonth;

                                break;

                            case 4 :
                                if (month4 < newMonth){
                                    items4 = new ArrayList<Item>();
                                    items4.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[4] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[4].put(newMonth,items4);

                                }
                                else {
                                    items4.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[4].put(newMonth,items4);
                                }
                                month4 = newMonth;

                                break;

                            case 5 :
                                if (month5 < newMonth){
                                    items5 = new ArrayList<Item>();
                                    items5.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[5] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[5].put(newMonth,items5);

                                }
                                else {
                                    items5.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[5].put(newMonth,items5);
                                }
                                month5 = newMonth;

                                break;

                            case 6 :
                                if (month6 < newMonth){
                                    items6 = new ArrayList<Item>();
                                    items6.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[6] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[6].put(newMonth,items6);

                                }
                                else {
                                    items6.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[6].put(newMonth,items6);
                                }
                                month6 = newMonth;

                                break;

                            case 7 :
                                if (month7 < newMonth){
                                    items7 = new ArrayList<Item>();
                                    items7.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[7] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[7].put(newMonth,items7);
                                }
                                else {
                                    items7.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[7].put(newMonth,items7);
                                }
                                month7 = newMonth;

                                break;

                            case 8 :
                                if (month8 < newMonth){
                                    items8 = new ArrayList<Item>();
                                    items8.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[8] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[8].put(newMonth,items8);

                                }
                                else {
                                    items8.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[8].put(newMonth,items8);
                                }
                                month8 = newMonth;

                                break;

                            case 9 :
                                if (month9 < newMonth){
                                    items9 = new ArrayList<Item>();
                                    items9.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[9] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[9].put(newMonth,items9);
                                }
                                else {
                                    items9.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[9].put(newMonth,items9);
                                }
                                month9 = newMonth;

                                break;

                            case 10 :
                                if (month10 < newMonth){
                                    items10 = new ArrayList<Item>();
                                    items10.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[10] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[10].put(newMonth,items10);

                                }
                                else {
                                    items10.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[10].put(newMonth,items10);
                                }
                                month10 = newMonth;

                                break;

                            case 11 :

                                // HashMap <Integer,ArrayList<Item>>[] itemsMonth

                                if (month11 < newMonth){
                                    items11 = new ArrayList<Item>();
                                    items11.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[11] = new HashMap <Integer,ArrayList<Item>>();
                                    itemsMonth[11].put(month,items11);

                                }
                                else {
                                    items11.add((Item) new EntryItem(acivityName, subTitle,_id,month,region));
                                    itemsMonth[11].put(month,items11);
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

            for (int i=0 ; i<12 ; i++) {
                if(itemsMonth[i]!= null){

                    // int month = year*100+newMonth ;

                    int newMonth = (int) itemsMonth[i].keySet().toArray()[0] % 100;

                    String monthName = new DateFormatSymbols().getMonths()[newMonth];
                    String upperMonthName = monthName.substring(0,1).toUpperCase() + monthName.substring(1);
                    items.add(new SectionItem(upperMonthName,newMonth));

                    for(int j=0; j<itemsMonth[i].get(itemsMonth[i].keySet().toArray()[0]).size(); j++){
                        items.add(itemsMonth[i].get(itemsMonth[i].keySet().toArray()[0]).get(j) );
                    }


                }
            }

            if(items.size() == 0) {
                informationMessage.setText(getResources().getText(R.string.activity_log_message));
                informationMessage.setVisibility(View.VISIBLE);
                listView.setVisibility(View.GONE);
            }
        }
        else {
            informationMessage.setText(getResources().getText(R.string.activity_log_message));
            informationMessage.setVisibility(View.VISIBLE);
            listView.setVisibility(View.GONE);
        }


    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getActivity().getMenuInflater();
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
                askPermissionsAndShowMyLocation();

            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }


    private void askPermissionsAndShowMyLocation() {

        // With API> = 23, you have to ask the user for permission to view their location.
        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission
                    = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission
                    = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED
                    || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                // The Permissions to ask user.
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION};
                // Show a dialog asking the user to allow the above permissions.
                ActivityCompat.requestPermissions(getActivity(), permissions,
                        REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.
        this.showMyLocation();
    }


    // Call this method only when you have the permissions to view a user's location.
    private void showMyLocation() {

        LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

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




    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }


    @Override
    public void onLocationChanged(Location location) {
    }



    public void onConnectionFailed(ConnectionResult connectionResult) {
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

}
