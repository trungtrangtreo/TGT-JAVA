package ca.thegreattrail.data.local.db;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.google.android.gms.maps.model.LatLng;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import ca.thegreattrail.data.model.other.OfflineItem;
import ca.thegreattrail.data.model.other.PointState;

/**
 * Created by Houari on 10/11/2016.
 */


public class ActivityDBHelper extends SQLiteOpenHelper {

    private static ActivityDBHelper sInstance;

    // Table Names
    private static final String TABLE_ACTIVITY = "activity";
    private static final String TABLE_LOCATION = "location";
    private static final String TABLE_ACTIVITY_REGION = "activity_region";

    private static final String KEY_ACTIVITY_ID = "_id";
    private static final String KEY_LOCATION_ID = "_id";
    private static final String KEY_ACTIVITY_REGION_ID = "_id";

    // If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "ATDB.db";
    private static final String SQL_CREATE_ACTIVITY = "CREATE TABLE activity(   _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                                "Latitude REAL, " +
                                                                                "Longitude REAL, " +
                                                                                "Region INTEGER, " +
                                                                                "Activity_name TEXT, " +
                                                                                "Start_time TEXT, " +
                                                                                "End_time TEXT," +
                                                                                "Time TEXT, " +
                                                                                "Distance REAL, " +
                                                                                "Elevation REAL)" ;

    private static final String SQL_CREATE_LOCATION = "CREATE TABLE location(   _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                                "Activity_id INTEGER, " +
                                                                                "Latitude REAL, " +
                                                                                "Longitude REAL, " +
                                                                                "Altitude REAL, " +
                                                                                "State TEXT)" ;

    private static final String SQL_CREATE_LOCATION_REGION = "CREATE TABLE activity_region( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                                            "Latitude REAL, " +
                                                                                            "Longitude REAL, " +
                                                                                            "Region INTEGER, " +
                                                                                            "Region_number INTEGER)" ;

    private static final String SQL_CREATE_OFFLINE_TRAILS = "CREATE TABLE offline_trails( _id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                                                                                        "name TEXT, " +
                                                                                        "date TEXT,"+
                                                                                        "status INTEGER)" ;


    private static final String SQL_DELETE_ACTIVITY = "DROP TABLE IF EXISTS " + "activity";
    private static final String SQL_DELETE_LOCATION = "DROP TABLE IF EXISTS " + "location";
    private static final String SQL_DELETE_ACTIVITY_REGION = "DROP TABLE IF EXISTS " + "activity_region";
    private static final String SQL_DELETE_OFFLINE_TRAILS  = "DROP TABLE IF EXISTS " + "offline_trails";


    public static synchronized ActivityDBHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new ActivityDBHelper(context.getApplicationContext());
        }
        return sInstance;
    }


    public ActivityDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }




    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ACTIVITY);
        db.execSQL(SQL_CREATE_LOCATION);
        db.execSQL(SQL_CREATE_LOCATION_REGION);
        db.execSQL(SQL_CREATE_OFFLINE_TRAILS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        db.execSQL(SQL_DELETE_ACTIVITY);
        db.execSQL(SQL_DELETE_LOCATION);
        db.execSQL(SQL_DELETE_ACTIVITY_REGION);
        db.execSQL(SQL_DELETE_OFFLINE_TRAILS);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }





    //  Activity Table CRUD


    public long getActivityId(String selectQuery ){

        long _Id = 0;
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        Log.e("LocationService", "Bravo Avant    ------  : Query =  " + selectQuery+" et = "+cursor.getColumnIndex("_id"));

        // 2. if we got results get the first one
        if( cursor != null && cursor.moveToFirst() ){
            // 3. build book object
            _Id =  cursor.getInt(cursor.getColumnIndex("_id"));
            Log.e("LocationService", "Bravo In --------  : activityId =  " + _Id+" et = "+cursor.getColumnIndex("_id"));
        }

        cursor.close();
//        db.close();
        return _Id;
    }


    public Activity getAllActivities(String selectQuery ){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelper : getAllActivities : Request = "+selectQuery);

        Cursor cursor = db.rawQuery(selectQuery, null);

        Activity activity = new Activity();
        // 2. if we got results get the first one
        if( cursor != null && cursor.moveToFirst() ){

             Log.e("LocationService", "Voila ce qui est recupe : "+cursor.isNull(0));
            // 3. build book object
            cursor.getInt(cursor.getColumnIndex("_id"));

           /* activity.setActivityId(cursor.getInt(cursor.getColumnIndex("activity_id")));
            activity.setLatitude(cursor.getDouble(cursor.getColumnIndex("latitude")));
            activity.setLongitude(cursor.getDouble(cursor.getColumnIndex("longitude")));
            activity.setRegion(cursor.getInt(cursor.getColumnIndex("region")));
            activity.setActivityName(cursor.getString(cursor.getColumnIndex("activity_name")));
            activity.setStartTime(cursor.getString(cursor.getColumnIndex("start_time")));
            activity.setStartTime(cursor.getString(cursor.getColumnIndex("end_time")));
            activity.setTime(cursor.getString(cursor.getColumnIndex("time")));
            activity.setDistance(cursor.getDouble(cursor.getColumnIndex("distance")));
            activity.setElevation(cursor.getDouble(cursor.getColumnIndex("elevation")));*/
        }




        cursor.close();
//        db.close();
        return activity;
    }

    public Cursor getAllActivities(){

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelper : Request = "+"Select * from activity;");

        Cursor cursor = db.rawQuery("Select * from activity;", null);

        return cursor;
    }


    public float getAltitudeFromLocation(String selectQuery ){

        float altitude = 0;
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelper (Altitude calculate): Request = "+selectQuery+"*******************************");

        Cursor cursor = db.rawQuery(selectQuery, null);


        // 2. if we got results get the first one

        if( cursor != null && cursor.moveToFirst() ){

            Log.e("LocationService", "ActivityDBHelper (Altitude calculate): Request = "+selectQuery+"  et ordre = "+cursor.getColumnIndex("Altitude"));
            // 3. build book object
            altitude =  cursor.getFloat(0);
        }

        cursor.close();
//        db.close();
        return altitude;
    }


    public void writeLocation(long _id, Double latitude , Double longitude, Double altitude, String state) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Activity_id",_id);
        values.put("Latitude", latitude);
        values.put("Longitude", longitude);
        values.put("Altitude", altitude);
        values.put("State", state);

        long newRowId = db.insert("location", null, values);

        Log.i("LocationService", "ActivityDBHELPER : Bien ecrit ------------------------------  Rowid= "+newRowId);
//        db.close();

    }

    public void writeEmptyActivity() {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("Latitude", 0.0);
        values.put("longitude", 0.0);
        values.put("region",0);
        values.put("activity_Name","No Name");
        values.put("start_time",getDateTime());
        values.put("end_time","00:00:00");
        values.put("time","00:00:00");
        values.put("distance","0.0");
        values.put("elevation","0.0");
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("activity", null, values);
//        db.close();

        Log.i("LocationService","Empty activity First-------------------------------------------------------------------------------------------------N = "+newRowId);
    }


    private String getDateTime() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        Date date = new Date();
        return dateFormat.format(date);
    }

    public long giveMeLastId() {

        SQLiteDatabase db = this.getWritableDatabase();
        long _id = this.getActivityId("SELECT _id FROM activity ORDER BY  _id DESC LIMIT 1;");
//        db.close();

        Log.i("LocationService","TrackService :  _id = "+_id);

        Log.i("LocationService","Empty activity second -------------------------------------------------------------------------------------------------");
        return _id;
    }


    public void endSaveActivity(long activityId, String activityName, String elapsedTime, String distance, double elevation){

        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        int region = calculateRegion();
      //  int elevation = calculateElevation(activityId);

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();


        values.put("Region",region);

        values.put("Activity_name",activityName);



        values.put("End_time",getDateTime());


        values.put("Time",elapsedTime);

        // long distance = CalculateDistanceOnLine();
        values.put("Distance",distance);


        values.put("Elevation",elevation);

        // updating row
        int tempo = db.update("activity", values, "_id" + " = ?",
                new String[] { String.valueOf(activityId) });

//        db.close();

        Log.i("LocationService"," ActivityDBHelper : Update with success : "+tempo);


    }



    private int calculateElevation(long activityId){
        SQLiteDatabase db = this.getWritableDatabase();

        String querySQL = "SELECT altitude FROM location WHERE Activity_id = "+activityId+" ORDER BY _id ASC LIMIT 1;" ;
        double altitude1 = getAltitudeFromLocation(querySQL );
        Log.i("LocationService","ActivityTrackFragment :  altitude1 = "+altitude1);

        querySQL = "SELECT altitude FROM location WHERE Activity_id = "+activityId+" ORDER BY _id DESC LIMIT 1;" ;
        double altitude2 = getAltitudeFromLocation(querySQL );
        Log.i("LocationService","ActivityTrackFragment :  altitude2 = "+altitude2);

//        db.close();

        double altitude = altitude1 - altitude2 ;
        Log.i("LocationService","ActivityTrackFragment :  altitude = "+altitude);


        return (int) Math.round(altitude);
    }

    private int calculateRegion(){

        return 1;
    }


    public String giveMeStartTime(long _id){

        String startTime="";
        String selectQuery = "SELECT Start_time FROM activity WHERE _id="+_id;
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        Log.e("LocationService", "Bravo Avant    ------  : Query =  " + selectQuery+" et = "+cursor.getColumnIndex("Start_time"));

        // 2. if we got results get the first one
        if( cursor != null && cursor.moveToFirst() ){
            // 3. build book object
            startTime =  cursor.getString(cursor.getColumnIndex("Start_time"));
            Log.e("LocationService", "Bravo In --------  : startTime =  " + startTime+" et = "+cursor.getColumnIndex("_id"));
        }

        cursor.close();


        return startTime;
    }

    public List<PointState> giveMeAllPoints(long _id){
        List<PointState> points = new ArrayList<PointState>();

        String startTime="";
        String selectQuery = "SELECT Latitude,Longitude,State FROM location WHERE activity_id="+_id;
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        Log.e("LocationService", "Bravo Avant    ------  : Query =  " + selectQuery+" et = "+cursor.getColumnIndex("Latitude"));


         if( cursor != null && cursor.moveToFirst() ){

            do {

                Log.e("LocationService", "TRouveeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee  " + selectQuery+" et = "+cursor.getColumnIndex("Longitude"));

                float latitude  = cursor.getFloat(cursor.getColumnIndex("Latitude"));
                float longitude  = cursor.getFloat(cursor.getColumnIndex("Longitude"));
                String state = cursor.getString(cursor.getColumnIndex("State"));
                PointState pointState = new PointState(new LatLng(latitude,longitude), state);

                points.add(pointState);

            }
            while(cursor.moveToNext());
        }

        cursor.close();



        return points;
    }

    public List<String> giveMeActivity(long _id){

        List<String> dataToSend = new ArrayList<String>();
        String selectQuery = "SELECT Activity_name, Distance, Time, Elevation FROM activity WHERE _id="+_id;
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(selectQuery, null);

        Log.e("LocationService", "Bravo Avant    ------  : Query =  " + selectQuery+" et = "+cursor.getColumnIndex("Start_time"));

        // 2. if we got results get the first one
        if( cursor != null && cursor.moveToFirst() ){
            // 3. build book object
            float distance =  cursor.getFloat(cursor.getColumnIndex("Distance"));
            String time =  cursor.getString(cursor.getColumnIndex("Time"));
            float elevation =  cursor.getFloat(cursor.getColumnIndex("Elevation"));
            String activityName = cursor.getString(cursor.getColumnIndex("Activity_name"));

            dataToSend.add(distance+"");
            dataToSend.add(time);
            dataToSend.add(elevation+"");
            dataToSend.add(activityName);

            Log.e("LocationService", "Bravo In --------  : startTime =  " + dataToSend.toString()+" et = "+cursor.getColumnIndex("_id"));
        }

        cursor.close();


        return dataToSend;
    }

    public LatLng CalculateMedianePoint(long activityId) {

        SQLiteDatabase db = this.getWritableDatabase();

        String querySQL = "SELECT Latitude, Longitude FROM location WHERE Activity_id = "+activityId+" ORDER BY _id ASC LIMIT 1;" ;
        LatLng point1 = getPointFromLocation(querySQL );

        Log.i("LocationService","ActivityTrackFragment :  point1 = "+point1.toString());

        querySQL = "SELECT Latitude, Longitude FROM location WHERE Activity_id = "+activityId+" ORDER BY _id DESC LIMIT 1;" ;
        LatLng point2 = getPointFromLocation(querySQL );
        Log.i("LocationService","ActivityTrackFragment :  altitude2 = "+point2.toString());

//        db.close();

        LatLng medianePoint = new LatLng((point1.latitude+point2.latitude)/2 , (point1.longitude+point2.longitude)/2);
        Log.i("LocationService","ActivityTrackFragment :  altitude = "+medianePoint.toString());

        return medianePoint;
    }


    public LatLng getPointFromLocation(String selectQuery ){

        LatLng point = new LatLng(0,0);
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e("LocationService", "ActivityDBHelper (Altitude calculate): Request = "+selectQuery+"*******************************");

        Cursor cursor = db.rawQuery(selectQuery, null);


        // 2. if we got results get the first one

        if( cursor != null && cursor.moveToFirst() ){

            Log.e("LocationService", "ActivityDBHelper (Altitude calculate): Request = "+selectQuery+"  et ordre = "+cursor.getColumnIndex("Altitude"));
            // 3. build book object
            point = new LatLng(cursor.getFloat(0), cursor.getFloat(1));
        }

        cursor.close();
         return point;
    }

    public List<OfflineItem> getOfflineTrails(){
        List<OfflineItem> offlineItems = new ArrayList<OfflineItem>();
        String selectQuery = "SELECT * FROM offline_trails";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if( cursor != null && cursor.moveToFirst() ){
            do {
                String name  = cursor.getString(cursor.getColumnIndex("name"));
                String date  = cursor.getString(cursor.getColumnIndex("date"));
                boolean status =   cursor.getInt(cursor.getColumnIndex("date")) > 1;
                OfflineItem offlineItem = new OfflineItem(name,date, status);
                offlineItems.add(offlineItem);
            }
            while(cursor.moveToNext());        }

        cursor.close();
        return offlineItems;
    }

    public HashMap<String, String> getOfflineTrailsAsHashMap(){
        HashMap<String, String> offlineItems = new HashMap<String, String>();
        String selectQuery = "SELECT * FROM offline_trails";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if( cursor != null && cursor.moveToFirst() ){
            do {
                String name  = cursor.getString(cursor.getColumnIndex("name"));
                String date  = cursor.getString(cursor.getColumnIndex("date"));
                offlineItems.put(name,date);
            }
            while(cursor.moveToNext());        }

        cursor.close();
        return offlineItems;
    }



    public void addOfflineMapInDB(String name, String date, int status) {
        // Gets the data repository in write mode
        SQLiteDatabase db = this.getWritableDatabase();

        // Create a new map of values, where column names are the keys
        ContentValues values = new ContentValues();
        values.put("name", name);
        values.put("date",date);
        values.put("status",status);
        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert("offline_trails", null, values);
//        db.close();

     }


    public void deleteOfflineMapInDB(String name)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("offline_trails", " name = ?",
                new String[]{name} );
        // db.close();
    }


    public void deleteActivity(String id)
    {
        deleteFromActivity(id);
        deleteFromLocation(id);
    }


    public void deleteFromActivity(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("activity", "_id = ?",
                new String[]{id} );
        // db.close();
    }

    public void deleteFromLocation(String id)
    {
        SQLiteDatabase db = this.getWritableDatabase();

        db.delete("location", "Activity_id = ?",
                new String[]{id} );
        // db.close();
    }



}