package ca.thegreattrail.ui.tracker;

import android.content.Context;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public abstract class GenericDao<Type extends RealmObject> {

    private Class<Type> getRealmClass() {
        return (Class<Type>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    public void insertAll(final List<Type> list, Context context) {
        Realm realm = getRealmInstance(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                if (list != null) {
                    try {
                        for (Type type : list) {
                            realm.copyToRealmOrUpdate(type);
                        }
                    } catch (IllegalArgumentException exception) {
                        exception.printStackTrace();
                    } finally {

                    }
                }
            }
        });
    }

    public void insert(final Type type, Context context) {
        Realm realm = getRealmInstance(context);
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.copyToRealmOrUpdate(type);
            }
        });
    }

    public ArrayList<Type> findAll(Context context) {
        Realm realm = getRealmInstance(context);
        Class<Type> realmClass = getRealmClass();
        RealmQuery<Type> query = realm.where(realmClass);
        RealmResults<Type> result = query.findAll();
        ArrayList<Type> results = convertRealmResults(realm, result);
        realm.close();
        return results;
    }

    public ArrayList<Type> findWithId(Context context, String idKey, int id) {
        Realm realm = getRealmInstance(context);
        Class<Type> realmClass = getRealmClass();
        RealmQuery<Type> query = realm.where(realmClass).equalTo(idKey, id);
        RealmResults<Type> result = query.findAll();
        ArrayList<Type> results = convertRealmResults(realm, result);
        realm.close();
        return results;
    }

    public void update(Context context, final Type object) {
        Realm realm = getRealmInstance(context);
        realm.beginTransaction();
        realm.copyToRealmOrUpdate(object);
        realm.commitTransaction();
        realm.close();
    }

    protected ArrayList<Type> convertRealmResults(Realm realm, RealmResults<Type> result) {
        // Parse ObjectRealm to Object
        ArrayList<Type> allObjects = new ArrayList<>();

        for (Type type : result) {
            Type converted = realm.copyFromRealm(type);
            allObjects.add(converted);
        }
        return allObjects;
    }

    protected Realm getRealmInstance(Context context) {
        Realm.init(context);
        Realm realm = Realm.getDefaultInstance();
        return realm;
    }
}
