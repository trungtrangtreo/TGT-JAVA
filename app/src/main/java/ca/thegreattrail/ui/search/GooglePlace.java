package ca.thegreattrail.ui.search;

import com.google.android.gms.maps.model.LatLng;

public class GooglePlace {
	private String name;
	private String category;
	private String rating;
	private String open;
	private LatLng location;
	private String contactInfo;
	private String address;

	//
	String placeName ;
	String description;
	String place_id;
	String reference ;

	//


	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPlace_id() {
		return place_id;
	}

	public void setPlace_id(String place_id) {
		this.place_id = place_id;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public GooglePlace(String placeName, String description, String place_id, String reference) {
		this.placeName = placeName;
		this.description = description;
		this.place_id = place_id;
		this.reference = reference;
	}

	public GooglePlace() {
		this.name = "";
		this.rating = "";
		this.open = "";
		this.setCategory("");
	}

	public GooglePlace(String name, String category, LatLng location, String contactInfo, String address) {
		this.name = name;
		this.category = category;
		this.location = location;
		this.contactInfo = contactInfo;
		this.address = address;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public String getRating() {
		return rating;
	}

	public void setOpenNow(String open) {
		this.open = open;
	}

	public String getOpenNow() {
		return open;
	}

	public String getOpen() {
		return open;
	}

	public void setOpen(String open) {
		this.open = open;
	}

	public LatLng getLocation() {
		return location;
	}

	public void setLocation(LatLng location) {
		this.location = location;
	}

	public String getContactInfo() {
		return contactInfo;
	}

	public void setContactInfo(String contactInfo) {
		this.contactInfo = contactInfo;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
