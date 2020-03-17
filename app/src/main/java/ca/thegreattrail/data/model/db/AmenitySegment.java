package ca.thegreattrail.data.model.db;

import android.database.Cursor;

import org.json.JSONArray;

/**
 * Created by hardikfumakiya on 2016-12-15.
 */

public class AmenitySegment {

    private int objectid;
    private String trailid;
    private String trailid_segment;
    private int type_amenity;
    private String name_amenity;
    private String description;
    private String description_fr;

    private int trailhead;
    private int picnic_table;
    private int restroom;
    private int water;
    private int camping;
    private int information;

    private String geometry;
    private String lat;
    private String lng;

    public AmenitySegment(Cursor result) {
        this.objectid = result.getInt(result.getColumnIndex("objectid"));
        this.trailid = result.getString(result.getColumnIndex("trailid"));
        this.trailid_segment = result.getString(result.getColumnIndex("trailid_segment"));
        this.type_amenity = result.getInt(result.getColumnIndex("type_amenity"));
        this.name_amenity = result.getString(result.getColumnIndex("name_amenity"));
        this.description = result.getString(result.getColumnIndex("description"));
        this.description_fr = result.getString(result.getColumnIndex("description_fr"));
        this.trailhead = result.getInt(result.getColumnIndex("trailhead"));
        this.picnic_table = result.getInt(result.getColumnIndex("picnic_table"));
        this.restroom =  result.getInt(result.getColumnIndex("restroom"));
        this.water =  result.getInt(result.getColumnIndex("water"));
        this.camping =  result.getInt(result.getColumnIndex("camping"));
        this.information=result.getInt(result.getColumnIndex("information"));;
        this.geometry = result.getString(result.getColumnIndex("geometry"));
        try {
            JSONArray location= new JSONArray(geometry.trim());
            this.lat=location.get(1).toString();
            this.lng=location.get(0).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    public int getObjectid() {
        return objectid;
    }

    public void setObjectid(int objectid) {
        this.objectid = objectid;
    }

    public String getTrailid() {
        return trailid;
    }

    public void setTrailid(String trailid) {
        this.trailid = trailid;
    }

    public String getTrailid_segment() {
        return trailid_segment;
    }

    public void setTrailid_segment(String trailid_segment) {
        this.trailid_segment = trailid_segment;
    }

    public int getType_amenity() {
        return type_amenity;
    }

    public void setType_amenity(int type_amenity) {
        this.type_amenity = type_amenity;
    }

    public String getName_amenity() {
        return name_amenity;
    }

    public void setName_amenity(String name_amenity) {
        this.name_amenity = name_amenity;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription_fr() {
        return description_fr;
    }

    public void setDescription_fr(String description_fr) {
        this.description_fr = description_fr;
    }

    public int getTrailhead() {
        return trailhead;
    }

    public void setTrailhead(int trailhead) {
        this.trailhead = trailhead;
    }

    public int getPicnic_table() {
        return picnic_table;
    }

    public void setPicnic_table(int picnic_table) {
        this.picnic_table = picnic_table;
    }

    public int getRestroom() {
        return restroom;
    }

    public void setRestroom(int restroom) {
        this.restroom = restroom;
    }

    public int getWater() {
        return water;
    }

    public void setWater(int water) {
        this.water = water;
    }

    public int getCamping() {
        return camping;
    }

    public void setCamping(int camping) {
        this.camping = camping;
    }

    public String getGeometry() {
        return geometry;
    }

    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }

    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getLng() {
        return lng;
    }

    public void setLng(String lng) {
        this.lng = lng;
    }
}
