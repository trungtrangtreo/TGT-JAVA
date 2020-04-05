package ca.thegreattrail.ui.tracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import java.util.Date;

import ca.thegreattrail.data.service.AchievementsGrantingService;
import ca.thegreattrail.data.service.TrackingInfoSavingService;
import ca.thegreattrail.utlis.DateUtils;
import ca.thegreattrail.utlis.TrailUtility;
import io.realm.RealmList;

public class TrackingManager {

    // TODO: to be returned to 1 mins
    private static final long TRACK_SAVING_INFO_LISTENER_TRIGGER_PERIOD = 10 * 1000; // 10 secs
    // TODO: to be returned to 5 mins
    private static final long ACHIEVEMENT_CHECKING_LISTENER_TRIGGER_PERIOD = 15 * 1000; // 20 secs

    private static final int REQUEST_CODE_TRACK_SAVING_INFO_LISTENER = 0;
    private static final int REQUEST_CODE_ACHIEVEMENTS_CHECKING_LISTENER = 1;

    private static final int MIN_DISTANCE_TO_BE_ON_THE_GREAT_TRAIL = 10; // In meters
    private static TrackingManager sInstance;
    private boolean isTracking;
    private boolean isTrackingListenersRegistered;
    private UserTrackingInfo currentUserTrackingInfo;
    private ProvincesDao provincesDao;
    private UserTrackingInfoDao userTrackingInfoDao;

    private TrackingManager() {
        userTrackingInfoDao = UserTrackingInfoDao.getInstance();
        provincesDao = ProvincesDao.getInstance();
    }

    public static TrackingManager getInstance() {

        if (sInstance == null)
            sInstance = new TrackingManager();
        return sInstance;
    }

    /**
     * load tracking info initially
     */
    public void loadTrackingInfo(Context context) {

        if (currentUserTrackingInfo != null)
            return; // then it is loaded
        currentUserTrackingInfo = userTrackingInfoDao.getDefaultUserTrackInfo(context);

        if (currentUserTrackingInfo == null) {
            userTrackingInfoDao.insertIfNotExist(context);
            currentUserTrackingInfo = userTrackingInfoDao.getDefaultUserTrackInfo(context);
        }
    }

    public void startTrackingListeners(Context context) {

        if (isTracking || isTrackingListenersRegistered)
            return;
        registerTrackingSavingInfoListener(context);
        registerAchievementsCheckingListener(context);
        isTracking = true;
        isTrackingListenersRegistered = true;
    }

    public boolean isTracking() {
        return isTracking;
    }

    public void setTracking(boolean tracking) {
        isTracking = tracking;
    }

    private void registerTrackingSavingInfoListener(Context context) {
        Date firstTriggerAfter = DateUtils.addMilliSecondsToCurrentTime((int) TRACK_SAVING_INFO_LISTENER_TRIGGER_PERIOD);

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = getPendingIntent(context, REQUEST_CODE_TRACK_SAVING_INFO_LISTENER);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    firstTriggerAfter.getTime(),
                    TRACK_SAVING_INFO_LISTENER_TRIGGER_PERIOD,
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void registerAchievementsCheckingListener(Context context) {
        Date firstTriggerAfter = DateUtils.addMilliSecondsToCurrentTime((int) ACHIEVEMENT_CHECKING_LISTENER_TRIGGER_PERIOD);

        try {
            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
            PendingIntent pendingIntent = getPendingIntent(context, REQUEST_CODE_ACHIEVEMENTS_CHECKING_LISTENER);
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP,
                    firstTriggerAfter.getTime(),
                    ACHIEVEMENT_CHECKING_LISTENER_TRIGGER_PERIOD,
                    pendingIntent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public UserTrackingInfo getCurrentUserTrackingInfo() {
        return currentUserTrackingInfo;
    }

    public String getCurrentProvinceName(Context context, Location location) {
        double lat = location.getLatitude();
        double lng = location.getLongitude();

        // Don't return any province, in case you're away from the trail
        if (!isOnTheGreatTrial(location))
            return null;
        Log.d("Great Trail", "on the trail");
        return TrailUtility.currentProvince(context, new LatLng(lat, lng));
    }

    /**
     * Update tracking info in memory without persisting it as this is called frequently and it will be overhead to keep persisting it
     */
    public void addCoveredDistance(Location location, float distance) {

        if (currentUserTrackingInfo == null || !isTracking)
            return;
        currentUserTrackingInfo.addCoveredDistance(distance);
    }

    public void addAchievedElevation(Location location, float elevation) {

        if (currentUserTrackingInfo == null || !isTracking || elevation <= 0)
            return;
        currentUserTrackingInfo.addAchievedElevation(elevation);
    }

    public void addTrackedTime(Location location, float time) {

        if (currentUserTrackingInfo == null || !isTracking)
            return;
        currentUserTrackingInfo.addTrackedTime(time);
    }

    private boolean isOnTheGreatTrial(Location location) {
        double distanceFromTrail = TrailUtility.distanceFromTrail(new LatLng(location.getLatitude(), location.getLongitude()));

        if (distanceFromTrail <= MIN_DISTANCE_TO_BE_ON_THE_GREAT_TRAIL)
            return true;
        return false;
    }

    public void addOrUpdateProvince(Province province) {

        if (currentUserTrackingInfo == null || !isTracking)
            return;
        RealmList<Province> provinces = currentUserTrackingInfo.getVisitedProvinces();
        boolean provinceExisted = false;

        for (Province existed : provinces) {

            if (existed.getName().equals(province.getName())) {
                existed.updateNumberOfVisits();
                existed.setLastVisitAt(province.getLastVisitAt());
                provinceExisted = true;
                break;
            }
        }

        if (!provinceExisted) {
            province.setUserTrackingInfo(currentUserTrackingInfo);
            province.setNumberOfVisits(1);
            currentUserTrackingInfo.addVisitedProvince(province);
        }
    }

    /**
     * Stores tracking info on Realm and persists it
     */
    public void persistTrackingInfo(Context context) {

        if (currentUserTrackingInfo == null)
            return; // Data is not loaded
        persistTrackingProvinces(context);
        userTrackingInfoDao.updateAllTrackingInfo(context, currentUserTrackingInfo);
    }

    private void persistTrackingProvinces(Context context) {
        RealmList<Province> provinces = currentUserTrackingInfo.getVisitedProvinces();

        for (Province province : provinces) {
            provincesDao.insertOrUpdate(context, province);
        }
    }

    public void stopTrackingListeners(Context context) {
        unregisterAlarmManagerEvents(context);
        isTracking = false;
        isTrackingListenersRegistered = false;
        persistTrackingInfo(context);
    }

    private void unregisterAlarmManagerEvents(Context context) {
        AlarmManager alarms = (AlarmManager) context.getSystemService(
                Context.ALARM_SERVICE);
        PendingIntent trackSavingServicePendingIntent = getPendingIntent(context, REQUEST_CODE_TRACK_SAVING_INFO_LISTENER);
        PendingIntent achievementsCheckingServicePendingIntent = getPendingIntent(context, REQUEST_CODE_ACHIEVEMENTS_CHECKING_LISTENER);
        alarms.cancel(trackSavingServicePendingIntent);
        alarms.cancel(achievementsCheckingServicePendingIntent);
    }

    private PendingIntent getPendingIntent(Context context, int requestCode) {
        Intent intent;

        if (requestCode == REQUEST_CODE_TRACK_SAVING_INFO_LISTENER) {
            intent = new Intent(context, TrackingInfoSavingService.class);
        } else {
            intent = new Intent(context, AchievementsGrantingService.class);
        }
        return PendingIntent.getService(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_CANCEL_CURRENT);
    }
}
