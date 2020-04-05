package ca.thegreattrail.data.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import ca.thegreattrail.data.local.db.ActivityDBHelper;
import ca.thegreattrail.ui.tracker.Province;
import ca.thegreattrail.ui.tracker.TrackerFragment;
import ca.thegreattrail.ui.tracker.TrackingManager;
import ca.thegreattrail.utlis.Constants;
import static ca.thegreattrail.utlis.Constants.SMALLEST_DISPLACEMENT;

public class TrackService extends Service implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    public static String TAG = "LocationService";

    private boolean currentlyProcessingLocation = false;
    private LocationRequest locationRequest;
    private GoogleApiClient googleApiClient;
    private Boolean servicesAvailable = false;

    private long activityId = 0;

    private PowerManager.WakeLock mWakeLock;


    private SharedPreferences preferences;

    private double latitudeOld;
    private double longitudeOld;
    private double lastElevation;
    private long lastUpdateTime;


    @Override
    public void onCreate() {
        super.onCreate();
        servicesAvailable = servicesConnected();
        setUpLocationClientIfNeeded();
        preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
    }

    @SuppressLint("InvalidWakeLockTag")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // if we are currently trying to get a location and the alarm manager has called this again,
        // no need to start processing a new location.

        super.onStartCommand(intent, flags, startId);

        PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);

        /*
        WakeLock is reference counted so we don't want to create multiple WakeLocks. So do a check before initializing and acquiring.
        This will fix the "java.lang.Exception: WakeLock finalized while still held: MyWakeLock" error that you may find.
        */
        if (this.mWakeLock == null) { //**Added this
            this.mWakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
        }

        if (!this.mWakeLock.isHeld()) { //**Added this
            this.mWakeLock.acquire();
        }

        if (!servicesAvailable || googleApiClient.isConnected() || currentlyProcessingLocation)
            return START_STICKY;

        setUpLocationClientIfNeeded();

        if (!googleApiClient.isConnected() || !googleApiClient.isConnecting() && !currentlyProcessingLocation) {
            Log.i(TAG, DateFormat.getDateTimeInstance().format(new Date()) + ": Started Service in OnStartCommand");
            currentlyProcessingLocation = true;
            googleApiClient.connect();
        }


        return START_STICKY;
    }


    private void setUpLocationClientIfNeeded() {
        if (googleApiClient == null)
            buildGoogleApiClient();
    }

    protected synchronized void buildGoogleApiClient() {
        this.googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }


    private boolean servicesConnected() {

        // Check that Google Play services is available

        GoogleApiAvailability googleAPI = GoogleApiAvailability.getInstance();
        int resultCode = googleAPI.isGooglePlayServicesAvailable(this);

        // If Google Play services is available
        if (ConnectionResult.SUCCESS == resultCode) {

            return true;
        } else {

            return false;
        }
    }


    @Override
    public void onDestroy() {

        this.currentlyProcessingLocation = false;

        if (this.servicesAvailable && this.googleApiClient != null) {
            this.googleApiClient.unregisterConnectionCallbacks(this);
            this.googleApiClient.unregisterConnectionFailedListener(this);
            this.googleApiClient.disconnect();
            // Destroy the current location client
            this.googleApiClient = null;
        }


        if (this.mWakeLock != null) {
            this.mWakeLock.release();
            this.mWakeLock = null;
        }


        super.onDestroy();

        //  Toast.makeText(this, "Service Destroyed", Toast.LENGTH_SHORT).show();
        Log.i(TAG, "Service destroyed succefully");

    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {

        // Toast.makeText( getApplicationContext(), "Interception du changement", Toast.LENGTH_SHORT ).show();
        Boolean writeOnBD = preferences.getBoolean("WriteOnBD", false);
        String state = preferences.getString("State", "Stop");

        Log.i(TAG, "Interception:" + (location != null) + "  And " + (writeOnBD));

        if (location != null && writeOnBD) {
            // Prepare data (latitude, longitude, ......)
            Log.i(TAG, "altitude:" + location.getAltitude() + ", Speed:" + location.getSpeed());
            double latitudeNew = location.getLatitude();
            double longitudeNew = location.getLongitude();
            double altitudeNew = location.getAltitude();
            //double speed = location.getSpeed();
            double accuracy = location.getAccuracy();

            Location locationNew = new Location("");
            locationNew.setLatitude(latitudeNew);
            locationNew.setLongitude(longitudeNew);

            Location locationOld = new Location("");
            locationOld.setLatitude(latitudeOld);
            locationOld.setLongitude(longitudeOld);

            float distance = locationNew.distanceTo(locationOld);

            //  Toast.makeText( getApplicationContext(), "distance:"+distance+", Speed:"+location.getSpeed(), Toast.LENGTH_LONG ).show();


            Log.i(TAG, "TrackSerice : position:  latitude = " + location.getLatitude() + ", " + location.getLongitude() + " accuracy: " + location.getAccuracy() + ", Speed:" + location.getSpeed());

            // Toast.makeText( getApplicationContext(), "distance:"+distance+", accuracy:"+accuracy, Toast.LENGTH_LONG ).show();

            // TODO: Ensure that we'really need our tracking logic to be inside
            if (writeOnBD && distance > SMALLEST_DISPLACEMENT && accuracy < 20 && activityId != 0 && state != "Stop") {
                //   Toast.makeText( getApplicationContext(), "Send .... distance:"+distance+", Speed:"+location.getSpeed(), Toast.LENGTH_LONG ).show();

                ActivityDBHelper db = new ActivityDBHelper(this);
                db.writeLocation(activityId, latitudeNew, longitudeNew, altitudeNew, state);
                db.close();

                LatLng coordinate = new LatLng(latitudeNew, longitudeNew);
//                TrackerFragment.coordinatesList.add(coordinate);

                // Send information to ActivityTrack
                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                dateFormat.setTimeZone(TimeZone.getDefault());
                Date date = new Date(location.getTime());

                // To be sent to Activity Tracker Fragment as there is broadcast receiver is registered using same TAG
                Intent sendLocationUpdatesToTrackerFragment = new Intent(TAG);
                sendLocationUpdatesToTrackerFragment.putExtra("latitude", location.getLatitude());
                sendLocationUpdatesToTrackerFragment.putExtra("longitude", location.getLongitude());
                sendLocationUpdatesToTrackerFragment.putExtra("altitude", location.getAltitude());
                try {
                    sendLocationUpdatesToTrackerFragment.putExtra("date", URLEncoder.encode(dateFormat.format(date), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                }

                this.sendBroadcast(sendLocationUpdatesToTrackerFragment);

                TrackingManager trackingManager = TrackingManager.getInstance();

                if (trackingManager.isTracking()) {
                    // TODO: To be changed, checking on zero for latitude
                    if (latitudeOld != 0 && lastUpdateTime != 0) {
                        // TODO: to be removed, this supposed to be check inside AchievementsGrantingService and AchievementsManager
                        // AchievementsManager.getsInstance(getApplicationContext()).checkProximityToGreatTrail(new LatLng(location.getLatitude(), location.getLongitude()));
                        // Collect Data with tracking manager
                        long timeTracked = (System.currentTimeMillis() - lastUpdateTime) / 1000;
                        trackingManager.addCoveredDistance(location, distance);
                        trackingManager.addTrackedTime(location, timeTracked);
                    }
                    String provinceName = trackingManager.getCurrentProvinceName(this, location);
                    Log.d("Great Trail", "current province " + provinceName);

                    if (!TextUtils.isEmpty(provinceName)) {
                        Province province = new Province(provinceName, new Date());
                        trackingManager.addOrUpdateProvince(province);
                    }
                    lastUpdateTime = System.currentTimeMillis();
                }
                latitudeOld = latitudeNew;
                longitudeOld = longitudeNew;
                lastElevation = altitudeNew;
            }

        } else {
            Log.i(TAG, "position is null");
        }

    }

    private void stopLocationUpdates() {
        if (googleApiClient != null && googleApiClient.isConnected()) {
            googleApiClient.disconnect();
        }
    }

    /**
     * Called by Location Services when the request to connect the
     * client finishes successfully. At this point, you can
     * request the current location or start periodic updates
     */
    @Override
    public void onConnected(Bundle bundle) {
        ActivityDBHelper db = new ActivityDBHelper(this);
        activityId = db.giveMeLastId();
        db.close();


        // Create the LocationRequest object
        locationRequest = LocationRequest.create();
        // Use high accuracy
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);    //  PRIORITY_BALANCED_POWER_ACCURACY
        // Set the update interval to 10 seconds
        locationRequest.setInterval(Constants.UPDATE_INTERVAL);
        // TODO: check if we need that as we only depend on change on displacement
        // Set the fastest update interval to 10 second
        // locationRequest.setFastestInterval(Constants.FASTEST_INTERVAL);
        locationRequest.setSmallestDisplacement(SMALLEST_DISPLACEMENT);

        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest, this);
        // TODO: I think this block of code is useless and is configured in wrong way, please ensure from that
        Intent locationUpdatedIntent = new Intent(this, TrackerFragment.class);

        LocationServices.FusedLocationApi.requestLocationUpdates(
                googleApiClient, locationRequest,
                PendingIntent.getService(this, 0, locationUpdatedIntent, 0));
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.e(TAG, "onConnectionFailed");

        stopLocationUpdates();
        stopSelf();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection has been suspend");
    }


}