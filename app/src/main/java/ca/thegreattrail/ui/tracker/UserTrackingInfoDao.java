package ca.thegreattrail.ui.tracker;

import android.content.Context;
import java.util.ArrayList;
import io.realm.Realm;
import io.realm.RealmQuery;

public class UserTrackingInfoDao extends GenericDao<UserTrackingInfo> {

    private static UserTrackingInfoDao userTrackingInfoDao;

    private UserTrackingInfoDao() {
        super();
    }

    public static UserTrackingInfoDao getInstance() {

        if (userTrackingInfoDao == null)
            userTrackingInfoDao = new UserTrackingInfoDao();
        return userTrackingInfoDao;
    }

    public void insertIfNotExist(final Context context) {
        Realm realm = getRealmInstance(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                try {
                    ArrayList<UserTrackingInfo> userTrackingInfoEntries = findAll(context);

                    if (userTrackingInfoEntries != null && userTrackingInfoEntries.size() > 0)
                        return;
                    // Currently our implementation has one tuple of user tracking info
                    UserTrackingInfo userTrackingInfo = new UserTrackingInfo();
                    userTrackingInfo.setId(UserTrackingInfo.DEFAULT_ID);
                    realm.insertOrUpdate(userTrackingInfo);
                } catch (IllegalArgumentException exception) {
                    exception.printStackTrace();
                }
            }
        });
    }

    public UserTrackingInfo getDefaultUserTrackInfo(final Context context) {
        // Currently our implementation has one tuple of user tracking info
        ArrayList<UserTrackingInfo> userTrackingInfoEntries = findWithId(context, UserTrackingInfo.ID_FIELD_NAME, UserTrackingInfo.DEFAULT_ID);

        if (userTrackingInfoEntries != null && userTrackingInfoEntries.size() > 0)
            return userTrackingInfoEntries.get(0);
        return null;
    }

    public void updateTotalCoveredDistance(final Context context, final float totalCoveredDistance) {
        Realm realm = getRealmInstance(context);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                // As currently we only has one tuple of user tracking info
                RealmQuery<UserTrackingInfo> query = bgRealm.where(UserTrackingInfo.class);
                final UserTrackingInfo userTrackingInfo = query.findFirst();

                if (userTrackingInfo != null) {
                    userTrackingInfo.setTotalCoveredDistance(totalCoveredDistance);
                }
            }
        });
    }

    public void updateTotalAchievedElevation(final Context context, final float totalAchievedElevation) {
        Realm realm = getRealmInstance(context);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                // As currently we only has one tuple of user tracking info
                RealmQuery<UserTrackingInfo> query = bgRealm.where(UserTrackingInfo.class);
                final UserTrackingInfo userTrackingInfo = query.findFirst();

                if (userTrackingInfo != null) {
                    userTrackingInfo.setTotalAchievedElevation(totalAchievedElevation);
                }
            }
        });
    }

    public void updateTotalTime(final Context context, final float totalTrackedTime) {
        Realm realm = getRealmInstance(context);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                // As currently we only has one tuple of user tracking info
                RealmQuery<UserTrackingInfo> query = bgRealm.where(UserTrackingInfo.class);
                final UserTrackingInfo userTrackingInfo = query.findFirst();

                if (userTrackingInfo != null) {
                    userTrackingInfo.setTotalTrackedTime(totalTrackedTime);
                }
            }
        });
    }

    public void updateAllTrackingInfo(final Context context, final UserTrackingInfo currentUserTrackingInfo) {
        Realm realm = getRealmInstance(context);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                // As currently we only has one tuple of user tracking info
                RealmQuery<UserTrackingInfo> query = bgRealm.where(UserTrackingInfo.class);
                final UserTrackingInfo savedUserTrackingInfo = query.findFirst();

                if (savedUserTrackingInfo != null) {
                    savedUserTrackingInfo.setTotalCoveredDistance(currentUserTrackingInfo.getTotalCoveredDistance());
                    savedUserTrackingInfo.setTotalTrackedTime(currentUserTrackingInfo.getTotalTrackedTime());
                    savedUserTrackingInfo.setTotalAchievedElevation(currentUserTrackingInfo.getTotalAchievedElevation());
                }
            }
        });
    }
}
