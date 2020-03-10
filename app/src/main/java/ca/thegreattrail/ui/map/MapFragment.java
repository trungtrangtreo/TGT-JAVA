package ca.thegreattrail.ui.map;

import android.database.Cursor;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.JointType;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import ca.thegreattrail.R;
import ca.thegreattrail.data.local.db.ActivityDBHelperTrail;
import ca.thegreattrail.data.model.db.TrailSegment;
import ca.thegreattrail.data.model.db.TrailSegmentLight;
import ca.thegreattrail.ui.base.BaseFragment;
import ca.thegreattrail.ui.main.MainActivity;
import ca.thegreattrail.utlis.Constants;
import ca.thegreattrail.utlis.TrailUtility;

public class MapFragment extends BaseFragment implements OnMapReadyCallback {


    private GoogleMap googleMap;
    private Polyline lastSelectedPolyline;
    private HashMap<Integer, Polyline> drawnPolylinesMap=new HashMap<>();

    private int land = -0xc75800; //  56-168-0
    private int water = -0xff6224;//    0, 157, 220
    private int gap = -0x1692e1;   //   233 , 109, 31
    private int PATTERN_DASH_LENGTH_PX = 40;
    private int PATTERN_GAP_LENGTH_PX = 20;
    private int TRAIL_LOW_RESOLUTION_DROPPED_POINTS_COUNT = 50;

    private final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DASH);

    protected int unSelectedPolylineWidth = 5;
    protected int selectedPolylineWidth = 5;
    public int selectedSegmentId = 0;
    public int lastSelectedSegmentId = 0;
    public int lastObjectIdMeasureTool = 0;

    private TrailSegment selectedTrail;

    private TextView tvSuggest;
    private TextView tvTrailName;
    private TextView tvDistance;

    @Override
    public int getLayoutId() {
        return R.layout.fragment_map;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvSuggest = mRootView.findViewById(R.id.tvSuggest);
        tvTrailName = mRootView.findViewById(R.id.tvTrailName);
        tvDistance = mRootView.findViewById(R.id.tvDistance);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        SupportMapFragment mapFragment= (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        this.googleMap = googleMap;

        googleMap.setMyLocationEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add a marker in Sydney and move the camera
        LatLng canada = new LatLng(55.508930, -96.654281);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(canada));

        googleMap.setOnPolylineClickListener(polyLineClickListner());

        drawGreatTrail();

    }

    private void drawGreatTrail() {

        if (MainActivity.listSegments != null) {

            for (TrailSegmentLight segment : MainActivity.listSegments) {
                drawTrailSegment(segment);
            }
        }
    }

    private void drawTrailSegment(TrailSegmentLight segment) {

//        if (segment == null)
//            return

        List<LatLng> points = MainActivity.listPoints.get(segment.objectId);

        List<PatternItem> patternItems = null;

        int segmentColor = findSegmentColor(segment);

        if (segment.categoryCode == 5) {
            patternItems = null;
            patternItems = PATTERN_POLYLINE_DOTTED;
        } else {
            points = TrailUtility.compressedSegment(points, TRAIL_LOW_RESOLUTION_DROPPED_POINTS_COUNT);
        }

        Polyline polyline = googleMap.addPolyline(new PolylineOptions().addAll(points));
        polyline.setTag(segment.objectId);
        stylePolyline(polyline, segmentColor, patternItems);
        drawnPolylinesMap.put(segment.objectId, polyline);
    }

    private int findSegmentColor(TrailSegmentLight segment) {
        int color = Constants.land;
        int statusCode = segment.statusCode;
        int categoryCode = segment.categoryCode;

        if (statusCode == 1) {
            if (categoryCode == 2) {
                color = Constants.water; // water
            } else {
                color = Constants.land; // land
            }
        } else if (statusCode == 2) {
            color = Constants.gap;   // gap
        }

        if (categoryCode == 5) {
            color = Constants.water; // water
        }

        return color;
    }

    private void stylePolyline(Polyline polyline, int color, List<PatternItem> patternItems) {
        // Get the data object stored with the polyline.
        // Use a round cap at the start of the line.

        polyline.setStartCap(new RoundCap());

        polyline.setEndCap(new RoundCap());
        polyline.setWidth(unSelectedPolylineWidth);
        polyline.setColor(color);
        polyline.setJointType(JointType.ROUND);
        polyline.setPattern(patternItems);
    }

    private GoogleMap.OnPolylineClickListener polyLineClickListner() {
        return new GoogleMap.OnPolylineClickListener() {
            @Override
            public void onPolylineClick(Polyline polyline) {
                if (lastSelectedPolyline != null)
                    lastSelectedPolyline.setWidth(unSelectedPolylineWidth);

                lastSelectedPolyline = polyline;
                polyline.setWidth(selectedPolylineWidth);


                List<LatLng> points = polyline.getPoints();
                selectedSegmentId = findSegmentIdWithPoints(points);

                lastObjectIdMeasureTool = lastSelectedSegmentId;
                lastSelectedSegmentId = selectedSegmentId;

                ActivityDBHelperTrail db = ActivityDBHelperTrail.getInstance(getContext());
                Cursor cursor = db.getSpecificSegments(selectedSegmentId);
                if (cursor != null && cursor.moveToFirst()) {
                    TrailSegment segment = new TrailSegment(cursor);
                    selectedTrail = segment;

                    showSegmentInfo(segment);
                }
                cursor.close();
            }
        };

    }

    protected void showSegmentInfo(TrailSegment segment) {
        tvSuggest.setText("");
        tvTrailName.setText(segment.getTrailName());
        tvDistance.setText(segment.getSumLengthKm()+" km");
    }

    private int findSegmentIdWithPoints(List<LatLng> points) {

        if (points == null || points.size() < 2)
            return -1;

        LatLng firstPoint = points.get(0);
        LatLng lastPoint = points.get(points.size() - 1);

        ArrayList<LatLng> segmentPoints;
        for (int id : MainActivity.listPoints.keySet()) {
            segmentPoints = MainActivity.listPoints.get(id);
            if (segmentPoints.get(0).equals(firstPoint) &&
                    segmentPoints.get(segmentPoints.size() - 1).equals(lastPoint))
                return id;
        }

        return -1;                      // Not found
    }
}
