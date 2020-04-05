package ca.thegreattrail.ui.tracker;

import java.util.Date;
import ca.thegreattrail.utlis.DateUtils;
import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UserTrackingInfo extends RealmObject {

    public static final String ID_FIELD_NAME = "id";
    public static final int DEFAULT_ID = 1;

    @PrimaryKey
    private int id;

    private float totalCoveredDistance;

    private float totalAchievedElevation;

    private float totalTrackedTime;

    private RealmList<Province> visitedProvinces;

    public UserTrackingInfo() {
        visitedProvinces = new RealmList<Province>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public float getTotalCoveredDistance() {
        return totalCoveredDistance;
    }

    public void setTotalCoveredDistance(float totalCoveredDistance) {
        this.totalCoveredDistance = totalCoveredDistance;
    }

    public float getTotalAchievedElevation() {
        return totalAchievedElevation;
    }

    public void setTotalAchievedElevation(float totalAchievedElevation) {
        this.totalAchievedElevation = totalAchievedElevation;
    }

    public float getTotalTrackedTime() {
        return totalTrackedTime;
    }

    public void setTotalTrackedTime(float totalTrackedTime) {
        this.totalTrackedTime = totalTrackedTime;
    }

    public RealmList<Province> getVisitedProvinces() {
        return visitedProvinces;
    }

    public void setVisitedProvinces(RealmList<Province> visitedProvinces) {
        this.visitedProvinces = visitedProvinces;
    }

    public void addCoveredDistance(float coveredDistance) {
        totalCoveredDistance += coveredDistance;
    }

    public void addAchievedElevation(float achievedElevation) {
        totalAchievedElevation += achievedElevation;
    }

    public void addTrackedTime(float trackedTime) {
        totalTrackedTime += trackedTime;
    }

    public void addVisitedProvince(Province province) {

        if (visitedProvinces == null)
            visitedProvinces = new RealmList<>();
        visitedProvinces.add(province);
    }

    public int getTotalNumberOfTrailVisits() {

        if (visitedProvinces == null)
            return 0;
        int totalVisits = 0;

        for (Province province : visitedProvinces) {
            totalVisits += province.getNumberOfVisits();
        }
        return totalVisits;
    }

    public boolean visitTrailInPeriod(int period) {

        if (visitedProvinces == null || visitedProvinces.size() == 0)
            return false;

        Date minDate = visitedProvinces.get(0).getLastVisitAt();
        Date maxDate = visitedProvinces.get(0).getLastVisitAt();
        for (Province province : visitedProvinces) {

            if (province.getLastVisitAt().before(minDate)) {
                minDate = province.getLastVisitAt();
            } else if (province.getLastVisitAt().after(maxDate)) {
                maxDate = province.getLastVisitAt();
            }
        }
        long daysBetween = DateUtils.daysBetween(minDate, maxDate);
        return daysBetween >= 1 && daysBetween <= period;
    }

    public boolean visitedProvinceMoreThanOne() {

        if (visitedProvinces == null || visitedProvinces.size() == 0)
            return false;

        for (Province province : visitedProvinces) {
            if (province.getNumberOfVisits() > 1)
                return true;
        }

        return false;
    }
}
