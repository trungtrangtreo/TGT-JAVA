package ca.thegreattrail.ui.traildetail;

import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;
import java.io.IOException;
import java.io.StringReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.Map;
import ca.thegreattrail.R;
import ca.thegreattrail.utlis.AppHelper;
import ca.thegreattrail.utlis.Constants;
import ca.thegreattrail.utlis.VolleyMultipartRequest;
import ca.thegreattrail.utlis.VolleySingleton;

public class UploadFlickrActivity extends AppCompatActivity {

    private TextView shareTxt;
    private ImageView shareImg;
    public static Bitmap photo;
    public static String trailId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_flickr);

        shareTxt = findViewById(R.id.shareTxt);
        shareTxt.setText("");
        shareImg = findViewById(R.id.shareImg);
        shareImg.setImageBitmap(photo);
        Button shareBtn = findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String photoId = "";
                photoId = uploadFileToFlickr(shareTxt.getText().toString().trim(), trailId);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        shareTxt.getWindowToken(), 0);
            }
        });
    }

    private String uploadFileToFlickr(final String caption, final String trailId) {


        final String tags = "TrailCode_" + trailId + " APP";
        String apiSig = Constants.SECRET_FLICKR + "api_key" + Constants.API_KEY_FLICKR + "auth_token" + Constants.AUTH_TOKEN_FLICKR + "hidden1is_public1tags" + tags;
        final String md5 = md5(apiSig);
        String url = Constants.UPLOAD_URL;
        final String[] photoId = {""};

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);

        RequestQueue queue = Volley.newRequestQueue(this);
        // loading or check internet connection or something...
        // ... then


        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);


                Log.i("LocationService", "Response    ----------------------------------------------------------------------------------------------   " + resultResponse);

                try {
                    photoId[0] = parseXML(resultResponse);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();

                if (!photoId.equals("")) {
                    addToAlbum(photoId[0]);
                } else {
                    displayDialog("Fail To Upload", "Fail to upload the photo, Try in a while");
                    //exitThisfragment();
                    onBackPressed();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                NetworkResponse networkResponse = error.networkResponse;
                String errorMessage = "Unknown error";
                if (networkResponse == null) {
                    if (error.getClass().equals(TimeoutError.class)) {
                        errorMessage = "Request timeout";
                    } else if (error.getClass().equals(NoConnectionError.class)) {
                        errorMessage = "Failed to connect server";
                    }
                } else {
                    String result = new String(networkResponse.data);
                    try {
                        JSONObject response = new JSONObject(result);
                        String status = response.getString("status");
                        String message = response.getString("message");

                        Log.e("Error Status", status);
                        Log.e("Error Message", message);

                        if (networkResponse.statusCode == 404) {
                            errorMessage = "Resource not found";
                        } else if (networkResponse.statusCode == 401) {
                            errorMessage = message + " Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message + " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message + " Something is getting wrong";
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
                Log.i("Error", errorMessage);
                error.printStackTrace();
                progressDialog.dismiss();
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("api_key", Constants.API_KEY_FLICKR);
                params.put("auth_token", Constants.AUTH_TOKEN_FLICKR);
                params.put("api_sig", md5);
                params.put("hidden", "1");
                params.put("is_public", "1");
                params.put("tags", tags);

                return params;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();
                // file name could found file base or direct access from real path
                // for now just get bitmap data from ImageView
                params.put("photo", new DataPart(caption.trim() + ".jpg", AppHelper.getFileDataFromDrawable(photo), "image/jpeg"));
                return params;
            }
        };

        VolleySingleton.getInstance(this).addToRequestQueue(multipartRequest);

        return photoId[0];
    }

    private void addToAlbum(final String photoId) {
        // let string = "\(Config.flickr.secret)api_key\(Config.flickr.apiKey)auth_token\(Config.flickr.authToken)formatrestmethodflickr.photosets.addPhotophoto_id\(photoId)photoset_id\(albumId)"

        final String apiSig = Constants.SECRET_FLICKR + "api_key" + Constants.API_KEY_FLICKR + "auth_token" + Constants.AUTH_TOKEN_FLICKR + "formatrestmethodflickr.photosets.addPhotophoto_id" + photoId + "photoset_id" + Constants.UPLOAD_ALBUM_ID;
        //"\(Config.flickr.secret)api_key\(Config.flickr.apiKey)auth_token\(Config.flickr.authToken)formatrestmethodflickr.photosets.addPhotophoto_id\(photoId)photoset_id\(albumId)"
        final String md5 = md5(apiSig);
        String url = "https://api.flickr.com/services/rest/?method=flickr.photosets.addPhoto&api_key=" + Constants.API_KEY_FLICKR + "&photoset_id=" + Constants.UPLOAD_ALBUM_ID + "&photo_id=" + photoId + "&format=rest&auth_token=" + Constants.AUTH_TOKEN_FLICKR + "&api_sig=" + md5;

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Add photo to Album", "Please wait...", false, false);

        RequestQueue queue = Volley.newRequestQueue(this);  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.i("LocationService", "Response    ----------------------------------------------------------------------------------------------   " + response);
                        progressDialog.dismiss();
                        displayDialog("Successfull Upload", "The photo is uploaded succefully");
                        //exitThisfragment();
//                        activity.onBackPressed();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("LocationService", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();
                        displayDialog("Error", "The photo is not uploaded ");
                        //exitThisfragment();
                        onBackPressed();
                        progressDialog.dismiss();
                    }
                }
        ) {

        };
        queue.add(postRequest);
    }

    private void displayDialog(String title, String message) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                        // MainActivity.this.finish();
                        dialog.dismiss();
                        finish();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    private String parseXML(String xmlString) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        String photoId = "";

        xpp.setInput(new StringReader(xmlString));
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if (eventType == XmlPullParser.START_TAG) {
                if (xpp.getName().equals("photoid")) {
                    eventType = xpp.next();
                    photoId = xpp.getText();
                    return photoId;
                }

                System.out.println("Start tag " + xpp.getName());
            }
            eventType = xpp.next();
        }
        return photoId;
    }

    private String md5(String in) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.reset();
            digest.update(in.getBytes());
            byte[] a = digest.digest();
            int len = a.length;
            StringBuilder sb = new StringBuilder(len << 1);
            for (int i = 0; i < len; i++) {
                sb.append(Character.forDigit((a[i] & 0xf0) >> 4, 16));
                sb.append(Character.forDigit(a[i] & 0x0f, 16));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }
}
