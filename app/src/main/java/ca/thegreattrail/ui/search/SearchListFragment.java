package ca.thegreattrail.ui.search;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Point;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.ListView;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.android.gms.maps.model.CameraPosition;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelperTrail;
import ca.thegreattrail.data.local.db.AmenityDBHelperTrail;
import ca.thegreattrail.data.model.db.TrailSegment;
import ca.thegreattrail.ui.main.MainActivity;

/**
 * Created by hardikfumakiya on 2016-12-11.
 */

public class SearchListFragment extends Fragment {

    private static final String ARGUMENT_CAMERA_POSITION = "ARGUMENT_CAMERA_POSITION";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";
    public static final String API_KEY = "AIzaSyDmUchHRXUJdnzGOE0vLD1txL7fvE3FeO8";//AIzaSyAiYlTgt7c0dA3aQkjofkf_IfF23d-4juQ
    public static int resourceID;
    public static HashMap<String, Fragment> fragStack = new HashMap<>();
    public static Stack<String> fragTagStack = new Stack<>();
    public SearchAdapter mAdapter;
    public ArrayList<TrailSegment> trailSegments = new ArrayList<>();
    public ArrayList<GooglePlace> mGoogleAutoCompleteArray = new ArrayList<>();

    private Tracker mTracker;
    private CameraPosition mLatestCameraPosition;

    ListView search_service;
    int visibleSearchItemCount = 0;
    String searchData;
    MainActivity activity;


    public SearchListFragment() {
        super();
        // Required empty public constructor
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        activity = (MainActivity) context;
    }

    public static SearchListFragment newInstance(CameraPosition cameraPosition) {
        Bundle args = new Bundle();
        args.putParcelable(ARGUMENT_CAMERA_POSITION, cameraPosition);

        SearchListFragment fragment = new SearchListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public void refreshData(Context activity, String searchText) {
        searchData=searchText;
        trailSegments.clear();
        //mGoogleAutoCompleteArray.clear();
        Log.d("searchText:", "" + searchText);
        if (!searchText.equals("")) {
//            FetchTrailbyText(activity,searchText);
        }
//        autocomplete(searchText);
        // FetchListData();
        if (mAdapter != null)
            mAdapter.filter(searchText);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null)
            mLatestCameraPosition = getArguments().getParcelable(ARGUMENT_CAMERA_POSITION);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search, container, false);

        // [START shared_tracker]
        // Obtain the shared Tracker instance.
//        MvvmApp application = (MvvmApp) getActivity().getApplication();
//        mTracker = application.getDefaultTracker();
//        // [END shared_tracker]
//
//        search_service = (ListView) v.findViewById(R.id.search_service);
//        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back);
////        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("");
//
//        //setHasOptionsMenu(true);
//        ImageView empty = new ImageView(getContext());
//        AbsListView.LayoutParams lp = new AbsListView.LayoutParams(
//                LinearLayout.LayoutParams.MATCH_PARENT,
//                LinearLayout.LayoutParams.WRAP_CONTENT);
//        empty.setLayoutParams(lp);
//        empty.setPadding(20, 20, 20, 20);
//        empty.setBackgroundColor(Color.parseColor("#E9E1E6"));
//        empty.setImageResource(R.drawable.powered_by_google_dark);
//
//        search_service.addFooterView(empty);
//        search_service.setOnScrollListener(new AbsListView.OnScrollListener() {
//            @Override
//            public void onScrollStateChanged(AbsListView view, int scrollState) {
//
////                hideKeyboard();
//                // searchView.clearFocus();
//            }
//
//            @Override
//            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
//                visibleSearchItemCount = visibleItemCount;
//            }
//        });

//        FetchListData();

//        search_service.setOnItemClickListener(
//                new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//
//                        int itemType = mAdapter.getItemViewType(position);
//                        ListViewItem itemData = mAdapter.getItem(position);
//
//                        String parentSection = itemData.getParentSection();
//
////                        hideKeyboard();
//
//                        if (itemType == SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW) {
//                            activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
//
//                            switch (parentSection) {

//                                case "Trails":
//                                    try {
//                                        String name = itemData.getObject();
//                                        if (NeedToSave(name))
//                                            SaveRecentSearch(name);
//
//                                        pushSearchFragmentToStack();
//
//                                        TrailDetailFragment trailSegment = new TrailDetailFragment();
//                                        Bundle bundle = new Bundle();
//                                        bundle.putString("trailid", itemData.getTrailid());
//                                        trailSegment.setArguments(bundle);
//
//                                        replaceFragment(resourceID, trailSegment);

//                                    } catch (Exception e) {
//                                        Log.d("Trails clicked", e.getMessage());
//                                    }

//                                    break;
//                                case "RecentSearch":

//                                    if (!itemData.getObject().equals(activity.getResources().getString(R.string.more))) {
//                                        FetchTrailbyText(activity,itemData.getObject());
//                                        autocomplete(itemData.getObject());
//                                        FetchListData();
//                                        mAdapter.filter(itemData.getObject());
//
//                                    } else {
//                                        pushSearchFragmentToStack();
//
//                                        MoreListFragment frag = new MoreListFragment();
//                                        Bundle bundle = new Bundle();
//                                        bundle.putString("title", activity.getResources().getString(R.string.recent_search));
//                                        bundle.putString("itemType", "RecentSearch");
//                                        frag.setArguments(bundle);
//
//                                        replaceFragment(resourceID, frag);
//                                    }
//                                    break;
//                                case "TrailService":
//
//                                    pushSearchFragmentToStack();
//
//                                    String amenityType = itemData.getObject();
//                                    AmenitiesMarkerFragment markerFragment = AmenitiesMarkerFragment.newInstance(amenityType, mLatestCameraPosition);
//                                    replaceFragment(resourceID, markerFragment);

//                                    break;
//                                case "GooglePlacesService":

//                                    if (!itemData.getObject().equals(activity.getResources().getString(R.string.more)))
//                                    {
//                                        pushSearchFragmentToStack();
//                                        GooglePlaceCategoryFragment gfragment = new GooglePlaceCategoryFragment();
//                                        gfragment.categoryType = itemData.getObject();
//                                        replaceFragment(resourceID, gfragment);
//                                    }
//                                    else
//                                    {
//                                        pushSearchFragmentToStack();
//
//                                        MoreListFragment frag = new MoreListFragment();
//                                        Bundle bundle = new Bundle();
//                                        bundle.putString("title", activity.getResources().getString(R.string.categories));
//                                        bundle.putString("itemType", "GooglePlacesService");
//                                        bundle.putString("resourceID", "" + resourceID);
//                                        frag.setArguments(bundle);
//
//                                        replaceFragment(resourceID, frag);
//                                    }

//                                    break;
//                                case "GooglePlaces":
//                                    pushSearchFragmentToStack();
//
//                                    GooglePlaceFragment placefragment = new GooglePlaceFragment();
//                                    Bundle bundle = new Bundle();
//                                    bundle.putString("placeid", itemData.getTrailid());
//                                    placefragment.setArguments(bundle);
//
//                                    replaceFragment(resourceID, placefragment);

//                                    break;
//                                default:
//                                    break;
//                            }
//                        }
//                    }
//                }
//        );

        return v;
    }


    @Override
    public void onResume() {
        super.onResume();

        // [START screen_view_hit]
        mTracker.setScreenName("Search Map View");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
        // [END screen_view_hit]

    }

    private void replaceFragment(int resourceID, Fragment replaceFragment) {
        FragmentTransaction mFragmentTransaction = activity.getSupportFragmentManager().beginTransaction();

        mFragmentTransaction
                .replace(resourceID, replaceFragment)
                .commit();
    }

    private void pushSearchFragmentToStack() {
        FragmentManager fragmentManager = activity.getSupportFragmentManager();
        Fragment fsearch = null;
        String searchtag = getFragmentTagbyTab("SearchFragment");

//        switch (MainActivity.currentTab) {
//            case "MapFragment":
//                fsearch = fragmentManager.findFragmentById(R.id.searchLayout);
//
//                break;
//            case "MeasureFragment":
//                fsearch = fragmentManager.findFragmentById(R.id.measureSearchLayout);
//                break;
//
//            case "ActivityTrackerFragment":
//                fsearch = fragmentManager.findFragmentById(R.id.trackerSearchLayout);
//                break;
//        }

        if (fsearch instanceof SearchListFragment) {
            fragStack.put(searchtag, fsearch);
            fragTagStack.push(searchtag);
        }
    }

    private void hideKeyboard() {
        View focusedView = activity.getCurrentFocus();
        if (focusedView != null) {
            InputMethodManager imm = (InputMethodManager)activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private String getFragmentTagbyTab(String fragment) {
        String tag = null;
//        switch (MainActivity.currentTab) {
//            case "MapFragment":
//                tag = "Map" + fragment;
//                break;
//            case "MeasureFragment":
//                tag = "Measure" + fragment;
//                break;
//            case "ActivityTrackerFragment":
//                tag = "Tracker" + fragment;
//                break;
//        }
        return tag;
    }

    // google place autocomplete api call
    public void autocomplete(String input) {
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:ca");
            sb.append("&input=").append(URLEncoder.encode(input, "utf8"));
            URL url = new URL(sb.toString());
            searchData = input;

            PlacesTask placesTask = new PlacesTask();
            placesTask.execute(url.toString());


        } catch (MalformedURLException e) {
            Log.e("autocomplete", "Error processing Places API URL", e);

        } catch (IOException e) {
            Log.e("autocomplete", "Error connecting to Places API", e);

        }
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();

            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }

            data = sb.toString();
            br.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    public void FetchListData() {

        ArrayList<ListViewItem> listItems = new ArrayList<>();
        ArrayList<ListViewItem> searchItems = new ArrayList<>();
        ArrayList<ListViewItem> sectionItems = new ArrayList<>();
        ListViewItem x;
        Resources resources = activity.getResources();

        //if (trailSegments.size() > 0) {//searchData
        if (searchData != null && !searchData.equals("")) {
            x = new ListViewItem(SearchAdapter.ListItemType.HEADER_VIEW, activity.getResources().getString(R.string.label_trails), trailSegments.size());
            sectionItems.add(x);
            listItems.add(x);

            for (TrailSegment segment : trailSegments) {
                x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, segment.getTrailName(), "Trails", segment.getTrailId());
                searchItems.add(x);
                listItems.add(x);
            }

        }

        if (searchData == null || searchData.equals("")) {
            SharedPreferences preferences = activity.getSharedPreferences("SearchPreferences", Context.MODE_PRIVATE);
            Set<String> searchText = preferences.getStringSet("search", new HashSet<String>());


            if (searchText.size() > 0) {
                List<String> searchList = new ArrayList<>(searchText);
                Collections.sort(searchList, new Comparator<String>() {
                    @Override
                    public int compare(String text1, String text2) {
                        return text1.compareToIgnoreCase(text2);
                    }
                });

                x = new ListViewItem(SearchAdapter.ListItemType.HEADER_VIEW, activity.getResources().getString(R.string.label_recent_search), 3);
                sectionItems.add(x);
                listItems.add(x);

                for (int i = 0; i < searchList.size(); i++)//for(String searchStr: searchList)
                {
                    if (i == 3)
                        break;
                    x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, searchList.get(i), "RecentSearch");
                    listItems.add(x);
                    searchItems.add(x);

                }
                if (searchList.size() > 3) {
                    x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, activity.getResources().getString(R.string.label_more), "RecentSearch");
                    listItems.add(x);
                }
            }
        }


        String[] mTrailServiceArray, mGooglePlacesServiceArray;

        AmenityDBHelperTrail db = AmenityDBHelperTrail.getInstance(activity);
        mTrailServiceArray = db.getAmenitiesBySearchText(searchData);

        x = new ListViewItem(SearchAdapter.ListItemType.HEADER_VIEW, activity.getResources().getString(R.string.label_trail_services), mTrailServiceArray.length);
        sectionItems.add(x);
        listItems.add(x);

        for (String tServices : mTrailServiceArray) {
            x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, tServices, "TrailService");
            searchItems.add(x);
            listItems.add(x);
        }


        mGooglePlacesServiceArray = resources.getStringArray(R.array.googlePlacesServiceArray);


        if (searchData == null || searchData.equals(""))
        {
            WindowManager wmanager = activity.getWindowManager();
            int childlimit = 17;
            if (wmanager != null) {
                Display display = wmanager.getDefaultDisplay();
                Point screenSize = new Point();
                display.getSize(screenSize);
                int screenHeight = screenSize.y;
                int rowHeight = dpToPx(32);
                childlimit = ((screenHeight - 180) / rowHeight) - 12;


            }
            x = new ListViewItem(SearchAdapter.ListItemType.HEADER_VIEW, activity.getResources().getString(R.string.label_google_place_service), childlimit);
            sectionItems.add(x);
            listItems.add(x);

            for (int i = 0; i < childlimit; i++) {
                //mGooglePlacesServiceArray[i],tServices
                x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, mGooglePlacesServiceArray[i], "GooglePlacesService");
                searchItems.add(x);
                listItems.add(x);

            }

            x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, activity.getResources().getString(R.string.label_more), "GooglePlacesService");
            listItems.add(x);
        }
        else
        {
            x = new ListViewItem(SearchAdapter.ListItemType.HEADER_VIEW, activity.getResources().getString(R.string.label_google_place_service), 0);
            sectionItems.add(x);
            listItems.add(x);

            for (String itemString: mGooglePlacesServiceArray) {

                String tempString=removeAccents(itemString);
                if(tempString.toLowerCase().contains(searchData))
                {
                    x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, itemString, "GooglePlacesService");
                    searchItems.add(x);
                    listItems.add(x);
                }

            }
        }
        // Google Places from web api
        //int size = mGoogleAutoCompleteArray.size();
        //if (size > 0) {
        if (searchData != null && !searchData.equals("")) {
            x = new ListViewItem(SearchAdapter.ListItemType.HEADER_VIEW, activity.getResources().getString(R.string.label_google_place), mGoogleAutoCompleteArray.size());
            sectionItems.add(x);
            listItems.add(x);
            for (GooglePlace place : mGoogleAutoCompleteArray) {
                x = new ListViewItem(SearchAdapter.ListItemType.CONTEXT_PLUGIN_VIEW, place.getPlaceName(), "GooglePlaces", place.getPlace_id());
                searchItems.add(x);
                listItems.add(x);
            }
        }

        mAdapter = new SearchAdapter(getContext(), listItems, searchItems, sectionItems);
        search_service.setAdapter(mAdapter);
    }

    public int dpToPx(int dp) {
        //Context context = MainActivity.context;
        Resources resources = activity.getResources();
        DisplayMetrics metrics = resources.getDisplayMetrics();
        return (int) (dp * ((float) metrics.densityDpi / DisplayMetrics.DENSITY_DEFAULT));
    }

    public void FetchTrailbyText(Context activity, String text) {
        ActivityDBHelperTrail db = new ActivityDBHelperTrail(activity);
        Cursor cursor = db.getTrailsBySearch(text);
        trailSegments.clear();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    TrailSegment segment = new TrailSegment(cursor);
                    trailSegments.add(segment);
                } catch (IllegalStateException e) {
                    // exeption due to oversize field of the cursor
                    continue;
                }
            }
            while (cursor.moveToNext());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //amenitySegments.clear();
        trailSegments.clear();
        mGoogleAutoCompleteArray.clear();
        // mGooglePlacesArray.clear();
    }

    public String removeAccents(String s){
        try{
            s = s.toLowerCase();
            if(Build.VERSION.SDK_INT >  Build.VERSION_CODES.FROYO){
                s = Normalizer.normalize(s, Normalizer.Form.NFD);
                s = s.replaceAll("[^\\p{ASCII}]", "");
            } else{
                s = s.replace("á", "a").replace("é", "e").replace("í", "i").replace("ó", "o").replace("ú", "u").replace("ñ", "n");
            }
        }catch(Exception e){

        }
        return s;
    }


    public void SearchViewIconify() {
//        MapFragment mapFragment = ((MainActivity) getActivity()).getMapFragment();
//        MeasureFragment measureFragment = ((MainActivity) getActivity()).getMeasureFragment();
//        ActivityTrackerFragment activityTrackerFragment = ((MainActivity) getActivity()).getActivityTrackerFragment();
//
//
//        if(mapFragment == null)
//            return;
//        switch(currentTab){
//            case "MapFragment":
//                mapFragment.searchView.setVisibility(View.GONE);
//                break;
//            case "MeasureFragment":
//                measureFragment.searchView.setVisibility(View.GONE);
//                break;
//            case "ActivityTrackerFragment":
//                activityTrackerFragment.searchView.setVisibility(View.GONE);
//                break;
//        }
//        switch (MainActivity.currentTab) {
//            case "MapFragment":
//                mapFragment.searchView.setInputType(InputType.TYPE_NULL);
//                // enableSearchView(mapFragment.searchView,false);
//                break;
//            case "MeasureFragment":
//                mapFragment.searchView.setInputType(InputType.TYPE_NULL);
//                //enableSearchView(measureFragment.searchView,false);
//                break;
//            case "ActivityTrackerFragment":
//                mapFragment.searchView.setInputType(InputType.TYPE_NULL);
//                //enableSearchView(activityTrackerFragment.searchView,false);
//                break;
//        }
    }

    private void enableSearchView(SearchView view, boolean enabled) {
        ImageView clearButton = (ImageView) view.findViewById(R.id.search_close_btn);
        SearchView.SearchAutoComplete searchEditText = (SearchView.SearchAutoComplete) view.findViewById(R.id.search_src_text);

        clearButton.setEnabled(false);
        searchEditText.setEnabled(false);
        view.setSubmitButtonEnabled(false);

//        view.setEnabled(enabled);
//        if (view instanceof ViewGroup) {
//            ViewGroup viewGroup = (ViewGroup) view;
//            for (int i = 0; i < viewGroup.getChildCount(); i++) {
//                View child = viewGroup.getChildAt(i);
//                enableSearchView(child, enabled);
//            }
//        }
    }

    public boolean NeedToSave(String newText) {
        SharedPreferences preferences = getContext().getSharedPreferences("SearchPreferences", Context.MODE_PRIVATE);
        Set<String> searchText = preferences.getStringSet("search", new HashSet<String>());
        List<String> searchList = new ArrayList<>(searchText);

        if (searchList.size() > 0) {
            if (searchList.contains(newText))
                return false;
        }
        return true;
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void SaveRecentSearch(String newText) {
        SharedPreferences preferences = getContext().getSharedPreferences("SearchPreferences", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> searchText = preferences.getStringSet("search", new HashSet<String>());
        List<String> searchList = new ArrayList<>(searchText);
        boolean res = searchList.add(newText);
        searchText = new HashSet<>(searchList);

        //Toast.makeText(getContext(), "SaveRecentSearch : " + res, Toast.LENGTH_SHORT).show();
        editor.putStringSet("search", searchText);
        editor.apply();
    }

    private class PlacesTask extends AsyncTask<String, Integer, String> {

        String datas = null;

        @Override
        protected String doInBackground(String... url) {
            try {
                datas = downloadUrl(url[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return datas;
        }

        @Override
        protected void onPostExecute(String result) {

            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

        JSONObject jObject;

        // Invoked by execute() method of this object
        @Override
        protected List<HashMap<String, String>> doInBackground(String... jsonData) {

            List<HashMap<String, String>> places = new ArrayList<>();
            Place_JSON placeJson = new Place_JSON();

            try {
                jObject = new JSONObject(jsonData[0]);

                places = placeJson.parse(jObject);

            } catch (Exception e) {
                Log.d("Exception", e.toString());
            }
            return places;
        }

        // Executed after the complete execution of doInBackground() method
        @Override
        protected void onPostExecute(List<HashMap<String, String>> list) {

            Log.d("Map", "list size: " + list.size());
            FetchListData();
        }
    }

    private class Place_JSON {

        private List<HashMap<String, String>> parse(JSONObject jObject) {
            JSONArray jPlaces = null;


            try {
                jPlaces = jObject.getJSONArray("predictions");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return getPlaces(jPlaces);
        }

        private List<HashMap<String, String>> getPlaces(JSONArray jPlaces) {
            int placesCount = jPlaces.length();
            List<HashMap<String, String>> placesList = new ArrayList<>();
            HashMap<String, String> place;
            mGoogleAutoCompleteArray.clear();

            for (int i = 0; i < placesCount; i++) {
                try {
                    place = getPlace((JSONObject) jPlaces.get(i));
                    placesList.add(place);

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            //mGooglePlacesArray=mGoogleAutoCompleteArray;
            return placesList;
        }

        private HashMap<String, String> getPlace(JSONObject jPlace) {

            HashMap<String, String> place = new HashMap<>();
            String placeName = null;
            String description = null;
            String id = null;
            String place_id = null;
            String reference = null;

            try {

                String structured_formatting = jPlace.getString("structured_formatting");
                Log.d("Place 1:", structured_formatting);
                JSONObject main_text = new JSONObject(structured_formatting);

                if (!main_text.isNull("main_text")) {
                    placeName = main_text.getString("main_text");
                }

                if (!jPlace.isNull("description")) {
                    description = jPlace.getString("description");
                }

                if (!jPlace.isNull("place_id")) {
                    place_id = jPlace.getString("place_id");
                }

                if (!jPlace.isNull("id")) {
                    id = jPlace.getString("id");
                }

                if (!jPlace.isNull("reference")) {
                    reference = jPlace.getString("reference");
                }

                place.put("main_text", placeName);
                place.put("description", description);
                place.put("id", id);
                place.put("place_id", place_id);
                place.put("reference", reference);

                mGoogleAutoCompleteArray.add(new GooglePlace(placeName, description, place_id, reference));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return place;
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        menu.clear();

    }

}