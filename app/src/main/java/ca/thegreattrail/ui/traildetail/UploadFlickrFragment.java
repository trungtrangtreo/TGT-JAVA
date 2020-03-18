package ca.thegreattrail.ui.traildetail;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
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
import ca.thegreattrail.ui.base.BaseFragment;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.utlis.AppHelper;
import ca.thegreattrail.utlis.Constants;
import ca.thegreattrail.utlis.VolleyMultipartRequest;
import ca.thegreattrail.utlis.VolleySingleton;

public class UploadFlickrFragment extends BaseFragment {

    public static UploadFlickrFragment instance = null;
     private TextView shareTxt ;
    private ImageView shareImg;
    public Bitmap photo;
    public String trailId = "";

    public static UploadFlickrFragment getInstance() {

        if (instance == null) {
            Log.i("Instance AT", "New Creation");
            instance = new UploadFlickrFragment();
        }
        Log.i("Instance AT", "No Creation");
        return instance;
    }


    public static UploadFlickrFragment newInstance() {
        UploadFlickrFragment uploadFlickrFragment = getInstance();

        return uploadFlickrFragment;
    }

    public UploadFlickrFragment() {
        // Required empty public constructor
    }

    @Override
    public int getLayoutId() {
        return 0;
    }


    /* @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(photo != null){
            shareImg.setImageBitmap(photo);
        }
    }*/

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_upload_flickr, container, false);

        shareTxt = view.findViewById(R.id.shareTxt);
        shareTxt.setText("");
        shareImg = view.findViewById(R.id.shareImg);
        shareImg.setImageBitmap(photo);
        Button shareBtn  = (Button) view.findViewById(R.id.shareBtn);
        shareBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String photoId = "" ;
                photoId = uploadFileToFlickr(shareTxt.getText().toString().trim(), trailId);


                InputMethodManager imm = (InputMethodManager) getBaseActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(
                        shareTxt.getWindowToken(), 0);

                /*if(!photoId.equals("")){
                    addToAlbum(photoId);
                    displayDialog("Successfull Upload","The photo is uploaded succefully");
                }*/


             //   uploadFile(shareTxt.getText().toString().trim(), trailId);

               // saveProfileAccount(shareTxt.getText().toString().trim(), trailId);
               /* try {
                    parseXML("<?xml version=\"1.0\" encoding=\"utf-8\" ?> <rsp stat=\"ok\"><photoid>30913062063</photoid></rsp>");
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                /*File file = new File(""); //provide a valid file
                POST(client, url, uploadRequestBody("title", "png", "someUploadToken", file));*/
            }
        });

        return view;

    }

    @Override
    public void onResume() {
        super.onResume();

        shareTxt.setText("");
    }



  /*  //Upload request body
    public static MultipartBody uploadRequestBody(String title, String imageFormat, String token, File file) {

        MediaType MEDIA_TYPE = MediaType.parse("image/" + imageFormat); // e.g. "image/png"
        return new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("action", "upload")
                .addFormDataPart("format", "json")
                .addFormDataPart("filename", title + "." + imageFormat) //e.g. title.png --> imageFormat = png
                .addFormDataPart("file", "...", RequestBody.create(MEDIA_TYPE, file))
                .addFormDataPart("token", token)
                .build();
    }



    //POST network request
    public static String POST(OkHttpClient client, HttpUrl url, RequestBody body) throws IOException {
        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();
        Response response = client.newCall(request).execute();
        return response.body().string();
    }


    private void attemptLogin(String url) {
        new AsyncTask<String, Void, Void>() {
            @Override
            protected Void doInBackground(String... params) {

                try {
                   String  response = POST(
                            client,
                            params[0],
                            LoginBody("username", "password","token"));

                    Log.d("Response", response);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        }.execute(url);
    }


    //Login request body
    public static RequestBody LoginBody(String username, String password, String token) {
        return new FormBody.Builder()
                .add("action", "login")
                .add("format", "json")
                .add("username", username)
                .add("password", password)
                .add("logintoken", token)
                .build();
    }

    public static HttpUrl buildURL() {
        return new HttpUrl.Builder()
                .scheme("https") //http
                .host("www.somehostname.com")
                .addPathSegment("pathSegment")//adds "/pathSegment" at the end of hostname
                .addQueryParameter("param1", "value1") //add query parameters to the URL
                .addEncodedQueryParameter("encodedName", "encodedValue")//add encoded query parameters to the URL
                .build();
        *//**
         * The return URL:
         *  https://www.somehostname.com/pathSegment?param1=value1&encodedName=encodedValue
         *//*
    }

*/

    private void addToAlbum(final String photoId){
        // let string = "\(Config.flickr.secret)api_key\(Config.flickr.apiKey)auth_token\(Config.flickr.authToken)formatrestmethodflickr.photosets.addPhotophoto_id\(photoId)photoset_id\(albumId)"

        final String apiSig = Constants.SECRET_FLICKR+"api_key"+ Constants.API_KEY_FLICKR+"auth_token"+ Constants.AUTH_TOKEN_FLICKR+"formatrestmethodflickr.photosets.addPhotophoto_id"+photoId+"photoset_id"+ Constants.UPLOAD_ALBUM_ID;
                    //"\(Config.flickr.secret)api_key\(Config.flickr.apiKey)auth_token\(Config.flickr.authToken)formatrestmethodflickr.photosets.addPhotophoto_id\(photoId)photoset_id\(albumId)"
        final String md5 =  md5(apiSig);
        String url = "https://api.flickr.com/services/rest/?method=flickr.photosets.addPhoto&api_key="+ Constants.API_KEY_FLICKR+"&photoset_id="+ Constants.UPLOAD_ALBUM_ID+"&photo_id="+photoId+"&format=rest&auth_token="+ Constants.AUTH_TOKEN_FLICKR+"&api_sig="+md5;

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"Add photo to Album","Please wait...",false,false);

        RequestQueue queue = Volley.newRequestQueue(getActivity());  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.i("LocationService","Response    ----------------------------------------------------------------------------------------------   "+response);
                        progressDialog.dismiss();
                        displayDialog("Successfull Upload","The photo is uploaded succefully");
                        //exitThisfragment();
//                        activity.onBackPressed();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("LocationService", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();
                        displayDialog("Error","The photo is not uploaded ");
                        //exitThisfragment();
                        getBaseActivity().onBackPressed();
                        progressDialog.dismiss();
                    }
                }
        ) {

        };
        queue.add(postRequest);


    }

    /*private  void uploadFile(final String caption, final String trailId) {


        final String tags = "TrailCode_"+trailId+ " APP";
        String apiSig = Constants.SECRET_FLICKR+"api_key"+Constants.API_KEY_FLICKR+"auth_token"+Constants.AUTH_TOKEN_FLICKR+"hidden1is_public1tags"+tags;
        final String md5 =  md5(apiSig);
        String url = Constants.UPLOAD_URL;

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"Uploading...","Please wait...",false,false);

        RequestQueue queue = Volley.newRequestQueue(getActivity());  // this = context


        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.i("LocationService","Response    ----------------------------------------------------------------------------------------------   "+response);
                        progressDialog.dismiss();
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("LocationService", error.getMessage());
                        // TODO handle the error
                        error.printStackTrace();
                        progressDialog.dismiss();
                    }
                }
        ) {
            @Override
            protected Map<String, String> getParams()
            {
                String uuid = UUID.randomUUID().toString();
                String boundaryConstant = "Boundary"+uuid;
                String contentType = "multipart/form-data; boundary="+boundaryConstant;
                String body = "" ;


                //  Set POST data
                Map<String, String>  params = new HashMap<String, String>();


                params.put("Content-Type", contentType+"\r\n");

                // Set API key
                body += "--"+boundaryConstant+"\r\n";
                body += "Content-Disposition: form-data; name=\"api_key\"\r\n\r\n";
                body += Constants.API_KEY_FLICKR+"\r\n";

                // Set auth token
                body += "--"+boundaryConstant+"\r\n";
                body += "Content-Disposition: form-data; name=\"auth_token\"\r\n\r\n";
                body += Constants.AUTH_TOKEN_FLICKR+"\r\n";

                // Set API sig
                body += "--"+boundaryConstant+"\r\n";
                body += "Content-Disposition: form-data; name=\"api_sig\"\r\n\r\n";
                body += md5+"\r\n";

                // Set hidden = 1 no
                body += "--"+boundaryConstant+"\r\n";
                body += "Content-Disposition: form-data; name=\"hidden\"\r\n\r\n";
                body += "1\r\n";

                // Set is_public = 1 yes
                body += "--"+boundaryConstant+"\r\n";
                body += "Content-Disposition: form-data; name=\"is_public\"\r\n\r\n";
                body += "1\r\n";

                // Tag the photo
                body += "--"+boundaryConstant+"\r\n";
                body += "Content-Disposition: form-data; name=\"tags\"\r\n\r\n";
                body += tags+"\r\n";

                // set file name amd photo data
                String fileName = "Photo upload";
                if(!caption.equals("")){
                    fileName = caption ;
                }


                String photoString = getStringImage(photo);

                body += "--"+boundaryConstant+"\r\n";
                body += "Content-Disposition: form-data; name=\"photo\"; filename=\""+fileName+"\"\r\n";
                body += "Content-Type: image/jpeg\r\n";
                body += photoString;
                body += "\r\n--"+boundaryConstant+"--\r\n";

                params.put("body", body);


                *//*params.put("api_key", Constants.API_KEY_FLICKR+"\r\n");
                params.put("auth_token"+"\r\n\r\n", Constants.AUTH_TOKEN_FLICKR+"\r\n");
                params.put("api_sig"+"\r\n\r\n", md5+"\r\n");
                params.put("hidden"+"\r\n\r\n", "1"+"\r\n");
                params.put("is_public"+"\r\n\r\n", "1"+"\r\n");
                params.put("tags"+"\r\n\r\n", tags+"\r\n");
                params.put("photo", caption+"\r\n");
                params.put("image/jpg"+"\r\n\r\n", photoString+"\r\n");*//*


                return params;
            }
        };
        queue.add(postRequest);



    }

*/


    /*private void uploadImage(){
        //Showing the progress dialog
        final ProgressDialog loading = ProgressDialog.show(getActivity(),"Uploading...","Please wait...",false,false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String s) {
                        //Disimissing the progress dialog
                        loading.dismiss();
                        //Showing toast message of the response
                        Toast.makeText(getActivity(), s , Toast.LENGTH_LONG).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Dismissing the progress dialog
                        loading.dismiss();

                        //Showing toast
                        Toast.makeText(getActivity(), volleyError.getMessage().toString(), Toast.LENGTH_LONG).show();
                    }
                }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //Converting Bitmap to String
                String image = getStringImage(bitmap);

                //Getting Image Name
                String name = shareTxt.getText().toString().trim();

                //Creating parameters
                Map<String,String> params = new Hashtable<String, String>();

                //Adding parameters
                params.put(KEY_IMAGE, image);
                params.put(KEY_NAME, name);

                //returning parameters
                return params;
            }
        };

        //Creating a Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());

        //Adding request to the queue
        requestQueue.add(stringRequest);
    }

*/
    /*public String getStringImage(Bitmap bmp){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }*/

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
        } catch (NoSuchAlgorithmException e) { e.printStackTrace(); }
        return null;
    }





    private String uploadFileToFlickr(final String caption, final String trailId) {


        final String tags = "TrailCode_"+trailId+ " APP";
        String apiSig = Constants.SECRET_FLICKR+"api_key"+ Constants.API_KEY_FLICKR+"auth_token"+ Constants.AUTH_TOKEN_FLICKR+"hidden1is_public1tags"+tags;
        final String md5 =  md5(apiSig);
        String url = Constants.UPLOAD_URL;
        final String[] photoId = {""};

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(),"Uploading...","Please wait...",false,false);

        RequestQueue queue = Volley.newRequestQueue(getActivity());
        // loading or check internet connection or something...
        // ... then


        VolleyMultipartRequest multipartRequest = new VolleyMultipartRequest(Request.Method.POST, url, new Response.Listener<NetworkResponse>() {
            @Override
            public void onResponse(NetworkResponse response) {
                String resultResponse = new String(response.data);


                Log.i("LocationService","Response    ----------------------------------------------------------------------------------------------   "+resultResponse);

                try {
                    photoId[0] = parseXML(resultResponse);
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                progressDialog.dismiss();

                if(!photoId.equals("")){
                    addToAlbum(photoId[0]);
                }
                else{
                    displayDialog("Fail To Upload","Fail to upload the photo, Try in a while");
                    //exitThisfragment();
                    getBaseActivity().onBackPressed();
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
                            errorMessage = message+" Please login again";
                        } else if (networkResponse.statusCode == 400) {
                            errorMessage = message+ " Check your inputs";
                        } else if (networkResponse.statusCode == 500) {
                            errorMessage = message+" Something is getting wrong";
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
                params.put("photo", new DataPart(caption.trim()+".jpg", AppHelper.getFileDataFromDrawable(photo), "image/jpeg"));
                return params;
            }
        };

        VolleySingleton.getInstance(getActivity().getBaseContext()).addToRequestQueue(multipartRequest);

        return photoId[0];
    }


    private String parseXML(String xmlString) throws XmlPullParserException, IOException {
        XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
        factory.setNamespaceAware(true);
        XmlPullParser xpp = factory.newPullParser();
        String photoId = "";

        xpp.setInput( new StringReader( xmlString ) );
        int eventType = xpp.getEventType();
        while (eventType != XmlPullParser.END_DOCUMENT) {
            if(eventType == XmlPullParser.START_TAG) {
                if(xpp.getName().equals("photoid")){
                    eventType = xpp.next();
                    photoId = xpp.getText();
                    return photoId;
                }

                System.out.println("Start tag "+xpp.getName());
            }
            eventType = xpp.next();
        }
        return photoId;
    }

    private void displayDialog(String title, String message){
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());

        // set title
        alertDialogBuilder.setTitle(title);

        // set dialog message
        alertDialogBuilder
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton("OK",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close
                        // current activity
                       // MainActivity.this.finish();
                        dialog.dismiss();
                    }
                }) ;

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

}

