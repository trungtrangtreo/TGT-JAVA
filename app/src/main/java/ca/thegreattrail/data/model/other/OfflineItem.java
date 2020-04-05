package ca.thegreattrail.data.model.other;

public class OfflineItem {
    private String name = "";
    private String date = "";
    private boolean status = false;

    public OfflineItem(String name, String date, boolean status){
        this.name = name;
        this.date = date;
        this.status = status;

    }



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }


}
