package ca.thegreattrail.ui.traildetail;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import butterknife.BindDimen;
import butterknife.ButterKnife;
import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.AmenityDBHelperTrail;
import ca.thegreattrail.data.model.db.AmenitySegment;
import ca.thegreattrail.ui.base.BaseTrailDrawingFragment;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.ui.map.MapFragment;
import ca.thegreattrail.utlis.Constants;
import static android.content.Context.LOCATION_SERVICE;

/**
 * Created by hardikfumakiya on 2017-03-09.
 */

public class DirectionTrailFragment extends BaseTrailDrawingFragment implements GoogleApiClient.ConnectionCallbacks {

    private static final String ARGUMENT_TRAIL_ID = "ARGUMENT_TRAIL_ID";

    String LANGUAGE_ENGLISH = "en";
    String LANGUAGE_FRENCH = "fr";
    String TRANSPORT_DRIVING = "driving";
    ArrayList<Step> routelist = new ArrayList<Step>();
    Location myLocation;
    RouteAdapter mAdapter;
    String direction_response = "";
    ListView direction_result;
    @BindDimen(R.dimen.animated_camera_padding) int animatedCameraPadding;

    public static final int REQUEST_ID_ACCESS_COURSE_FINE_LOCATION = 100;

    private String trailId = "";

    public static DirectionTrailFragment newInstance(String trailId) {
        Bundle args = new Bundle();
        args.putString(ARGUMENT_TRAIL_ID, trailId);

        DirectionTrailFragment fragment = new DirectionTrailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    protected boolean hasClickableSegments() {
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.get_direction, container, false);
        ButterKnife.bind(this, view);

        View footerView = inflater.inflate(R.layout.direction_footer, null, false);

//        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
//        activity.getSupportActionBar().setTitle(Html.fromHtml("<small>Directions to the Trail</small>"));

        mMapView = (MapView) view.findViewById(R.id.mapview);
        mMapView.onCreate(savedInstanceState);

        direction_result = (ListView) view.findViewById(R.id.direction_routeresult);

        direction_result.addFooterView(footerView);

        trailId = getArguments().getString(ARGUMENT_TRAIL_ID);
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(Objects.requireNonNull(getContext()))
                    .addConnectionCallbacks(this)
                    .addApi(LocationServices.API)
                    .build();
        }

        setupMapView();
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mMapView != null) {
            mMapView.onResume();
        }

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
                Log.e("LocationService", "Error while attempting MapView.onDestroy(), ignoring exception", e);
            }
        }
        super.onDestroy();
    }

    @Override
    protected void onMapReady() {
        super.onMapReady();
        highlightSelectedPolyline();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mMapView != null) {
            mMapView.onSaveInstanceState(outState);
        }

    }

    @Override
    protected void initializeMap(GoogleMap googleMap) {
        myMap = googleMap;
        myMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {

            @Override
            public void onMapLoaded() {

                askPermissionsAndShowMyLocation();

            }
        });
        myMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        myMap.getUiSettings().setZoomControlsEnabled(false);
        myMap.getUiSettings().setMapToolbarEnabled(false);

        if (MainActivity.listSegments == null) {
            myMap.setOnCameraIdleListener(null);
        } else {
            myMap.setOnCameraIdleListener(getCameraChangeListener());
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
        }
        myMap.setMyLocationEnabled(false);
        myMap.getUiSettings().setMyLocationButtonEnabled(false);
        showMyLocation();

        if (routelist.size() == 0)
            retrieveRoute();

    }

    private void askPermissionsAndShowMyLocation() {

        if (Build.VERSION.SDK_INT >= 23) {
            int accessCoarsePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION);
            int accessFinePermission = ContextCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION);


            if (accessCoarsePermission != PackageManager.PERMISSION_GRANTED || accessFinePermission != PackageManager.PERMISSION_GRANTED) {
                String[] permissions = new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION,
                        android.Manifest.permission.ACCESS_FINE_LOCATION};
                ActivityCompat.requestPermissions(getActivity(), permissions,REQUEST_ID_ACCESS_COURSE_FINE_LOCATION);

                return;
            }
        }

        // Show current location on Map.
        this.showMyLocation();
    }

    private void showMyLocation() {

        try {
            LocationManager locationManager = (LocationManager) getActivity().getSystemService(LOCATION_SERVICE);

            String locationProvider = getEnabledLocationProvider();

            if (locationProvider == null) {
                return;
            }

            myLocation = locationManager.getLastKnownLocation(locationProvider);
            if (myLocation != null) {
            } else {
                Toast.makeText(getActivity(), "Location not found!", Toast.LENGTH_LONG).show();

            }

        }
        // With Android API >= 23, need to catch SecurityException.
        catch (SecurityException e) {
            e.printStackTrace();
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
            Toast.makeText(getActivity(), "No location provider enabled!", Toast.LENGTH_LONG).show();
            Log.i("ActivityTrackerFragment", "No location provider enabled!");
            return null;
        }
        return bestProvider;
    }

    public GoogleMap.OnCameraIdleListener getCameraChangeListener() {
        return new GoogleMap.OnCameraIdleListener() {

            @Override
            public void onCameraIdle() {
                DirectionTrailFragment.super.onCameraIdle();
            }
        };
    }

    public void highlightSelectedPolyline() {

        if (getActivity() == null)
            return;

        MapFragment mapFragment = (MapFragment) MainActivity.fragment;
        if (mapFragment == null)
            return;

        int selectedSegmentId = mapFragment.getSelectedSegmentId();
        highLightSelectedPolyline(selectedSegmentId);
    }

    private void retrieveRoute() {
        try {
            AmenityDBHelperTrail db = new AmenityDBHelperTrail(getContext());
            AmenitySegment nearestAmenity = db.getNearestRestAmenity(trailId, myLocation);

            if (nearestAmenity != null) {
                StringBuilder sbValue = new StringBuilder(sbMethod(myLocation.getLatitude(), myLocation.getLongitude(), Double.parseDouble(nearestAmenity.getLat()), Double.parseDouble(nearestAmenity.getLng()), TRANSPORT_DRIVING));

                DirectionTask placesTask = new DirectionTask();
                placesTask.execute(sbValue.toString());

                Log.d("nearestAmenity", "" + nearestAmenity.getName_amenity());
            } else {
                Toast.makeText(getContext(), "Unable to get directions to this trail", Toast.LENGTH_SHORT).show();
//              activity.onBackPressed();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public StringBuilder sbMethod(double sourcelat, double sourcelog, double destlat, double destlog, String mode) {
        StringBuilder urlString = new StringBuilder();
        String lang = "";
        if (Locale.getDefault().getLanguage().equals("en")) {
            lang = LANGUAGE_ENGLISH;
        } else if (Locale.getDefault().getLanguage().equals("fr")) {
            lang = LANGUAGE_FRENCH;
        }

        if (mode == null)
            mode = "driving";

        urlString.append("https://maps.googleapis.com/maps/api/directions/json");
        urlString.append("?origin=");// from
        urlString.append(Double.toString(sourcelat));
        urlString.append(",");
        urlString.append(Double.toString(sourcelog));
        urlString.append("&destination=");// to
        urlString.append(Double.toString(destlat));
        urlString.append(",");
        urlString.append(Double.toString(destlog));
        urlString.append("&sensor=true&mode=" + mode + "&alternatives=true&language=" + lang);
        urlString.append("&key=" + Constants.API_KEY_SEARCH);

        return urlString;
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        myLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    private void drawPath(boolean withSteps) {

        try {
            //Tranform the string into a json object
            JSONObject json = new JSONObject(direction_response);
            JSONArray routeArray = json.getJSONArray("routes");
            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes.getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            PolylineOptions options = new PolylineOptions().width(9).color(Color.YELLOW).geodesic(true);
            LatLng latLng, startPoint = null, endPoint = null;
            for (int z = 0; z < list.size() - 1; z++) {
                latLng = list.get(z + 1);
                options.add(list.get(z));
                options.add(latLng);
            }

            myMap.addPolyline(options);

            if (withSteps) {
                JSONArray arrayLegs = routes.getJSONArray("legs");
                JSONObject legs = arrayLegs.getJSONObject(0);
                JSONArray stepsArray = legs.getJSONArray("steps");

                for (int i = 0; i < stepsArray.length(); i++) {
                    Step step = new Step(stepsArray.getJSONObject(i));
                    if (i == 0) {
                        startPoint = step.start_location;
                        myMap.addMarker(new MarkerOptions()
                                .position(startPoint)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_poi_marker)));
                    } else if (i == (stepsArray.length() - 1)) {
                        endPoint = step.end_location;
                        myMap.addMarker(new MarkerOptions()
                                .position(endPoint)
                                .icon(BitmapDescriptorFactory.fromResource(R.drawable.yellow_poi_marker)));
                    }

                    routelist.add(step);
                }
                if (routelist != null && routelist.size() > 0) {
                    mAdapter = new RouteAdapter(getContext(), routelist);
                    direction_result.setAdapter(mAdapter);

                    LatLngBounds.Builder b = new LatLngBounds.Builder();
                    b.include(startPoint);
                    b.include(endPoint);

                    LatLngBounds bounds = b.build();
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, animatedCameraPadding);
                    myMap.animateCamera(cu);
                }
            }

        } catch (JSONException e) {

        }
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private class DirectionTask extends AsyncTask<String, Integer, String> {

        String datas = null;
        ProgressDialog pDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(getContext());
            pDialog.setMessage("Loading...");
            pDialog.show();
        }

        // Invoked by execute() method of this object
        @Override
        protected String doInBackground(String... url) {

            try {
                InputStream iStream = null;
                HttpURLConnection urlConnection = null;
                try {
                    URL urls = new URL(url[0]);

                    // Creating an http connection to communicate with url
                    urlConnection = (HttpURLConnection) urls.openConnection();

                    // Connecting to url
                    urlConnection.connect();

                    // Reading data from url
                    iStream = urlConnection.getInputStream();

                    BufferedReader br = new BufferedReader(new InputStreamReader(iStream));

                    StringBuffer sb = new StringBuffer();

                    String line = "";

                    while ((line = br.readLine()) != null) {
                        sb.append(line);
                    }

                    datas = sb.toString();

                    br.close();

                } catch (Exception e) {
                    Log.d("Exception", e.toString());
                } finally {
                    iStream.close();
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            //Log.d("Background Task", ""+datas);
            return datas;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(String result) {
            direction_response = result;
            drawPath(true);
            pDialog.dismiss();
        }
    }

    /**
     * Class that represent every step of the directions. It store distance, location and instructions
     */
    class Step {
        String distance;
        LatLng start_location, end_location;

        String instructions;
        String duration;
        String maneuver;

        Step(JSONObject stepJSON) {
            JSONObject startLocation, endLocation;
            try {

                distance = stepJSON.getJSONObject("distance").getString("text");
                duration = stepJSON.getJSONObject("duration").getString("text");

                if (stepJSON.has("maneuver"))
                    maneuver = stepJSON.getString("maneuver");

                startLocation = stepJSON.getJSONObject("start_location");
                start_location = new LatLng(startLocation.getDouble("lat"), startLocation.getDouble("lng"));

                endLocation = stepJSON.getJSONObject("end_location");
                end_location = new LatLng(endLocation.getDouble("lat"), endLocation.getDouble("lng"));

                instructions = stepJSON.getString("html_instructions");
                //Log.d("instructions","i: "+instructions);


            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
