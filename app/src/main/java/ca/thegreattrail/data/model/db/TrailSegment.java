package ca.thegreattrail.data.model.db;

import android.database.Cursor;

/**
 * Created by Dev1 on 11/4/2016.
 */

public class TrailSegment {
    int objectId = 0;
    String trailId = "";
    String segmentId = "";
    String sectionId = "";
    String trailName = "";
    String sectionName = "";
    float sumLengthKm = 0.0f ;
    int statusCode = 0;
    float operational = 0.0f ;
    float proposed = 0.0f ;
    float dirtTrail = 0.0f ;
    float gravelTrail = 0.0f ;
    float pavedTrail = 0.0f ;
    float waterTrail = 0.0f ;
    float dirtRoad = 0.0f ;
    float gravelRoad = 0.0f ;
    float pavedRoad = 0.0f ;
    int categoryCode = 0;
    float greenway = 0.0f ;
    float blueway = 0.0f ;
    float roadway = 0.0f ;
    float yellowTrail = 0.0f ;
    String tctMapsUrl = "";
    String groupName1 = "";
    String websiteUrl1 = "";
    String groupName2 = "";
    String websiteUrl2 = "";
    String groupName3 = "";
    String websiteUrl3 = "";
    String groupName4 = "";
    String websiteUrl4 = "";
    float scale = 0.0f ;
    double shapeLength = 0.0 ;
    String status = "";
    String provinceId = "";
    String province = "";
    String description_fr = "";
    String description = "";
    String statusLength = "";
    String trailType = "";
    String trailType_fr = "";
    String trailTypeLength = "";
    String category = "";
    String categoryLength = "";
    String activities = "";
    String activities_fr = "";
    String environment = "";
    String environment_fr = "";
    String notes = "";
    String atv = "";
    String geometry = "" ;


    public TrailSegment(Cursor result) {
        this.objectId = result.getInt(result.getColumnIndex("objectid"));
        this.trailId = result.getString(result.getColumnIndex("trailid"));
        this.segmentId = result.getString(result.getColumnIndex("segmentid"));
        this.trailName = result.getString(result.getColumnIndex("trailname"));
        this.sectionName = result.getString(result.getColumnIndex("sectionname"));
        this.sumLengthKm = result.getFloat(result.getColumnIndex("sumlengthkm"));
        this.provinceId = result.getString(result.getColumnIndex("provinceid"));
        this.statusCode = result.getInt(result.getColumnIndex("statuscode"));
        this.categoryCode =  result.getInt(result.getColumnIndex("categorycode"));
        this.geometry = result.getString(result.getColumnIndex("geometry"));

        this.groupName1 = (result.getString(result.getColumnIndex("groupname1"))!= null ) ? result.getString(result.getColumnIndex("groupname1")) : "";
        this.websiteUrl1 =  (result.getString(result.getColumnIndex("websiteurl1"))!= null ) ? result.getString(result.getColumnIndex("websiteurl1")) : "";
        this.groupName2 =  (result.getString(result.getColumnIndex("groupname2"))!= null ) ? result.getString(result.getColumnIndex("groupname2")) : "";
        this.websiteUrl2 =  (result.getString(result.getColumnIndex("websiteurl2"))!= null ) ? result.getString(result.getColumnIndex("websiteurl2")) : "";
        this.groupName3 =  (result.getString(result.getColumnIndex("groupname3"))!= null ) ? result.getString(result.getColumnIndex("groupname3")) : "";
        this.websiteUrl3 =  (result.getString(result.getColumnIndex("websiteurl3"))!= null ) ? result.getString(result.getColumnIndex("websiteurl3")) : "";
        this.groupName4 =  (result.getString(result.getColumnIndex("groupname4"))!= null ) ? result.getString(result.getColumnIndex("groupname4")) : "";
        this.websiteUrl4 =  (result.getString(result.getColumnIndex("websiteurl4"))!= null ) ? result.getString(result.getColumnIndex("websiteurl4")) : "";
        this.description = (result.getString(result.getColumnIndex("description")) != null) ? result.getString(result.getColumnIndex("description")) : "";
        this.description_fr = result.getString(result.getColumnIndex("description_fr"));
        this.trailType = result.getString(result.getColumnIndex("trailtype"));
        this.trailType_fr = result.getString(result.getColumnIndex("trailtype_fr"));
        this.activities = result.getString(result.getColumnIndex("activities"));
        this.activities_fr = result.getString(result.getColumnIndex("activities_fr"));
        this.environment = result.getString(result.getColumnIndex("environment"));
        this.environment_fr = result.getString(result.getColumnIndex("environment_fr"));


    }

    public TrailSegment() {

    }

    public int getObjectId() {
        return objectId;
    }

    public void setObjectId(int objectId) {
        this.objectId = objectId;
    }

    static public TrailSegment mapFromDatabase(Cursor result ) {
        TrailSegment segment = new TrailSegment();
        segment.objectId = result.getInt(result.getColumnIndex("objectid"));
        segment.trailId = result.getString(result.getColumnIndex("trailid"));
        segment.segmentId = result.getString(result.getColumnIndex("segmentid"));
        segment.trailName = result.getString(result.getColumnIndex("trailname"));
        segment.sectionName = result.getString(result.getColumnIndex("sectionname"));
        segment.sumLengthKm = result.getFloat(result.getColumnIndex("sumlengthkm"));
        segment.provinceId = result.getString(result.getColumnIndex("provinceid"));
        segment.statusCode = result.getInt(result.getColumnIndex("statuscode"));
        segment.categoryCode =  result.getInt(result.getColumnIndex("categorycode"));
        segment.geometry = result.getString(result.getColumnIndex("geometry"));


        segment.trailType = result.getString(result.getColumnIndex("trailtype"));
        segment.activities = result.getString(result.getColumnIndex("activities"));
        segment.environment = result.getString(result.getColumnIndex("environment"));



        return segment;
    }



    // *****************************************   Getter ********************************************************

    public String getTrailId() {
        return trailId;
    }

    public String getSegmentId() {
        return segmentId;
    }

    public String getSectionId() {
        return sectionId;
    }

    public String getTrailName() {
        return trailName;
    }

    public String getSectionName() {
        return sectionName;
    }

    public float getSumLengthKm() {
        return sumLengthKm;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public float getOperational() {
        return operational;
    }

    public float getProposed() {
        return proposed;
    }

    public float getDirtTrail() {
        return dirtTrail;
    }

    public float getGravelTrail() {
        return gravelTrail;
    }

    public float getPavedTrail() {
        return pavedTrail;
    }

    public float getWaterTrail() {
        return waterTrail;
    }

    public float getDirtRoad() {
        return dirtRoad;
    }

    public float getGravelRoad() {
        return gravelRoad;
    }

    public float getPavedRoad() {
        return pavedRoad;
    }

    public int getCategoryCode() {
        return categoryCode;
    }

    public float getGreenway() {
        return greenway;
    }

    public float getBlueway() {
        return blueway;
    }

    public float getRoadway() {
        return roadway;
    }

    public float getYellowTrail() {
        return yellowTrail;
    }

    public String getTctMapsUrl() {
        return tctMapsUrl;
    }

    public String getGroupName1() {
        return groupName1;
    }

    public String getWebsiteUrl1() {
        return websiteUrl1;
    }

    public String getGroupName2() {
        return groupName2;
    }

    public String getWebsiteUrl2() {
        return websiteUrl2;
    }

    public String getGroupName3() {
        return groupName3;
    }

    public String getWebsiteUrl3() {
        return websiteUrl3;
    }

    public String getGroupName4() {
        return groupName4;
    }

    public String getWebsiteUrl4() {
        return websiteUrl4;
    }

    public float getScale() {
        return scale;
    }

    public double getShapeLength() {
        return shapeLength;
    }

    public String getStatus() {
        return status;
    }

    public String getProvinceId() {
        return provinceId;
    }

    public String getProvince() {
        return province;
    }


    public String getStatusLength() {
        return statusLength;
    }

    public String getTrailType() {
        return trailType;
    }

    public String getTrailTypeLength() {
        return trailTypeLength;
    }

    public String getCategory() {
        return category;
    }

    public String getCategoryLength() {
        return categoryLength;
    }

    public String getActivities() {
        return activities;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getNotes() {
        return notes;
    }

    public String getAtv() {
        return atv;
    }



    // *****************************************   Setter ********************************************************


    public void setTrailId(String trailId) {
        this.trailId = trailId;
    }

    public void setSegmentId(String segmentId) {
        this.segmentId = segmentId;
    }

    public void setSectionId(String sectionId) {
        this.sectionId = sectionId;
    }

    public void setTrailName(String trailName) {
        this.trailName = trailName;
    }

    public void setSectionName(String sectionName) {
        this.sectionName = sectionName;
    }

    public void setSumLengthKm(float sumLengthKm) {
        this.sumLengthKm = sumLengthKm;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public void setOperational(float operational) {
        this.operational = operational;
    }

    public void setProposed(float proposed) {
        this.proposed = proposed;
    }

    public void setDirtTrail(float dirtTrail) {
        this.dirtTrail = dirtTrail;
    }

    public void setGravelTrail(float gravelTrail) {
        this.gravelTrail = gravelTrail;
    }

    public void setPavedTrail(float pavedTrail) {
        this.pavedTrail = pavedTrail;
    }

    public void setWaterTrail(float waterTrail) {
        this.waterTrail = waterTrail;
    }

    public void setDirtRoad(float dirtRoad) {
        this.dirtRoad = dirtRoad;
    }

    public void setGravelRoad(float gravelRoad) {
        this.gravelRoad = gravelRoad;
    }

    public void setPavedRoad(float pavedRoad) {
        this.pavedRoad = pavedRoad;
    }

    public void setCategoryCode(int categoryCode) {
        this.categoryCode = categoryCode;
    }

    public void setGreenway(float greenway) {
        this.greenway = greenway;
    }

    public void setBlueway(float blueway) {
        this.blueway = blueway;
    }

    public void setRoadway(float roadway) {
        this.roadway = roadway;
    }

    public void setYellowTrail(float yellowTrail) {
        this.yellowTrail = yellowTrail;
    }

    public void setTctMapsUrl(String tctMapsUrl) {
        this.tctMapsUrl = tctMapsUrl;
    }

    public void setGroupName1(String groupName1) {
        this.groupName1 = groupName1;
    }

    public void setWebsiteUrl1(String websiteUrl1) {
        this.websiteUrl1 = websiteUrl1;
    }

    public void setGroupName2(String groupName2) {
        this.groupName2 = groupName2;
    }

    public void setWebsiteUrl2(String websiteUrl2) {
        this.websiteUrl2 = websiteUrl2;
    }

    public void setGroupName3(String groupName3) {
        this.groupName3 = groupName3;
    }

    public void setWebsiteUrl3(String websiteUrl3) {
        this.websiteUrl3 = websiteUrl3;
    }

    public void setGroupName4(String groupName4) {
        this.groupName4 = groupName4;
    }

    public void setWebsiteUrl4(String websiteUrl4) {
        this.websiteUrl4 = websiteUrl4;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setShapeLength(double shapeLength) {
        this.shapeLength = shapeLength;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setProvinceId(String provinceId) {
        this.provinceId = provinceId;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getDescription_fr() {
        return description_fr;
    }

    public void setDescription_fr(String description_fr) {
        this.description_fr = description_fr;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTrailType_fr() {
        return trailType_fr;
    }

    public void setTrailType_fr(String trailType_fr) {
        this.trailType_fr = trailType_fr;
    }

    public String getActivities_fr() {
        return activities_fr;
    }

    public void setActivities_fr(String activities_fr) {
        this.activities_fr = activities_fr;
    }

    public String getEnvironment_fr() {
        return environment_fr;
    }

    public void setEnvironment_fr(String environment_fr) {
        this.environment_fr = environment_fr;
    }

    public void setStatusLength(String statusLength) {
        this.statusLength = statusLength;
    }

    public void setTrailType(String trailType) {
        this.trailType = trailType;
    }

    public void setTrailTypeLength(String trailTypeLength) {
        this.trailTypeLength = trailTypeLength;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setCategoryLength(String categoryLength) {
        this.categoryLength = categoryLength;
    }

    public void setActivities(String activities) {
        this.activities = activities;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setAtv(String atv) {
        this.atv = atv;
    }


    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
}
