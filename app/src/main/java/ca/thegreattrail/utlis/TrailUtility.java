package ca.thegreattrail.utlis;

import android.content.Context;
import android.location.Location;
import com.google.android.gms.maps.model.LatLng;
import java.util.ArrayList;
import java.util.List;

import ca.thegreattrail.data.local.db.ActivityDBHelperTrail;
import ca.thegreattrail.data.model.db.TrailSegmentLight;
import ca.thegreattrail.ui.main.MainActivity;

/**
 * Created by Islam Salah on 8/13/17.
 */

public class TrailUtility {

    /**
     * Calculate distance between two points
     *
     * @return distance in meters
     */
    public static float distanceTo(LatLng start, LatLng end) {
        float[] distance = new float[1];
        Location.distanceBetween(start.latitude, start.longitude, end.latitude,
                end.longitude, distance);

        return distance[0];
    }

    public static String currentProvince(Context context, LatLng position) {
        TrailSegmentLight segment = closestSegment(position);

        if (segment == null)
            return null;
        return ActivityDBHelperTrail.findProvinceBySegment(context, segment);
    }

    public static List<TrailSegmentLight> nearbySegments(LatLng position) {
        List<TrailSegmentLight> nearbyTrails = new ArrayList();

        // TODO: to be handled and ensure that it is not equal null
        if (MainActivity.listSegments == null)
            return nearbyTrails;

        for (TrailSegmentLight segment : MainActivity.listSegments) {

            if (isNearbySegmentFromPoint(segment, position)) {
                nearbyTrails.add(segment);
            }
        }
        return nearbyTrails;
    }

    public static List<LatLng> compressedSegment(List<LatLng> points, int droppedPointsCount) {
        List<LatLng> compressedPoints = new ArrayList();
        if (points == null || points.isEmpty()) return compressedPoints;

        compressedPoints.add(points.get(0));                    // first point
        for (int i = 1; i < points.size() - 1; i += droppedPointsCount) {
            compressedPoints.add(points.get(i));
        }
        compressedPoints.add(points.get(points.size() - 1));      // last point

        return compressedPoints;
    }

    public static double distanceFromTrail(LatLng position) {
        double distanceFromTrail = Double.MAX_VALUE;
        List<TrailSegmentLight> nearbySegments = nearbySegments(position);

        for (TrailSegmentLight segment : nearbySegments) {
            distanceFromTrail = Math.min(distanceFromTrail, distanceFromSegment(segment, position));
        }
        return distanceFromTrail;
    }

    private static boolean isPointNonTangentiallyCloseToLine(LatLng position, LatLng lineStartingPoint, LatLng lineEndingPoint) {
        double minLat = Math.min(lineStartingPoint.latitude, lineEndingPoint.latitude);
        double maxLat = Math.max(lineStartingPoint.latitude, lineEndingPoint.latitude);

        double minLng = Math.min(lineStartingPoint.longitude, lineEndingPoint.longitude);
        double maxLng = Math.max(lineStartingPoint.longitude, lineEndingPoint.longitude);

        return (position.latitude > minLat && position.latitude < maxLat) ||
                (position.longitude > minLng && position.longitude < maxLng);
    }

    private static TrailSegmentLight closestSegment(LatLng position) {
        TrailSegmentLight closestSegment = null;
        double minDistanceFromTrail = Double.MAX_VALUE;
        List<TrailSegmentLight> nearbySegments = nearbySegments(position);

        for (TrailSegmentLight segment : nearbySegments) {
            double distanceFromSegment = distanceFromSegment(segment, position);
            if (distanceFromSegment < minDistanceFromTrail) {
                minDistanceFromTrail = distanceFromSegment;
                closestSegment = segment;
            }
        }
        return closestSegment;
    }

    // This method is called on the whole points list rather than compressed ones to ensure accurate results
    private static double distanceFromSegment(TrailSegmentLight segment, LatLng position) {
        List<LatLng> points = MainActivity.listPoints.get(segment.objectId);

        double minDistance = Double.MAX_VALUE;
        for (int i = 0; i < points.size() - 1; i++) {

            if (points.get(i).latitude == points.get(i + 1).latitude && points.get(i).longitude == points.get(i + 1).longitude) {
                // To avoid Nan return from min distance as area and base length will be equal to zero
                continue;
            }
            double perpendicularDistance = getMinDistancefromPointToLine(position, points.get(i), points.get(i + 1));

            if (Double.isNaN(perpendicularDistance)) {
                continue;
            }

            if (perpendicularDistance < minDistance && isPointNonTangentiallyCloseToLine(position, points.get(i), points.get(i + 1))) {
                minDistance = perpendicularDistance;
            }
        }
        return minDistance;
    }

    private static boolean isNearbySegmentFromPoint(TrailSegmentLight segment, LatLng position) {
        if(segment == null) return false;
        
        ArrayList<LatLng> points = MainActivity.listPoints.get(segment.objectId);
        if (points == null || points.size() < 2) return false;

        LatLng trailStartPoint = points.get(0);
        LatLng trailEndPoint = points.get(points.size() - 1);

        double segmentLatDiff = Math.abs(trailStartPoint.latitude - trailEndPoint.latitude);
        double segmentLngDiff = Math.abs(trailStartPoint.longitude - trailEndPoint.longitude);

        double latDiff1 = Math.abs(trailStartPoint.latitude - position.latitude);
        double latDiff2 = Math.abs(trailEndPoint.latitude - position.latitude);
        double latDiff = Math.max(latDiff1, latDiff2);

        double lngDiff1 = Math.abs(trailStartPoint.longitude - position.longitude);
        double lngDiff2 = Math.abs(trailEndPoint.longitude - position.longitude);
        double lngDiff = Math.max(lngDiff1, lngDiff2);

        if (latDiff < segmentLatDiff || lngDiff < segmentLngDiff)
            return true;

        return false;
    }

    private static double getMinDistancefromPointToLine(LatLng point, LatLng lineStartPoint, LatLng lineEndPoint) {
        ArrayList<Double> lats = new ArrayList();
        lats.add(point.latitude);
        lats.add(lineEndPoint.latitude);
        lats.add(lineStartPoint.latitude);

        ArrayList<Double> lngs = new ArrayList();
        lngs.add(point.longitude);
        lngs.add(lineEndPoint.longitude);
        lngs.add(lineStartPoint.longitude);

        double triangleBaseLength = distanceTo(lineStartPoint, lineEndPoint);
        double triangleArea = sphericalPolygonArea(lats, lngs);

        return 2 * triangleArea / triangleBaseLength;        // height = 2 * area / base
    }

    /**
     * Implementation of Matlab function called areaint which
     * calculates the spherical surface area of the polygon specified by the input vectors lats and lngs
     *
     * @param lats
     * @param lngs
     * @return area in meter squared
     */
    private static double sphericalPolygonArea(ArrayList<Double> lats, ArrayList<Double> lngs) {
        double sum = 0;
        double prevcolat = 0;
        double prevaz = 0;
        double colat0 = 0;
        double az0 = 0;
        for (int i = 0; i < lats.size(); i++) {
            double colat = 2 * Math.atan2(Math.sqrt(Math.pow(Math.sin(lats.get(i) * Math.PI / 180 / 2), 2) + Math.cos(lats.get(i) * Math.PI / 180) * Math.pow(Math.sin(lngs.get(i) * Math.PI / 180 / 2), 2)), Math.sqrt(1 - Math.pow(Math.sin(lats.get(i) * Math.PI / 180 / 2), 2) - Math.cos(lats.get(i) * Math.PI / 180) * Math.pow(Math.sin(lngs.get(i) * Math.PI / 180 / 2), 2)));
            double az = 0;
            if (lats.get(i) >= 90) {
                az = 0;
            } else if (lats.get(i) <= -90) {
                az = Math.PI;
            } else {
                az = Math.atan2(Math.cos(lats.get(i) * Math.PI / 180) * Math.sin(lngs.get(i) * Math.PI / 180), Math.sin(lats.get(i) * Math.PI / 180)) % (2 * Math.PI);
            }
            if (i == 0) {
                colat0 = colat;
                az0 = az;
            }
            if (i > 0 && i < lats.size()) {
                sum = sum + (1 - Math.cos(prevcolat + (colat - prevcolat) / 2)) * Math.PI * ((Math.abs(az - prevaz) / Math.PI) - 2 * Math.ceil(((Math.abs(az - prevaz) / Math.PI) - 1) / 2)) * Math.signum(az - prevaz);
            }
            prevcolat = colat;
            prevaz = az;
        }
        sum = sum + (1 - Math.cos(prevcolat + (colat0 - prevcolat) / 2)) * (az0 - prevaz);

        return 5.10072E14 * Math.min(Math.abs(sum) / 4 / Math.PI, 1 - Math.abs(sum) / 4 / Math.PI);
    }
}
