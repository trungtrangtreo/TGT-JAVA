package ca.thegreattrail.data.model.db;

import android.database.Cursor;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by Dev1 on 11/4/2016.
 */

public class TrailSegmentLightLight implements Serializable {
    public String trailId="";
    public byte statusCode = 0 ;
    public byte categoryCode = 0 ;



    static public TrailSegmentLightLight mapFromDatabase(Cursor result ) {
        TrailSegmentLightLight segment = new TrailSegmentLightLight();
        segment.trailId =  result.getString(result.getColumnIndex("trailid"));
        segment.statusCode = (byte) result.getInt(result.getColumnIndex("statuscode"));
        segment.categoryCode = (byte) result.getInt(result.getColumnIndex("categorycode"));
        return segment;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeObject(this.trailId);
        out.writeByte(this.statusCode);
        out.writeByte(this.categoryCode);

    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        this.trailId = (String) in.readObject();
        this.statusCode = in.readByte();
        this.categoryCode = in.readByte();
    }


}
