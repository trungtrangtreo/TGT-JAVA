package ca.thegreattrail.data.service;

import android.app.IntentService;
import android.content.Intent;
import ca.thegreattrail.ui.tracker.TrackingManager;

public class TrackingInfoSavingService extends IntentService {

    public TrackingInfoSavingService() {
        super(TrackingInfoSavingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        TrackingManager trackingManager = TrackingManager.getInstance();
        trackingManager.persistTrackingInfo(this);
    }
}
