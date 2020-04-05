package ca.thegreattrail.ui.tracker;

import java.util.Calendar;
import java.util.Date;
import ca.thegreattrail.utlis.DateUtils;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Province extends RealmObject {

    public static final String ID_FIELD_NAME = "id";
    public static final String NAME_FIELD_NAME = "name";

    @PrimaryKey
    private int id;

    private String name;

    private Date lastVisitAt;

    private int numberOfVisits;

    private UserTrackingInfo userTrackingInfo;

    public Province() {
    }

    public Province(String name) {
        this.name = name;
    }

    public Province(String name, Date lastVisitAt) {
        this.name = name;
        this.lastVisitAt = lastVisitAt;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Date getLastVisitAt() {
        return lastVisitAt;
    }

    public void setLastVisitAt(Date lastVisitAt) {
        this.lastVisitAt = lastVisitAt;
    }

    public int getNumberOfVisits() {
        return numberOfVisits;
    }

    public void setNumberOfVisits(int numberOfVisits) {
        this.numberOfVisits = numberOfVisits;
    }

    public UserTrackingInfo getUserTrackingInfo() {
        return userTrackingInfo;
    }

    public void setUserTrackingInfo(UserTrackingInfo userTrackingInfo) {
        this.userTrackingInfo = userTrackingInfo;
    }

    //TODO : why isn't that reflected on DB
    public void updateNumberOfVisits() {
        Date currentTime = Calendar.getInstance().getTime();

        if(lastVisitAt == null)
            return;
        long daysBetween = DateUtils.daysBetween(lastVisitAt, currentTime);
        // We only consider a visit as new visit when it is in new date and within one week
        if (daysBetween >= 1 && daysBetween < 8)
            numberOfVisits += 1;
    }
}
