package ca.thegreattrail.ui.splash;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.downloader.Error;
import com.downloader.OnCancelListener;
import com.downloader.OnDownloadListener;
import com.downloader.OnPauseListener;
import com.downloader.OnProgressListener;
import com.downloader.OnStartOrResumeListener;
import com.downloader.PRDownloader;
import com.downloader.PRDownloaderConfig;
import com.downloader.Progress;
import com.google.android.gms.maps.model.LatLng;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.orhanobut.logger.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelperTrail;
import ca.thegreattrail.data.model.db.TrailSegmentLight;
import ca.thegreattrail.data.model.db.TrailSegmentLightLight;
import ca.thegreattrail.data.model.db.TrailWarning;
import ca.thegreattrail.ui.howtouse.HowToUseActivity;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.utlis.Constants;
import ca.thegreattrail.utlis.JsonParserExecutor;


public class SplashScreenActivity extends Activity {

    //  private String download_file_path ="https://api.tctrail.ca";
    private String download_file_path = "https://firebasestorage.googleapis.com/v0/b/facedemo-2f20e.appspot.com/o/trailDb.sqlite?alt=media&token=b74f1951-83cc-4d44-ae21-1a0f1ab832eb";
    private boolean updateAvailable = false;

    private ProgressBar progressBar;

    private String workPath = "/data/data/ca.thegreattrail/databases/"; // "/storage/sdcard0/";  // "/data/data/ca.TransCanadaTrail.TheGreatTrail/databases/"; File file = new File(context.getFilesDir(), filename);
    private String dbName = "trailDb.sqlite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);


        File folder = new File(workPath);
        if (!folder.exists()) {
            folder.mkdir();
        }
        initPRDownLoader();

        progressBar = findViewById(R.id.progressBar);

//      getTrailWarning();
//      checkDownloadAmenitiesDb();

        checkPermissionReadWriteExternal();


//        achievementsParserAsyncTask = new AchievementsParserAsyncTask(this, this);
//        ArrayList<Achievement> achievements = AchievementsDao.getInstance().findAll(this);
//        if (achievements.size() == 0) {
//            achievementsParserAsyncTask.execute();
//        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    private void getTrailWarning() {

        if (!isNetworkAvailable()) {
            // close this activity
            return;
        }

        String LangageStringtoAppend = "";
        String urlTrailWarning = Constants.TRAIL_WARNING_SERVICE_URL;
        String messageKey = "Message";
        String locationKey = "Location";

        if (Locale.getDefault().getLanguage().equals("fr")) {
            LangageStringtoAppend = "_FR";
            messageKey = "Message" + LangageStringtoAppend;
            locationKey = "Location" + LangageStringtoAppend;
            urlTrailWarning = urlTrailWarning.replace("Location", locationKey);
            urlTrailWarning = urlTrailWarning.replace("Message", messageKey);
        }


        RequestQueue queue = Volley.newRequestQueue(this);  // this = context
        // final String checksum="";


        final String finalMessageKey = messageKey;
        final String finalLocationKey = locationKey;
        StringRequest postRequest = new StringRequest(Request.Method.GET, urlTrailWarning,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                        MainActivity.trailWarnings = new ArrayList<TrailWarning>();

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String features = jsonResponse.getString("features");
                            Log.i("LocationService", features.toString());
                            JSONArray jsonArray = new JSONArray(features);
                            Log.i("LocationService", jsonArray.toString());

                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject warning = new JSONObject(jsonArray.get(i).toString());

                                JSONObject attributes = new JSONObject(warning.getString("attributes"));
                                String message = attributes.getString(finalMessageKey);
                                String location = attributes.getString(finalLocationKey);

                                JSONObject geometry = new JSONObject(warning.getString("geometry"));
                                String x = geometry.getString("x");
                                String y = geometry.getString("y");

                                LatLng coordinate = new LatLng(Double.parseDouble(y), Double.parseDouble(x));

                                MainActivity.trailWarnings.add(new TrailWarning(message, location, coordinate));

                                Log.i("LocationService", geometry.toString());


                            }
                            Log.i("LocationService", MainActivity.trailWarnings.size() + "");


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO handle the error
                        error.printStackTrace();
                    }
                }
        ) {

        };
        queue.add(postRequest);

    }


    private boolean checkUpdate() {

        String url = download_file_path;
        RequestQueue queue = Volley.newRequestQueue(this);  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            updateAvailable = jsonResponse.getBoolean("updateAvailable");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("apiKey", "0ee743d4c3907b30d9bc61670a7e62e11215ce1d");
                params.put("requestType", "resources.trailDb");
                params.put("iHave", mycheck(workPath + "trailDb.sqlite"));
                params.put("action", "updateCheck");
                params.put("appVersion", "1.0.0");
                params.put("apiVersion", "1.0");
                params.put("appPlatform", "Android");
                return params;
            }
        };
        queue.add(postRequest);
        return updateAvailable;


    }

    public interface VolleyCallback {
        void onSuccess(String result);

    }


    private void fileChecksumServer(final String file, final String resource, final String localChecksum, final VolleyCallback callback) {

// si le fichier n existe pas ne pas  continuer
        String url = download_file_path;
        RequestQueue queue = Volley.newRequestQueue(this);  // this = context
        // final String checksum="";

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response

                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String status = jsonResponse.getString("status");
                            String checksum = jsonResponse.getString("checksum");
                            String result = response;
                            callback.onSuccess(result);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO handle the error
                        error.printStackTrace();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();
                params.put("apiKey", "0ee743d4c3907b30d9bc61670a7e62e11215ce1d");
                params.put("requestType", resource);
                params.put("iHave", localChecksum);
                params.put("action", "checksum");
                params.put("appVersion", "1.0.0");
                params.put("apiVersion", "1.0");
                params.put("appPlatform", "Android");
                return params;
            }
        };
        queue.add(postRequest);
    }


    private void checkDownloadAmenitiesDb() {

        // Test if file exist or not
        File file = new File(workPath, "amenitiesDb.sqlite");
        if (!file.exists()) {

            if (!isNetworkAvailable()) {
                // close this activity
                return;
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

            final DownloadTask downloadTask = new DownloadTask(SplashScreenActivity.this);
            downloadTask.execute(download_file_path, "amenitiesDb.sqlite", "resources.amenitiesDb");

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
            SharedPreferences.Editor editor = preferences.edit();
            SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
            String realToday = sdf.format(new Date());
            editor.putString("today1", realToday);
            editor.commit();

        } else {

            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getApplication());
            SharedPreferences.Editor editor = preferences.edit();

            boolean update = false;
            if (preferences.contains("today1")) {
                String storedToday = preferences.getString("today1", "");
                SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
                String realToday = sdf.format(new Date());

                if (!realToday.equals(storedToday)) {
                    update = true;
                }
                editor.putString("today1", realToday);
                editor.commit();
            } else {
                editor.putString("today1", "");
                editor.commit();
            }


            if (isNetworkAvailable() && update) {

                final String amenitiesDbChecksumLocal = mycheck(workPath + "amenitiesDb.sqlite");
                // checksum="";
                fileChecksumServer("amenitiesDb.sqlite", "resources.amenitiesDb", amenitiesDbChecksumLocal, new VolleyCallback() {
                    @Override
                    public void onSuccess(String result) {
                        JSONObject jsonResponse;
                        try {
                            jsonResponse = new JSONObject(result);
                            String status = jsonResponse.getString("status");
                            String amenitiesDbChecksumServer = jsonResponse.getString("checksum");

                            if (!amenitiesDbChecksumServer.equals(amenitiesDbChecksumLocal)) {

                                // execute this when the downloader must be fired
                                final DownloadTask downloadTask = new DownloadTask(SplashScreenActivity.this);
                                downloadTask.execute(download_file_path, "amenitiesDb.sqlite", "resources.amenitiesDb");
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                });
            }
        }


    }

    private void checkDownloadTrailDb() {
        // Test if file exist or not

        File file = new File(workPath + "trailDb.sqlite");
        if (!file.exists() || getSizeFile(workPath + "trailDb.sqlite") < 16000) {

            if (!isNetworkAvailable()) {
                // close this activity
                finish();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }

//          final DownloadTask downloadTask = new DownloadTask(SplashScreenActivity.this);
//          downloadTask.execute(download_file_path, "trailDb.sqlite", "resources.trailDb");
            // after we do calculation in  downloadTask

            PRDownloader.download(download_file_path, workPath, dbName)
                    .build()
                    .setOnStartOrResumeListener(new OnStartOrResumeListener() {
                        @Override
                        public void onStartOrResume() {
                            progressBar.setVisibility(View.VISIBLE);

                        }
                    })
                    .setOnPauseListener(new OnPauseListener() {
                        @Override
                        public void onPause() {

                        }
                    })
                    .setOnCancelListener(new OnCancelListener() {
                        @Override
                        public void onCancel() {

                        }
                    })
                    .setOnProgressListener(new OnProgressListener() {
                        @Override
                        public void onProgress(Progress progress) {

                        }
                    })
                    .start(new OnDownloadListener() {
                        @Override
                        public void onDownloadComplete() {
                            progressBar.setVisibility(View.GONE);
                            final DoCalculations doCalculations = new DoCalculations(SplashScreenActivity.this);
                            doCalculations.execute();
                        }

                        @Override
                        public void onError(Error error) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(SplashScreenActivity.this, error.getServerErrorMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });

        } else {
            DoCalculations doCalculations = new DoCalculations(SplashScreenActivity.this);
            doCalculations.execute();
        }
    }

    private void initPRDownLoader() {
        PRDownloaderConfig config = PRDownloaderConfig.newBuilder()
                .setReadTimeout(Constants.TIME_OUT)
                .setConnectTimeout(Constants.TIME_OUT)
                .build();
        PRDownloader.initialize(getApplicationContext(), config);
    }

    private void retrieveTrailFromDB() {
        if (MainActivity.listPoints != null && !MainActivity.listPoints.isEmpty())
            return;

        MainActivity.listSegments = new ArrayList<TrailSegmentLight>();
        MainActivity.listPoints = new HashMap<Integer, ArrayList<LatLng>>();

        MainActivity.listSegmentsByTrailId = new ArrayList<TrailSegmentLightLight>();
        MainActivity.listPointsByTrailId = new HashMap<String, ArrayList<LatLng>>();

        long time1 = Calendar.getInstance().getTimeInMillis();
        JsonParserExecutor jsonParserExecutor = JsonParserExecutor.getInstance(MainActivity.listPoints);
//        retrieveSegments(jsonParserExecutor);
//        retrieveSegmentsV2(jsonParserExecutor);
//        retrieveSegmentsV3(jsonParserExecutor);
        retrieveSegmentsV4(jsonParserExecutor, 0, 0);
        jsonParserExecutor.shutdown();
        jsonParserExecutor.awaitsTermination();
        long time2 = Calendar.getInstance().getTimeInMillis();

        Logger.e("time to parse data = " + (time2 - time1) / 1000 + "s");
    }

    private void retrieveSegments(JsonParserExecutor jsonParserExecutor) {
        ActivityDBHelperTrail db = ActivityDBHelperTrail.getInstance(this);
        Cursor cursor = db.getAllSegmentsLight();

        if (cursor == null || !cursor.moveToFirst()) {
            db.close();
            Logger.e("retrieveSegments cursor = null");
            return;
        }
        String segmentPointsJsonString;
        TrailSegmentLight segment;
        int count = 0;
        do {
            count++;
            segmentPointsJsonString = db.findSegmentPointsJsonString(cursor);
            if (segmentPointsJsonString == null) continue;
            segment = TrailSegmentLight.mapFromDatabase(cursor);

            if (segment.trailId.equals("0084")) {
                count++;
            }

            MainActivity.listSegments.add(segment);
            jsonParserExecutor.executeTaskWithId(segment.objectId, segmentPointsJsonString);
        } while (cursor.moveToNext());

        Logger.e("retrieveSegments seg = " + MainActivity.listSegments.size() + "   count = " + count);

        cursor.close();
        db.close();
    }

    private void retrieveSegmentsV4(JsonParserExecutor jsonParserExecutor, int endObjectId, int startObjectId) {
        ActivityDBHelperTrail db = ActivityDBHelperTrail.getInstance(this);
        Cursor cursor = db.getStartSegmentsLight(startObjectId);

        if (cursor == null || !cursor.moveToFirst()) {
            db.close();
            Logger.e("retrieveSegments cursor = null");
            return;
        }

        if (endObjectId == 0) {
            endObjectId = cursor.getCount();
        }

        String segmentPointsJsonString;
        TrailSegmentLight segment;
        int objectId = startObjectId;
        int testCount = 0;

        do {
            objectId++;
            segmentPointsJsonString = db.findSegmentPointsJsonString(cursor);
            if (segmentPointsJsonString == null) {
                Logger.e("failedList objectId = " + objectId);
                break;
            }
            segment = TrailSegmentLight.mapFromDatabase(cursor);

            if (segment.trailId.equals("0084")) {
                testCount++;
            }

            MainActivity.listSegments.add(segment);
            jsonParserExecutor.executeTaskWithId(segment.objectId, segmentPointsJsonString);
        } while (cursor.moveToNext());

        Logger.e("retrieveSegments seg = " + MainActivity.listSegments.size() + "   testCount = " + testCount);

        if (objectId < endObjectId) {
            retrieveSegmentsV4(jsonParserExecutor, endObjectId, objectId);
        }
        cursor.close();
        db.close();
    }

    /**
     * The Great Trail is made of small trails, each has unique name and id.
     * Some have similar colors others don't.
     * This method merges all segments belonging to the same trail in 2 steps.
     * step 1: group all the segments belonging to the same trail, by making use of trailId.
     * step 2: merge all the segments belonging to one trail.
     */
    private void mergeTrailSegments() {
        HashMap<String, ArrayList<TrailSegmentLight>> trailIdToSegments = new HashMap<>();

        for (TrailSegmentLight segment : MainActivity.listSegments) {
            ArrayList<TrailSegmentLight> segments = trailIdToSegments.get(segment.trailId);
            if (segments == null) segments = new ArrayList<>();
            segments.add(segment);
            trailIdToSegments.put(segment.trailId, segments);
        }

        for (ArrayList<TrailSegmentLight> segments : trailIdToSegments.values())
            combineSegmentsList(segments);
    }

    /**
     * let the segments looks like :
     * P1.....P2 ,  P3.....P4 , P5.....P6
     * This method combines the segments who have common boundary points.
     * P1.....P2 + P2.....P3 = P1..........P3
     * Segments which have common non-boundary points aren't merged because if they merged the drawn
     * Polyline will be defected.
     * P1..P2..P3 + P2.....P4 = P1..P2..P3 + P2.....P4
     *
     * @param segments
     */
    private void combineSegmentsList(ArrayList<TrailSegmentLight> segments) {
        TrailSegmentLight segment_j;

        for (TrailSegmentLight segment_i : segments) {
            if (segment_i == null) continue;

            for (int j = 0; j < segments.size(); j++) {
                segment_j = segments.get(j);

                if (segment_j != null && segment_i != segment_j && canBeMerged(segment_i, segment_j)) {
                    MainActivity.listPoints.get(segment_i.objectId).addAll(MainActivity.listPoints.remove(segment_j.objectId));
                    MainActivity.listSegments.remove(segment_j);
                    segments.set(j, null);
                    j = -1;
                }
            }
        }
    }

    /**
     * return true if the 2 segments have common boundary points as explain by drawing, otherwise returns false.
     * P1.....P2, P2.....P3    returns true
     * if the case is like the following,
     * P1.....P2, P3.....P2
     * the method reverses the second segment and returns true.
     *
     * @param s1
     * @param s2
     */
    public boolean canBeMerged(TrailSegmentLight s1, TrailSegmentLight s2) {
        ArrayList<LatLng> points1 = MainActivity.listPoints.get(s1.objectId);
        ArrayList<LatLng> points2 = MainActivity.listPoints.get(s2.objectId);
        if (points1 == null || points2 == null) return false;
        if (points1.isEmpty() || points2.isEmpty()) return false;

        LatLng s1LastPoint = points1.get(points1.size() - 1);
        LatLng s2FirstPoint = points2.get(0);
        LatLng s2LastPoint = points2.get(points2.size() - 1);

        if (s1LastPoint.equals(s2FirstPoint)) {
            return true;
        } else if (s1LastPoint.equals(s2LastPoint)) {
            Collections.reverse(points2);
            return true;
        }

        return false;
    }

    // usually, subclasses of AsyncTask are declared inside the activity class.
    // that way, you can easily modify the UI thread from here
    private class DownloadTask extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;
        private String fileToDownload;

        public DownloadTask(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... param) {
            InputStream input = null;
            OutputStream output = null;
            HttpsURLConnection connection = null;
            Logger.e("DownloadTask 1");

            try {

                File file = new File(workPath + param[1]);
                fileToDownload = param[1];
                file.createNewFile();

                URL url = new URL(param[0]);
                connection = (HttpsURLConnection) url.openConnection();

                connection.setRequestMethod("POST");

                String urlParameters = "apiKey=0ee743d4c3907b30d9bc61670a7e62e11215ce1d&requestType=" + param[2] + "&iHave=b6cba16d4e884d53f565c27357d8dfaf5717ce324d1c754a3496d65838464104" + mycheck(workPath + param[1]) + "&action=get&appVersion=1.0.0&apiVersion=1.0&appPlatform=iOS";
//                String urlParameters = "apiKey=0ee743d4c3907b30d9bc61670a7e62e11215ce1d&requestType=" + param[2] + "&iHave=b6cba16d4e884d53f565c27357d8dfaf5717ce324d1c754a3496d65838464104" + mycheck(workPath + param[1]) + "&action=get&appVersion=1.7.3&apiVersion=1.0&appPlatform=iOS";

                //  trailDb      amenitiesDb
                // Send post request
                connection.setDoOutput(true);
                DataOutputStream wr = new DataOutputStream(connection.getOutputStream());
                wr.writeBytes(urlParameters);
                wr.flush();
                wr.close();


                // download the file
                input = connection.getInputStream();

                output = new FileOutputStream(file); // "/sdcard/amenities.sqlite"

                Logger.e("Bravooooooo5  HTTP_OK file path = " + workPath + "/amenities.sqlite");


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {  //<=
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    output.write(data, 0, count);
                }
            } catch (Exception e) {
                return e.toString();
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

                if (connection != null)
                    connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            // mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            /*mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);*/
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            //    mProgressDialog.dismiss();

            if (result != null) {
                //  Toast.makeText(context, "Download error: " + result, Toast.LENGTH_LONG).show();
                if (fileToDownload.equals("trailDb.sqlite")) {
                    Intent i = new Intent(SplashScreenActivity.this, MainActivity.class);
                    startActivity(i);
                    // close this activity
                    finish();
                }

            } else if (fileToDownload.equals("trailDb.sqlite")) {

                // execute this when the downloader must be fired
                final DoCalculations doCalculations = new DoCalculations(SplashScreenActivity.this);
                doCalculations.execute();
            }
        }
    }

    public String mycheck(String filePath) {
        // Test if file exist or not
        File file = new File(filePath);
        if (!file.exists())
            return "";

        // File currentJavaJarFile = new File(Checksum.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        //String filepath = currentJavaJarFile.getAbsolutePath();
        StringBuilder sb = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");// MD5
            FileInputStream fis = new FileInputStream(filePath);
            byte[] dataBytes = new byte[1024];
            int nread = 0;

            while ((nread = fis.read(dataBytes)) != -1)
                md.update(dataBytes, 0, nread);

            byte[] mdbytes = md.digest();

            for (int i = 0; i < mdbytes.length; i++)
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("Checksum: " + sb);
        return sb + "";
    }

    private long getSizeFile(String filePath) {
        long length = 0;
        try {
            File file = new File(filePath);
            length = file.length();
            length = length / 1024;  // Calculate file size (KB)
            Log.i("LocationService", " MainActivity : File Path : " + file.getPath() + ", File size : " + length + " KB ---------------------------------------------------------");


        } catch (Exception e) {
            Log.i("MainActivity", e.getMessage() + e);
        }

        return length;
    }

    private class DoCalculations extends AsyncTask<String, Integer, String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public DoCalculations(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... param) {
            retrieveTrailFromDB();
            mergeTrailSegments();

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            // mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);

        }

        @Override
        protected void onPostExecute(String result) {

            mWakeLock.release();
            // mProgressDialog.dismiss();

            Intent i = new Intent(SplashScreenActivity.this, HowToUseActivity.class);
            startActivity(i);
            // close this activity
            finish();
        }
    }

    private void checkPermissionReadWriteExternal(){
        Dexter.withActivity(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .withListener(new MultiplePermissionsListener() {
                    @Override
                    public void onPermissionsChecked(MultiplePermissionsReport report) {
                        // check if all permissions are granted
                        if (report.areAllPermissionsGranted()) {
                            checkDownloadTrailDb();
                        }

                        // check for permanent denial of any permission
                        if (report.isAnyPermissionPermanentlyDenied()) {
                            // permission is denied permenantly, navigate user to app settings
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                })
                .onSameThread()
                .check();
    }

}