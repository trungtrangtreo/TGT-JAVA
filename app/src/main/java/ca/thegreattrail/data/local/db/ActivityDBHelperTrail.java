package ca.thegreattrail.data.local.db;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;

import com.orhanobut.logger.Logger;

import ca.thegreattrail.data.model.db.TrailSegmentLight;
import io.requery.android.database.sqlite.SQLiteDatabase;
import io.requery.android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Houari on 10/11/2016.
 */


public class ActivityDBHelperTrail extends SQLiteOpenHelper {

    public static final String COLUMN_GEOMETRY = "geometry";
    public static final String COLUMN_OBJECT_ID = "objectid";
    public static final String COLUMN_SUM_LENGTH_KM = "sumlengthkm";
    public static final String COLUMN_PROVINCE_TXT = "provincetxt";

    private static ActivityDBHelperTrail sInstance;

    public static String DB_PATH = "/data/data/ca.TransCanadaTrail.TheGreatTrail/databases/";

    public static String DB_NAME = "trailDb.sqlite";
    public static final int DB_VERSION = 1;

    public static final String TB_USER = "db_version";

    private SQLiteDatabase myDB;
    private Context context;

    public static synchronized ActivityDBHelperTrail getInstance(Context context) {

        Log.e("LocationService", "Appel de getInstance   ------------------------------------------------------------------------------   getInstance ");

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new ActivityDBHelperTrail(context.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */

    public ActivityDBHelperTrail(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        Log.e("LocationService", "Creation de ActivityDBHelperTrail   ------------------------------------------------------------------------------   ActivityDBHelperTrail ");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub

    }

    @Override
    public synchronized void close() {
        if (myDB != null) {
            myDB.close();
        }
        super.close();
    }

//    public int getCount(){
//
//        String version = "0.0";
//        // 1. get reference to readable DB
//        SQLiteDatabase db = this.getReadableDatabase();
//
//        Cursor cursor = db.get("select count (*) from trail_data", null);
//
//        if (cursor != null) {
//            cursor.moveToFirst();
//            version = cursor.getString(cursor.getColumnIndex("version"));
//            Log.e("LocationService", "ActivityDBHelper ----------------------------------------------  version" + version);
//        }
//        return version;
//    }

    public String getVersion() {
        String version = "0.0";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelperTrail : Request = " + "Select version from db_version limit 1");

        Cursor cursor = db.rawQuery("Select version from db_version limit 1", null);

        if (cursor != null) {
            cursor.moveToFirst();
            version = cursor.getString(cursor.getColumnIndex("version"));
            Log.e("LocationService", "ActivityDBHelper ----------------------------------------------  version" + version);
        }
        return version;
    }

    public Cursor getAllSegmentsLight() {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT trailid, objectid, geometry, statuscode, categorycode FROM trail_data ORDER BY objectid";
        Logger.e("getAllSegmentsLight : Request = " + query);

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);  // WHERE provinceid='05' AND sectionid = '01'
        } catch (Exception e) {
            Logger.e("getAllSegmentsLight e = " + e.toString());
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public Cursor getAllSegmentsLight(int objectId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT trailid, objectid, geometry, statuscode, categorycode FROM trail_data WHERE objectid like " + objectId;

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);  // WHERE provinceid='05' AND sectionid = '01'
        } catch (Exception e) {
            Logger.e("getAllSegmentsLight e = " + e.toString());
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }


    public Cursor getStartSegmentsLight(int startObjectId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT trailid, objectid, geometry, statuscode, categorycode FROM trail_data WHERE objectid >= " + startObjectId;

        Cursor cursor = null;

        try {
            cursor = db.rawQuery(query, null);  // WHERE provinceid='05' AND sectionid = '01'
        } catch (Exception e) {
            Logger.e("getStartSegmentsLight e = " + e.toString());
        }

        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }

    public static String findProvinceBySegment(Context context, TrailSegmentLight segment) {
        if (segment == null)
            return null;

        ActivityDBHelperTrail database = getInstance(context);
        Cursor cursor = database.getSpecificSegments(segment.objectId);
        String province = cursor.getString(cursor.getColumnIndex(COLUMN_PROVINCE_TXT));
        cursor.close();
        database.close();

        return province;
    }

    public Cursor getSpecificSegments(int objectId) {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("Select * from trail_data where objectid = " + objectId, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }


    public Cursor getTrailsBySearch(String searchTxt) {

        //SELECT * FROM trail_data WHERE trailname LIKE '%a%'
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM trail_data WHERE trailname LIKE \"%" + searchTxt + "%\" GROUP BY trailid ORDER BY trailname ASC";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public Cursor getTrailByID(String trailID) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM trail_data WHERE trailid = '" + trailID + "' ORDER BY segmentid";
        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public String findSegmentPointsJsonString(Cursor cursor) {
        if (cursor == null)
            return "";

        String jsonString;
        int index = cursor.getColumnIndex(COLUMN_GEOMETRY);

        try {
            jsonString = cursor.getString(index);

        } catch (Exception e) {
//            Logger.e("findSegmentPointsJsonString objectId = " + cursor.getString(cursor.getColumnIndex("trailid")));
            // exeption due to oversize field of the cursor

//            byte[] blob = cursor.getBlob(index);
//            try {
//                jsonString = new String(blob, "UTF-8");
//            } catch (UnsupportedEncodingException e1) {
//                e1.printStackTrace();
                jsonString = null;
//            }
        }

        return jsonString;
    }
}
