package ca.thegreattrail.ui.traildetail;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.ThumbnailUtils;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.analytics.Tracker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Stack;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.thegreattrail.MvvmApp;
import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelperTrail;
import ca.thegreattrail.data.model.db.TrailSegment;
import ca.thegreattrail.ui.base.BaseActivity;
import ca.thegreattrail.ui.map.MapFragment;
import ca.thegreattrail.ui.traildetail.fullimage.FullImageActivity;
import ca.thegreattrail.utlis.Constants;
import ca.thegreattrail.utlis.ImagePicker;
import ca.thegreattrail.utlis.Utility;

public class DetailTrailActivity extends BaseActivity {


    private String TAG = "LocationService";
    private String trailId = "";
    private int objectId;
    private int x;

    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 100;
    public static SegmentDetailsFragment instance = null;
    public static List<Bitmap> images = new ArrayList<Bitmap>();
    public static String currentTab = "MapFragment";

    public static HashMap<String, Fragment> mapfragStack = new HashMap<>();
    public static Stack<String> mapfragTagStack = new Stack<>();

    private int SELECT_IMAGE_RESULT = 100;

    String trailName = "";
    Tracker mTracker;

    @BindView(R.id.firstImage)
    ImageView firstImage;
    @BindView(R.id.imageGallery)
    LinearLayout imageGallery;
    @BindView(R.id.carouselHSV)
    HorizontalScrollView carouselHSV;
    @BindView(R.id.directionBtn)
    Button directionBtn;
    @BindView(R.id.descriptionTxt)
    TextView descriptionTxt;
    @BindView(R.id.trailTypeTxt)
    TextView trailTypeTxt;
    @BindView(R.id.activitiesTxt)
    TextView activitiesTxt;
    @BindView(R.id.environmentTxt)
    TextView environmentTxt;
    @BindView(R.id.resourcesTxt1)
    TextView resourcesTxt1;
    @BindView(R.id.resourcesTxt2)
    TextView resourcesTxt2;
    @BindView(R.id.resourcesTxt3)
    TextView resourcesTxt3;
    @BindView(R.id.resourcesTxt4)
    TextView resourcesTxt4;
    @BindView(R.id.resourcesTitle)
    TextView resourcesTitle;
    @BindView(R.id.segmentNameTxt)
    TextView segmentNameTxt;
    @BindView(R.id.resourcesSeparator)
    View resourcesSeparator;

    public static Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        canvas.drawBitmap(bitmap, m, new Paint());

        return output;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_trail);

        initData();
    }

    @Override
    protected void onResume() {
        super.onResume();
        images.clear();
        imageGallery.removeAllViews();
        loadDetailTrail();
    }

    private void initData() {

        ButterKnife.bind(this);

//      activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//      activity.getSupportActionBar().setDisplayShowHomeEnabled(true);
        trailName = getIntent().getStringExtra(MapFragment.TRAIL_ID);
        objectId = getIntent().getIntExtra(MapFragment.OBJECT_ID, 0);
//      activity.getSupportActionBar().setTitle(Html.fromHtml("<small>" + trailName + "</small>"));

        imageGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int num = x / (dpToPx(150) + 30);
                Log.i(TAG, " Le click  num = " + num);

//                if (num < images.size()) {
//                    Intent myIntent = new Intent(DetailTrailActivity.this, FullImageActivity.class);
//                    myIntent.putExtra("num", num);
//                    startActivity(myIntent);
//                }
            }
        });

        imageGallery.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                x = (int) event.getX();
                return false;
            }
        });

        Button uploadBtn = findViewById(R.id.uploadBtn);
        carouselHSV.setVisibility(View.GONE);
        uploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                String[] permissos = {"android.permission.CAMERA"};

                if (ContextCompat.checkSelfPermission(DetailTrailActivity.this,
                        Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {

                    ActivityCompat.requestPermissions(DetailTrailActivity.this,
                            permissos,
                            MY_PERMISSIONS_REQUEST_CAMERA
                    );

                } else {
                    Intent chooseImageIntent = ImagePicker.getPickImageIntent(DetailTrailActivity.this);
                    startActivityForResult(chooseImageIntent, SELECT_IMAGE_RESULT);

                    if (ContextCompat.checkSelfPermission(DetailTrailActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(DetailTrailActivity.this, new String[]{Manifest.permission.CAMERA}, 0);
                    }
                }
            }
        });
        directionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!isNetworkAvailable()) {
                    new AlertDialog.Builder(DetailTrailActivity.this)
                            .setTitle(R.string.alert_no_internet)
                            .setMessage(R.string.alert_must_online_trail)
                            .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            })
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show();
                    return;
                }

                pushSegmentDetailFragment();
                DirectionTrailFragment directionTrailFragment = DirectionTrailFragment.newInstance(trailId);
                replaceFragment(directionTrailFragment);
            }
        });
    }


    private void loadDetailTrail() {
        ActivityDBHelperTrail db = ActivityDBHelperTrail.getInstance(DetailTrailActivity.this);
        Cursor cursor = db.getSpecificSegments(objectId);

        if (cursor != null && cursor.moveToFirst()) {
            TrailSegment segment = new TrailSegment(cursor);
            trailId = segment.getTrailId();
            searchAndDownloadFlickrFeaturedPhoto("TrailCode_" + segment.getTrailId() + "_featured", false, "TrailCode_" + segment.getProvinceId() + "_featured", true);

            searchAndDownloadTrailFlickrPhotos("TrailCode_" + segment.getTrailId(), false);
            segmentNameTxt.setText(segment.getTrailName() + "\n" + segment.getSumLengthKm() + " km");

            if (Locale.getDefault().getLanguage().equals("fr")) {
                if (!segment.getDescription().equals("")) {
                    descriptionTxt.setVisibility(View.VISIBLE);
                    descriptionTxt.setText(segment.getDescription_fr());
                }
                segmentNameTxt.setText(segment.getTrailName() + "\n" + segment.getSumLengthKm() + " km");
                trailTypeTxt.setText(segment.getTrailType_fr());
                activitiesTxt.setText(segment.getActivities_fr());
                environmentTxt.setText(segment.getEnvironment_fr());
            } else {
                if (!segment.getDescription().equals("")) {
                    descriptionTxt.setVisibility(View.VISIBLE);
                    descriptionTxt.setText(segment.getDescription());
                }
                segmentNameTxt.setText(segment.getTrailName() + "\n" + segment.getSumLengthKm() + " km");
                trailTypeTxt.setText(segment.getTrailType());
                activitiesTxt.setText(segment.getActivities());
                environmentTxt.setText(segment.getEnvironment());
            }

            if (!segment.getGroupName1().equals("") && !segment.getWebsiteUrl1().equals("")) {
                Spanned link1, link2, link3, link4;
                resourcesTitle.setVisibility(View.VISIBLE);
                resourcesSeparator.setVisibility(View.VISIBLE);

                if (Build.VERSION.SDK_INT >= 24) {
                    link1 = Html.fromHtml("<a href=\"" + segment.getWebsiteUrl1() + "\">" + segment.getGroupName1() + "</a>", Html.FROM_HTML_MODE_LEGACY); // for 24 api and more

                } else {
                    link1 = (Html.fromHtml("<a href=\"" + segment.getWebsiteUrl1() + "\">" + segment.getGroupName1() + "</a>")); // or for older api
                }

                resourcesTxt1.setMovementMethod(LinkMovementMethod.getInstance());
                resourcesTxt1.setText(link1);

                if (!segment.getGroupName2().equals("") && !segment.getWebsiteUrl2().equals("")) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        link2 = Html.fromHtml("<a href=\"" + segment.getWebsiteUrl2() + "\">" + segment.getGroupName2() + "</a>", Html.FROM_HTML_MODE_LEGACY); // for 24 api and more

                    } else {
                        link2 = (Html.fromHtml("<a href=\"" + segment.getWebsiteUrl2() + "\">" + segment.getGroupName2() + "</a>")); // or for older api
                    }

                    resourcesTxt2.setMovementMethod(LinkMovementMethod.getInstance());
                    resourcesTxt2.setText(link2);

                }

                if (!segment.getGroupName3().equals("") && !segment.getWebsiteUrl3().equals("")) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        link3 = Html.fromHtml("<a href=\"" + segment.getWebsiteUrl3() + "\">" + segment.getGroupName3() + "</a>", Html.FROM_HTML_MODE_LEGACY); // for 24 api and more

                    } else {
                        link3 = (Html.fromHtml("<a href=\"" + segment.getWebsiteUrl3() + "\">" + segment.getGroupName3() + "</a>")); // or for older api
                    }

                    resourcesTxt3.setMovementMethod(LinkMovementMethod.getInstance());
                    resourcesTxt3.setText(link3);
                }

                if (!segment.getGroupName4().equals("") && !segment.getWebsiteUrl4().equals("")) {
                    if (Build.VERSION.SDK_INT >= 24) {
                        link4 = Html.fromHtml("<a href=\"" + segment.getWebsiteUrl4() + "\">" + segment.getGroupName4() + "</a>", Html.FROM_HTML_MODE_LEGACY); // for 24 api and more

                    } else {
                        link4 = (Html.fromHtml("<a href=\"" + segment.getWebsiteUrl4() + "\">" + segment.getGroupName4() + "</a>")); // or for older api
                    }

                    resourcesTxt4.setMovementMethod(LinkMovementMethod.getInstance());
                    resourcesTxt4.setText(link4);
                }

            } else {
                resourcesTitle.setVisibility(View.GONE);
                resourcesSeparator.setVisibility(View.GONE);
            }
        }
        Objects.requireNonNull(cursor).close();
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SELECT_IMAGE_RESULT) {

            Bitmap mphoto = ImagePicker.getImageFromResult(DetailTrailActivity.this, resultCode, data);

            if (mphoto != null) {
//              pushSegmentDetailFragment();
//              UploadFlickrFragment uploadFlickrFragment = new UploadFlickrFragment();
//              uploadFlickrFragment.photo = mphoto;
//              uploadFlickrFragment.trailId = trailId;
//              replaceFragment(uploadFlickrFragment);

                Intent intent = new Intent(this, UploadFlickrActivity.class);
                UploadFlickrActivity.photo = mphoto;
                UploadFlickrActivity.trailId = trailId;
                startActivity(intent);
            }
        }
    }

    private void replaceFragment(Fragment replaceFragment) {
        FragmentTransaction mFragmentTransaction = getSupportFragmentManager().beginTransaction();
        switch (currentTab) {
            case "MapFragment":
                mFragmentTransaction
                        .replace(R.id.searchLayout, replaceFragment)
                        .commit();
                break;
//            case "MeasureFragment":
//                mFragmentTransaction
//                        .replace(R.id.measureSearchLayout, replaceFragment)
//                        .commit();
//
//                break;
//            case "ActivityTrackerFragment":
//                mFragmentTransaction
//                        .replace(R.id.trackerSearchLayout, replaceFragment)
//                        .commit();
//
//                break;
        }
    }

    private void searchAndDownloadTrailFlickrPhotos(String tag, final boolean isThumbnail) {

        String url = Constants.URL_SEARCH_FLIKR + tag;
        RequestQueue queue = Volley.newRequestQueue(DetailTrailActivity.this);  // this = context

        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.i("LocationService", "Response   ----------------------------------------------------------------------------------------------   " + response);
                        images = new ArrayList<Bitmap>();
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONObject photos = jsonResponse.getJSONObject("photos");
                            int totalphotos = photos.getInt("total");
                            if (totalphotos > 0) {
                                JSONArray photoArray = new JSONArray(photos.getString("photo"));

                                for (int i = 0; i < totalphotos; i++) {
                                    JSONObject pic = new JSONObject(photoArray.get(i).toString());
                                    String server = pic.getString("server");
                                    String id = pic.getString("id");
                                    String secret = pic.getString("secret");
                                    int farm = pic.getInt("farm");
                                    Log.i("LocationService", "Photo   ----------------------------------------------------------------------------------------------   " + id + "  " + server + "   " + secret + "   " + farm);

                                    ImageLoader imageLoader = MvvmApp.getInstance().getImageLoader();


                                    String url = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret;

                                    if (isThumbnail) {
                                        url += "_q.jpg";  //  150 x 150
                                    } else {
                                        url += "_b.jpg"; // 1024
                                    }
                                    imageLoader.get(url, new ImageLoader.ImageListener() {

                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            Log.e(TAG, "Image Load Error: " + error.getMessage());
                                        }

                                        @Override
                                        public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {

                                            if (response == null) {
                                                return;
                                            }
                                            Bitmap bitmap = response.getBitmap();
                                            if (bitmap != null) {
                                                if (carouselHSV.getVisibility() == View.GONE) {
                                                    carouselHSV.setVisibility(View.VISIBLE);
                                                }

                                                imageGallery.addView(getImageView(bitmap)); // add image to view
                                                images.add(bitmap); //  Add image to a list
                                            }
                                        }
                                    });

                                }

                            } else {


                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        // TODO handle the error
                        error.printStackTrace();
                    }
                }
        );
        queue.add(postRequest);
    }

    private boolean searchAndDownloadFlickrFeaturedPhoto(String tag, final boolean isThumbnail, final String tag2, final boolean isSegment) {
        final boolean[] result = {false};
        String url = Constants.URL_SEARCH_FLIKR + tag;
        RequestQueue queue = Volley.newRequestQueue(DetailTrailActivity.this);  // this = context


        final ProgressDialog pDialog = new ProgressDialog(DetailTrailActivity.this);
        pDialog.setMessage("Loading...");
        pDialog.show();


        StringRequest postRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.i("LocationService", "Response   ----------------------------------------------------------------------------------------------   " + response);
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            JSONObject photos = jsonResponse.getJSONObject("photos");
                            int totalphotos = photos.getInt("total");
                            if (totalphotos > 0) {
                                JSONArray photoArray = new JSONArray(photos.getString("photo"));

                                JSONObject pic = new JSONObject(photoArray.get(0).toString());
                                String server = pic.getString("server");
                                String id = pic.getString("id");
                                String secret = pic.getString("secret");
                                int farm = pic.getInt("farm");
                                Log.i("LocationService", "Photo----------------------------------------------------------------------------------------------   " + id + "  " + server + "   " + secret + "   " + farm);

                                ImageLoader imageLoader = MvvmApp.getInstance().getImageLoader();


                                String url = "https://farm" + farm + ".staticflickr.com/" + server + "/" + id + "_" + secret;

                                if (isThumbnail) {
                                    url += "_q.jpg";
                                } else {
                                    url += "_b.jpg";
                                }

                                Log.i("LocationService", "url   ----------------------------------------------------------------------------------------------   " + url);


                                //  String url = "https://farm"+farm+".staticflickr.com/"+server+"/"+id+"_"+secret+"_b"+".jpg" ;
                                // If you are using normal ImageView
                                imageLoader.get(url, new ImageLoader.ImageListener() {

                                    @Override
                                    public void onErrorResponse(VolleyError error) {
                                        Log.e(TAG, "Image Load Error: " + error.getMessage());
                                    }

                                    @Override
                                    public void onResponse(ImageLoader.ImageContainer response, boolean arg1) {

                                        if (response == null) {
                                            pDialog.dismiss();
                                            if (isSegment) {
                                                searchAndDownloadFlickrFeaturedPhoto(tag2, false, "", false);
                                            }
                                            return;
                                        }
                                        Bitmap bitmap = response.getBitmap();
                                        if (bitmap != null) {
                                            Bitmap croppedBmp;

                                            if (firstImage.getWidth() > 0 && firstImage.getHeight() > 0) {
                                                croppedBmp = ThumbnailUtils.extractThumbnail(bitmap, firstImage.getWidth(), firstImage.getHeight());
                                            } else {
                                                croppedBmp = ThumbnailUtils.extractThumbnail(bitmap, carouselHSV.getWidth(), dpToPx(200));
                                            }

                                            firstImage.setImageBitmap(croppedBmp);
                                            pDialog.dismiss();
                                            result[0] = true;
                                            Log.i("LocationService", "True   ----------------------------------------------------------------------------------------------   " + result[0]);

                                        } else {
                                            pDialog.dismiss();

                                        }
                                    }
                                });

                            } else {
                                pDialog.dismiss();
                                result[0] = false;
                                if (isSegment) {
                                    searchAndDownloadFlickrFeaturedPhoto(tag2, false, "", false);
                                }
                            }


                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO handle the error
                        Utility.displayToast(DetailTrailActivity.this, "Photos not available on Server, Try in while", Toast.LENGTH_LONG);
                        pDialog.dismiss();
                    }
                }
        );
        queue.add(postRequest);
        Log.i("LocationService", "Displayed result  ----------------------------------------------------------------------------------------------   " + result[0]);

        return result[0];

    }

    private View getImageView(Bitmap image) {
        image = scaleBitmap(image, dpToPx(150), dpToPx(150));
        ImageView imageView = new ImageView(DetailTrailActivity.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dpToPx(150), dpToPx(150));
        lp.setMargins(15, 10, 15, 10); // left,top,right,bottom
        imageView.setLayoutParams(lp);
        imageView.setImageBitmap(image);
        return imageView;
    }

    private void pushSegmentDetailFragment() {
        String actlogtag = "SegmentDetailsFragment";
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fsegment = null;

        if ("MapFragment".equals(currentTab)) {
            actlogtag = "Map" + actlogtag;
            fsegment = fragmentManager.findFragmentById(R.id.searchLayout);

            if (fsegment instanceof SegmentDetailsFragment) {
                mapfragStack.put(actlogtag, fsegment);
                mapfragTagStack.push(actlogtag);
            }
//           case "MeasureFragment":
//                actlogtag = "Measure" + actlogtag;
//                fsegment = fragmentManager.findFragmentById(R.id.measureSearchLayout);
//                if (fsegment instanceof SegmentDetailsFragment) {
//                    measurefragStack.put(actlogtag, fsegment);
//                    measurefragTagStack.push(actlogtag);
//                }
//                break;
//            case "ActivityTrackerFragment":
//                actlogtag = "Tracker" + actlogtag;
//                fsegment = fragmentManager.findFragmentById(R.id.trackerSearchLayout);
//                if (fsegment instanceof SegmentDetailsFragment) {
//                    trackerfragStack.put(actlogtag, fsegment);
//                    trackerfragTagStack.push(actlogtag);
//                }
//                break;
        }
    }

    private int dpToPx(float dp) {
        Resources resources = getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        int px = Math.round(dp * (metrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        return px;
    }
}
