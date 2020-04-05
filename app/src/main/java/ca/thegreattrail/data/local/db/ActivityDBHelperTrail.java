package ca.thegreattrail.data.local.db;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

import com.orhanobut.logger.Logger;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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

    public static String DB_PATH = "/data/data/ca.thegreattrail/databases/";

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

        File file = new File(DB_PATH + "trailDb.sqlite");
        if (file.exists()) {
            opendatabase();
        } else {
            this.getReadableDatabase();
            try {
                copydatabase();
            } catch(IOException e) {
                throw new Error("Error copying database");
            }
        }

        Log.e("LocationService", "Creation de ActivityDBHelperTrail   ------------------------------------------------------------------------------   ActivityDBHelperTrail ");
    }

    private void copydatabase() throws IOException {
        //Open your local db as the input stream
        InputStream myinput = context.getAssets().open(DB_NAME);

        // Path to the just created empty db
        String outfilename = DB_PATH + DB_NAME;

        //Open the empty db as the output stream
        OutputStream myoutput = new FileOutputStream(outfilename);

        // transfer byte to inputfile to outputfile
        byte[] buffer = new byte[1024];
        int length;
        while ((length = myinput.read(buffer))>0) {
            myoutput.write(buffer,0,length);
        }

        //Close the streams
        myoutput.flush();
        myoutput.close();
        myinput.close();
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

    public void opendatabase() throws SQLException {
        //Open the database
        String mypath = DB_PATH + DB_NAME;
        myDB = SQLiteDatabase.openDatabase(mypath, null, SQLiteDatabase.OPEN_READWRITE);
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

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;
        try {
            in = assetManager.open(fromAssetPath);
            new File(toPath).createNewFile();
            out = new FileOutputStream(toPath);
            copyFile(in, out);
            in.close();
            in = null;
            out.flush();
            out.close();
            out = null;
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

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
