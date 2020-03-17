package ca.thegreattrail.data.local.db;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Location;
import android.util.Log;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import ca.thegreattrail.data.model.db.AmenitySegment;


/**
 * Created by Hardik on 10/11/2016.
 */


public class AmenityDBHelperTrail extends SQLiteOpenHelper {

    public static final int DB_VERSION = 1;
    public static final String TB_USER = "db_version";
    public static String TAG = "AmenityDBHelperTrail";
    public static String DB_PATH = "/data/data/ca.thegreattrail/databases/";
    public static String DB_NAME = "amenitiesDb.sqlite";
    private static AmenityDBHelperTrail sInstance;
    private SQLiteDatabase myDB;
    private Context context;

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */

    public AmenityDBHelperTrail(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
        this.context = context;
        Log.e(TAG, "AmenityDBHelperTrail");
    }

    public static synchronized AmenityDBHelperTrail getInstance(Context context) {

        Log.e(TAG, "getInstance");

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        if (sInstance == null) {
            sInstance = new AmenityDBHelperTrail(context.getApplicationContext());
        }
        return sInstance;
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


    /***
     * Copy database from source code assets to device
     * @throws IOException
     */

    private void copyDataBase() throws IOException {
        try {
            InputStream myInput = context.getAssets().open(DB_NAME);
            String outputFileName = DB_PATH + DB_NAME;
            OutputStream myOutput = new FileOutputStream(outputFileName);

            byte[] buffer = new byte[1024];
            int length;

            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }

            myOutput.flush();
            myOutput.close();
            myInput.close();

            Log.e(TAG, "Copy successfull");

        } catch (Exception e) {
            Log.e(TAG, e.getMessage() + " error");
        }

    }


    /***
     * Open database
     * @throws SQLException
     */
    public void openDataBase() throws SQLException {
        String myPath = DB_PATH + DB_NAME;
        myDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
    }


    /***
     * Check if the database doesn't exist on device, create new one
     * @throws IOException
     */
    public void createDataBase() throws IOException {
        Log.i(TAG, "createDB");


        boolean dbExist = checkDataBase();

        if (dbExist) {

        } else {
            this.getReadableDatabase();
            try {
                copyDataBase();
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }
        }
    }


    /***
     * Check if the database is exist on device or not
     * @return
     */
    private boolean checkDataBase() {
        SQLiteDatabase tempDB = null;
        try {
            String myPath = DB_PATH + DB_NAME;
            tempDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        }
        if (tempDB != null)
            tempDB.close();

        return tempDB != null;
    }

    public AmenitySegment getNearestRestAmenity(String trailid, Location from) {
        AmenitySegment bestAmenity = null;

        try {
            // AmenitySegment nearestAmenity = null;
            List<AmenitySegment> nearest_amenities = new ArrayList<AmenitySegment>();
            SQLiteDatabase db = this.getReadableDatabase();


            Log.e(TAG, "Select * from  amenities  WHERE trailid =" + trailid + " and restroom=1");

            Cursor cursor = db.rawQuery("Select * from  amenities  WHERE trailid like '%" + trailid + "%'", null);  //  WHERE provinceid='05'
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    // String geometry = cursor.getString(cursor.getColumnIndex("geometry"));
                    AmenitySegment segment = new AmenitySegment(cursor);
                    nearest_amenities.add(segment);
                    //Log.i("AmenitiesDBHelperTrail","getAllAmenities i ="+(i++));
                }
                while (cursor.moveToNext());
            }
            double bestdistance = Float.MAX_VALUE;

            if (nearest_amenities.size() > 0) {
                try{
                    for (AmenitySegment segement : nearest_amenities) {
                        Location to = new Location("to");
                        to.setLatitude(Double.parseDouble(segement.getLat()));
                        to.setLongitude(Double.parseDouble(segement.getLng()));
                        //Log.d("distanceTo 1",from.distanceTo(to)+" mtr");
                        double distance=from.distanceTo(to);
                        // double distance = distanceFrom(from.getLatitude(), from.getLongitude(), Double.parseDouble(segement.getLat()), Double.parseDouble(segement.getLng()));

                        //Log.d("distanceTo 2",distance+" mtr");
                        if (distance < bestdistance) {
                            bestdistance = distance;
                            bestAmenity = segement;
                        }
                    }
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bestAmenity;
    }

//    public double distanceFrom(double fromlat, double fromlng, double tolat, double tolng) {
//        double earthRadius = 3958.75;
//        double dLat = Math.toRadians(tolat - fromlat);
//        double dLng = Math.toRadians(tolng - fromlng);
//        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) + Math.cos(Math.toRadians(fromlat)) * Math.cos(Math.toRadians(tolng)) * Math.sin(dLng / 2) * Math.sin(dLng / 2);
//        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
//        double dist = earthRadius * c;
//        int meterConversion = 1609;
//        double distance= new Double(dist * meterConversion);
//        return distance;
//    }


    public List<String> getAllUsers() {
        List<String> listUsers = new ArrayList<String>();
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor c;

        try {
            c = db.rawQuery("SELECT * FROM " + TB_USER, null);
            if (c == null)
                return null;

            String name;
            c.moveToFirst();
            do {
                name = c.getString(1);
                listUsers.add(name);
            } while (c.moveToNext());
            c.close();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        db.close();

        return listUsers;
    }


    public String getVersion() {
        String version = "0.0";
        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e(TAG, "AmenityDBHelper : Request = " + "Select version  from db_version limit 1");

        Cursor cursor = db.rawQuery("Select version  from db_version limit 1", null);

        if (cursor != null) {
            cursor.moveToFirst();
            version = cursor.getString(cursor.getColumnIndex("version"));
            Log.e(TAG, "AmenityDBHelper version" + version);
        }
        return version;
    }


    public Cursor getAmenities() {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();
        getDatabaseStructure(db);
//        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
//
//        if (c.moveToFirst()) {
//            while ( !c.isAfterLast() ) {
//                Toast.makeText(context, "Table Name=> "+c.getString(0), Toast.LENGTH_LONG).show();
//                Log.d("Amenities","Table Name=> "+c.getString(0));
//                c.moveToNext();
//            }
//        }
//
//
        Log.e(TAG, "AmenityDBHelper : Request = Select * from  amenities ORDER BY trailid_segment");

        Cursor c = db.rawQuery("Select * from  amenities", null);


        if (c != null) {
            c.moveToFirst();
            Log.e(TAG, "ActivityDBHelper : getAllAmenities  Cursor not null");
        }
        return c;
    }

    public void getDatabaseStructure(SQLiteDatabase db) {

        Cursor c = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
        ArrayList<String[]> result = new ArrayList<String[]>();
        int i = 0;
        result.add(c.getColumnNames());
        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            String[] temp = new String[c.getColumnCount()];
            for (i = 0; i < temp.length; i++) {
                temp[i] = c.getString(i);
                System.out.println("TABLE - " + temp[i]);

                Cursor c1 = db.rawQuery("SELECT * FROM " + temp[i], null);
                c1.moveToFirst();
                String[] COLUMNS = c1.getColumnNames();
                for (int j = 0; j < COLUMNS.length; j++) {
                    c1.move(j);
                    System.out.println(" COLUMN - " + COLUMNS[j]);
                }
            }
            result.add(temp);
        }
    }

    public Cursor getAmenitiesByObjectId(int objectid) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM amenities WHERE objectid = " + objectid, null);

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public String[] getAmenitiesBySearchText(String searchtext) {
        ArrayList<String> stringArrayList = new ArrayList<String>();
        stringArrayList.clear();
        if (!searchtext.equals("")) {
            try {
                SQLiteDatabase db = this.getReadableDatabase();
                //SELECT DISTINCT(type_amenity) FROM amenities WHERE  name_amenity LIKE '%fi%' GROUP BY trailid ORDER BY name_amenity ASC
                //SELECT type_amenity FROM amenities WHERE  name_amenity LIKE '%fisherman%' GROUP BY trailid ORDER BY name_amenity ASC
                String query = "SELECT DISTINCT(type_amenity) FROM amenities WHERE name_amenity LIKE (?) GROUP BY trailid ORDER BY name_amenity ASC ";
                Cursor cursor= db.rawQuery(query, new String[] { "%"+searchtext+"%"});

                if (cursor != null && cursor.moveToFirst()) {
                    do {
                        String stringAmenity = null;
                        int i = cursor.getInt(0);
                        switch (i) {
                            case 1:
                                if (Locale.getDefault().getLanguage().equals("en")) {
                                    stringAmenity = "Parking";
                                 }
                                else if (Locale.getDefault().getLanguage().equals("fr")) {
                                    stringAmenity = "Stationnement";
                                }

                                stringArrayList.add(stringAmenity);
                                break;
                            case 2:
                                if (Locale.getDefault().getLanguage().equals("en")) {
                                    stringAmenity = "Access Point";
                                }
                                else if (Locale.getDefault().getLanguage().equals("fr")) {
                                    stringAmenity = "Points d'accès";
                                }

                                stringArrayList.add(stringAmenity);
                                break;
                            case 3:
                                if (Locale.getDefault().getLanguage().equals("en")) {
                                    stringAmenity = "Rest Areas";
                                }
                                else if (Locale.getDefault().getLanguage().equals("fr")) {
                                    stringAmenity = "Aires de repos";
                                }
                                stringArrayList.add(stringAmenity);
                                break;
                        }

                        Log.e("added category", "" + stringAmenity);

                    } while (cursor.moveToNext());

                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        else {
            if (Locale.getDefault().getLanguage().equals("en")) {
                stringArrayList.add("Access Point");
            }
            else if (Locale.getDefault().getLanguage().equals("fr")) {
                stringArrayList.add("Points d'accès");
            }
            if (Locale.getDefault().getLanguage().equals("en")) {
                stringArrayList.add("Parking");
            }
            else if (Locale.getDefault().getLanguage().equals("fr")) {
                stringArrayList.add("Stationnement");
            }
            if (Locale.getDefault().getLanguage().equals("en")) {
                stringArrayList.add("Rest Areas");
            }
            else if (Locale.getDefault().getLanguage().equals("fr")) {
                stringArrayList.add("Aires de repos");
            }
        }

        String[] stringArray = stringArrayList.toArray(new String[stringArrayList.size()]);
        return stringArray;
    }

    public Cursor getAmenitiesByType(int type_amenity) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM amenities WHERE type_amenity = ? AND name_amenity IS NOT NULL GROUP BY trailid ORDER BY name_amenity ASC ", new String[]{"" + type_amenity});

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }

    public Cursor getAmenitiesByName(int type_amenity) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM amenities WHERE name_amenity LIKE ? GROUP BY trailid ORDER BY name_amenity ASC", new String[]{"" + type_amenity});

        if (cursor != null) {
            cursor.moveToFirst();
        }

        return cursor;
    }


    public Cursor getAmenityCoordinates(int trailid) {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Log.e(TAG, "AmenityDBHelper : Request = Select geometry from  amenities  WHERE trailid =");

        Cursor cursor = db.rawQuery("Select geometry from  amenities  WHERE trailid =" + trailid, null);  //  WHERE provinceid='05'
        if (cursor != null) {
            cursor.moveToFirst();
            Log.e(TAG, "AmenityDBHelper : getAmenityCoordinates  Cursor not null");
        }
        return cursor;
    }


    public Cursor getAmenityCoordinatesAndType() {

        // 1. get reference to readable DB
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery("Select type_amenity, name_amenity, geometry from  amenities ORDER BY geometry ASC , type_amenity ASC ", null);  //  WHERE provinceid='05'
        if (cursor != null) {
            cursor.moveToFirst();
        }
        return cursor;
    }
}



