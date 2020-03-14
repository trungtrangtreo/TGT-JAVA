package ca.thegreattrail.utlis;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.bluelinelabs.logansquare.LoganSquare;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import ca.thegreattrail.R;
import ca.thegreattrail.data.model.db.ParsableLatLngList;
import ca.thegreattrail.data.model.db.TrailSegmentLight;
import ca.thegreattrail.ui.main.MainActivity;
import static java.lang.Math.abs;
import static java.lang.Math.atan;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

/**
 * Created by Dev1 on 1/19/2017.
 */

public class Utility {
    private static final String TAG = "Utility";

    // display customized Toast message
    public static int SHORT_TOAST = 0;
    public static int LONG_TOAST = 1;
    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static boolean checkPermission(final Context context)
    {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if(currentAPIVersion>= Build.VERSION_CODES.M)
        {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) context, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
                    alertBuilder.setCancelable(true);
                    alertBuilder.setTitle("Permission necessary");
                    alertBuilder.setMessage("External storage permission is necessary");
                    alertBuilder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                        }
                    });
                    AlertDialog alert = alertBuilder.create();
                    alert.show();

                } else {
                    ActivityCompat.requestPermissions((Activity) context, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }
        } else {
            return true;
        }
    }



    public static void displayToast(Context caller, String toastMsg, int toastType){

        try {// try-catch to avoid stupid app crashes
            LayoutInflater inflater = LayoutInflater.from(caller);

            View mainLayout = inflater.inflate(R.layout.toast_layout, null);
            View rootLayout = mainLayout.findViewById(R.id.toast_layout_root);

            ImageView image = (ImageView) mainLayout.findViewById(R.id.image);
            // image.setImageResource(R.drawable.img_icon_notification);
            TextView text = (TextView) mainLayout.findViewById(R.id.text);
            text.setText(toastMsg);

            Toast toast = new Toast(caller);
            //toast.setGravity(Gravity.CENTER_VERTICAL, 0, 0);
            toast.setGravity(Gravity.BOTTOM, 0, 0);
            if (toastType==SHORT_TOAST)//(isShort)
                toast.setDuration(Toast.LENGTH_SHORT);
            else
                toast.setDuration(Toast.LENGTH_LONG);
            toast.setGravity(Gravity.BOTTOM, 0, 190);
            toast.setView(rootLayout);
            toast.show();
        }
        catch(Exception ex) {// to avoid stupid app crashes
         }
    }

    public static LatLng nearestPoint(LatLng point) {
        List<TrailSegmentLight> nearbySegments = TrailUtility.nearbySegments(point);
        if (nearbySegments != null) {
            LatLng first, second;
            double minDistance = Double.MAX_VALUE;
            LatLng nearestPoint = null;
            for (TrailSegmentLight segment : nearbySegments) {
                second = null;
                for (int i = 0; i < MainActivity.listPoints.get(segment.objectId).size(); i++) {
                    first = second;
                    second = MainActivity.listPoints.get(segment.objectId).get(i);
                    if(first != null){
                        LatLng projectedPoint = nearestPointSegment(first, second, point);
                        double distance = distance(projectedPoint, point);
                        if(distance < minDistance){
                            minDistance = distance;
                            nearestPoint = projectedPoint;
                        }
                    }
                }
            }
            return nearestPoint;
        }
        return null;
    }

    public static ArrayList<LatLng> decodeJSON(String jsonValue) {
        ArrayList<LatLng> points = new ArrayList<>();

        try {
            String jsonObjectList = ParsableLatLngList.constructJsonObjectList(jsonValue);
            List<ParsableLatLngList> objectsList = LoganSquare.parseList(jsonObjectList, ParsableLatLngList.class);
            ParsableLatLngList latLngList = objectsList.get(0);

            for (Double[] latlngPoint : latLngList.points) {
                points.add(new LatLng(latlngPoint[1], latlngPoint[0]));                                 // The backend sends [Longitude, Latitude]
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return points;
    }

    private static double EARTH_RADIUS = 6371;

    private static LatLng nearestPointGreatCircle(LatLng a, LatLng b, LatLng c) {
        double[] cartesianA = latLongtoCartesian(a);
        double[] cartesianB = latLongtoCartesian(b);
        double[] cartesianC = latLongtoCartesian(c);

        double[] G = dotProduct(cartesianA, cartesianB);
        double[] F = dotProduct(cartesianC, G);
        double[] t = dotProduct(G, F);
        t = normalizeVector(t);
        t = multiplyWithScalar(t, EARTH_RADIUS);
        return cartesianToLatLong(t);
    }

    static LatLng nearestPointSegment(LatLng a, LatLng b, LatLng c) {
        LatLng t = nearestPointGreatCircle(a, b, c);
        if (onSegmentAB(a, b, t)) return t;
        return (distance(a, c) < distance(b, c)) ? a : b;
    }

    private static boolean onSegmentAB(LatLng a, LatLng b, LatLng t) {
        return abs(distance(a, b) - distance(a, t) - distance(b, t)) < 10e-5;
    }

    public static double distance(LatLng x, LatLng y) {
        double lat1 = x.latitude, lon1 = x.longitude, lat2 = y.latitude, lon2 = y.longitude;
        double dLat = toRadians(lat2 - lat1);
        double dLon = toRadians(lon2 - lon1);
        double a = sin(dLat / 2) * sin(dLat / 2) + cos(toRadians(lat1))
                * cos(toRadians(lat2)) * sin(dLon / 2) * sin(dLon / 2);
        double c = 2 * atan2(sqrt(a), sqrt(1 - a));
        return EARTH_RADIUS * c;
    }

    private static double[] latLongtoCartesian(LatLng latlong) {
        double lat = toRadians(latlong.latitude);
        double lng = toRadians(latlong.longitude);
        return new double[]{EARTH_RADIUS * cos(lat) * cos(lng), EARTH_RADIUS * cos(lat) * sin(lng), EARTH_RADIUS * sin(lat)};
    }

    private static double[] dotProduct(double[] b, double[] c) {
        return new double[]{b[1] * c[2] - b[2] * c[1], b[2] * c[0] - b[0] * c[2], b[0] * c[1] - b[1] * c[0]};
    }

    private static double mag(double[] x) {
        double mag = 0;
        for (double i : x) {
            mag += i * i;
        }
        return sqrt(mag);
    }

    private static double[] normalizeVector(double[] x) {
        double[] ret = new double[x.length];
        double mag = mag(x);
        for (int i = 0; i < x.length; i++) {
            ret[i] = x[i] / mag;
        }
        return ret;
    }

    private static double[] multiplyWithScalar(double[] x, double c) {
        double[] y = new double[x.length];
        for (int i = 0; i < y.length; i++) {
            y[i] = x[i] * c;
        }
        return y;
    }

    private static LatLng cartesianToLatLong(double[] cart) {
        double lng = toDegrees(atan(cart[1] / cart[0]));
        if (cart[0] < 0) {
            if (cart[1] > 0)
                lng += 180;
            else
                lng -= 180;
        }
        double lat = toDegrees(atan(cart[2] * cos(toRadians(lng)) / cart[0]));
        return new LatLng(lat, lng);
    }


}
