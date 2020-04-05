package ca.thegreattrail.ui.tracker;

import android.content.Context;
import java.util.Calendar;
import java.util.Date;
import ca.thegreattrail.utlis.DateUtils;
import io.realm.Realm;
import io.realm.RealmQuery;

/**
 * Created by Ayman Mahgoub on 8/8/17.
 */

public class ProvincesDao extends GenericDao<Province> {

    private static ProvincesDao provincesDao;

    private ProvincesDao() {
        super();
    }

    public static ProvincesDao getInstance() {

        if (provincesDao == null)
            provincesDao = new ProvincesDao();
        return provincesDao;
    }

    public void insertOrUpdate(final Context context, final Province province) {
        Realm realm = getRealmInstance(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {

                if (province == null)
                    return;
                RealmQuery<Province> provinceRealmQuery = realm.where(Province.class).equalTo(Province.NAME_FIELD_NAME, province.getName());
                Province savedProvince = provinceRealmQuery.findFirst();

                // It is already existed, so don't save it
                if (savedProvince != null) {
                    // check updating last visit at or number of visits
                    updateProvince(context, province.getName(), province.getLastVisitAt(), province.getNumberOfVisits());
                } else {

                    try {
                        // Auto Increment Id
                        Number currentIdNum = realm.where(Province.class).max(Province.ID_FIELD_NAME);
                        int nextId;

                        if (currentIdNum == null) {
                            nextId = 1;
                        } else {
                            nextId = currentIdNum.intValue() + 1;
                        }
                        province.setId(nextId);
                        realm.insertOrUpdate(province);
                    } catch (IllegalArgumentException exception) {
                        exception.printStackTrace();
                    }
                }
            }
        });
    }

    public void updateProvince(final Context context, final String provinceName, final Date provinceLastVisitAt, final int numberOfVisits) {
        Realm realm = getRealmInstance(context);

        realm.executeTransactionAsync(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                // As currently we only has one tuple of user tracking info
                RealmQuery<Province> query = bgRealm.where(Province.class).equalTo(Province.NAME_FIELD_NAME, provinceName);
                Province province = query.findFirst();

                if (province != null) {
                    province.setLastVisitAt(provinceLastVisitAt);
                    province.setNumberOfVisits(numberOfVisits);
                }
            }
        });
    }


    public void updateNumberOfVisit(final Context context, final String provinceName) {
        Realm realm = getRealmInstance(context);

        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm bgRealm) {
                // As currently we only has one tuple of user tracking info
                RealmQuery<Province> query = bgRealm.where(Province.class).equalTo(Province.NAME_FIELD_NAME, provinceName);
                Province province = query.findFirst();

                if (province != null) {
                    Date lastVisitAt = province.getLastVisitAt();
                    Date currentTime = Calendar.getInstance().getTime();
                    long daysBetween = DateUtils.daysBetween(lastVisitAt, currentTime);
                    // We only consider a visit as new visit when it is in new date and within one week
                    if (daysBetween > 1 && daysBetween < 8)
                        province.setNumberOfVisits(province.getNumberOfVisits() + 1);
                }
            }
        });
    }
}
