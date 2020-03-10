package ca.thegreattrail.data.model.db;

import com.bluelinelabs.logansquare.annotation.JsonField;
import com.bluelinelabs.logansquare.annotation.JsonObject;

import java.util.List;

/**
 * Created by Islam Salah on 8/21/17.
 */

@JsonObject
public class ParsableLatLngList {
    private static final String FIELD_NAME = "points";

    @JsonField(name = FIELD_NAME)
    public List<Double[]> points;

    public static String constructJsonObjectList(String jsonValue) {
        return "[{" + "\"" + FIELD_NAME + "\":" + jsonValue + "}]";
        /*
            [{
                "points" :  [
                                [1.0,2.0],
                                .
                                .
                                .
                                [3.0,4.0],
                            ]
            }]
         */
    }

}
