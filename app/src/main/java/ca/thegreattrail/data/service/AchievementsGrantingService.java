package ca.thegreattrail.data.service;

import android.app.IntentService;
import android.content.Intent;

public class AchievementsGrantingService extends IntentService {

    public AchievementsGrantingService() {
        super(AchievementsGrantingService.class.getName());
    }

    @Override
    protected void onHandleIntent(Intent intent) {
//        AchievementsManager achievementsManager = AchievementsManager.getInstance();
//        achievementsManager.checkUnlockOfAchievements(this);
    }
}
