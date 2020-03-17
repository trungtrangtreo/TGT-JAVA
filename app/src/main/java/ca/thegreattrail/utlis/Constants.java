package ca.thegreattrail.utlis;


import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Dev1 on 10/4/2016.
 */

public final class Constants {

    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 0; // to depend on distance only, it was 1000
    // Update frequency in seconds
    private static final int UPDATE_INTERVAL_IN_SECONDS = 10;  //  10 sec
    // Update frequency in milliseconds
    public static final long UPDATE_INTERVAL = MILLISECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;
    // The fastest update frequency, in seconds
    private static final int FASTEST_INTERVAL_IN_SECONDS =  10;  //
    // A smallest displacement in meter
    public static final int SMALLEST_DISPLACEMENT = 5; // 5m, It was 10
    // A fast frequency ceiling in milliseconds
    public static final long FASTEST_INTERVAL = MILLISECONDS_PER_SECOND * FASTEST_INTERVAL_IN_SECONDS;
    public static final int TIME_OUT = 10000;


    public static final int land = 0xFF38A800;       //  56-168-0
    public static final int water = 0xFF009DDC;     //    0, 157, 220
    public static final int gap = 0xFFE96D1F;       //   233 , 109, 31

    public static final int minElevationSamples = 10;
    public static final int maxElevationSamples = 50;
    public static final int maxElevationCoordinatePairs = 50;

    public static final String TIMESTAMP_FORMAT = "yyyyMMdd_HHmmss";
    public static final int TOLERANCE_DISTANCE = 20000;
    public static final int MARKER_WIDTH=80;
    public static final int MARKER_HEIGHT=100;
    public static final String API_KEY_FLICKR = "51181cfd2b7ef8ce5b2111fb971b3ec9";
    public static final String SECRET_FLICKR = "e855b9b8a807aa66";
    public static final String AUTH_TOKEN_FLICKR = "72157670878062012-37eca4a58c95f370";
    public static final String API_KEY_SEARCH = "AIzaSyDmUchHRXUJdnzGOE0vLD1txL7fvE3FeO8";
    public static final String NSID ="97852224@N00";
    public static final String UPLOAD_ALBUM_ID =  "72157671608185416" ;
    public static final String URL_SEARCH_FLIKR  = "https://api.flickr.com/services/rest/?&method=flickr.photos.search&format=json&nojsoncallback=1&user_id="+NSID+"&api_key="+API_KEY_FLICKR+"&tags=";
    public static final String URL_ADD_TO_PHOTOSET_FLICKR  = "https://api.flickr.com/services/rest/?method=flickr.photosets.addPhoto&api_key="+API_KEY_FLICKR+"&photoset_id="+UPLOAD_ALBUM_ID+"&format=rest&auth_token="+AUTH_TOKEN_FLICKR+"&api_sig=";
    public static final String UPLOAD_URL = "https://up.flickr.com/services/upload/";
    public static final String TRAIL_WARNING_SERVICE_URL = "https://devmap.thegreattrail.ca/arcgis/rest/services/TCT/TrailsFeatures/MapServer/0/query?where=Carto%3D1&text=&objectIds=&time=&geometry=&geometryType=esriGeometryPoint&inSR=&"+
                                                            "spatialRel=esriSpatialRelIntersects&relationParam=&outFields=Message%2CLocation&returnGeometry=true&returnTrueCurves=false&maxAllowableOffset=&geometryPrecision=&outSR=&returnIdsOnly=false&"+
                                                            "returnCountOnly=false&orderByFields=&groupByFieldsForStatistics=&outStatistics=&returnZ=false&returnM=false&gdbVersion=&returnDistinctValues=false&resultOffset=&resultRecordCount=&f=json";

    public static final LatLng DEFAULTORIGIN = new LatLng(56.1304,-106.3468);

    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }
}