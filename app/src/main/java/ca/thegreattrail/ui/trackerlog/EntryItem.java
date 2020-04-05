package ca.thegreattrail.ui.trackerlog;


import ca.thegreattrail.data.model.other.Item;

public class EntryItem implements Item {

	public final String title;
	public final String subtitle;
    public final long _id;
	public final int month;
    public final int region;


	public EntryItem(String title, String subtitle, long _id, int month, int region) {
		this.title = title;
		this.subtitle = subtitle;
		this._id = _id;
        this.month = month;
        this.region = region;

	}
	
	@Override
	public boolean isSection() {
		return false;
	}

	public String getTitle() { return title; }

    public long getId() { return _id; }

    public String tostring() { return _id+""; }


}
