package ca.thegreattrail.ui.search;

/**
 * Created by hardikfumakiya on 2016-12-24.
 */
public class ListViewItem {
    int type;
    String object;
    int no_of_item;
    String parentSection;
    String trailid;



    public ListViewItem(int type, String object, String parent, String trailid) {
        this.type = type;
        this.object = object;
        this.parentSection=parent;
        this.trailid=trailid;
    }
    public ListViewItem(int type, String object, String parent) {
        this.type = type;
        this.object = object;
        this.parentSection=parent;
    }

    public ListViewItem(int type, String object, int no_of_item) {
        this.type = type;
        this.object = object;
        this.no_of_item = no_of_item;
    }

    public String getTrailid() {
        return trailid;
    }

    public void setTrailid(String trailid) {
        this.trailid = trailid;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getObject() {
        return object;
    }

    public void setObject(String object) {
        this.object = object;
    }

    public int getNo_of_item() {
        return no_of_item;
    }

    public void setNo_of_item(int no_of_item) {
        this.no_of_item = no_of_item;
    }

    public String getParentSection() {
        return parentSection;
    }

    public void setParentSection(String parentSection) {
        this.parentSection = parentSection;
    }
}
