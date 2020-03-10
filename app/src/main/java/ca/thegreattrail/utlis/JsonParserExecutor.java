package ca.thegreattrail.utlis;

import androidx.annotation.NonNull;
import com.bluelinelabs.logansquare.LoganSquare;
import com.google.android.gms.maps.model.LatLng;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import ca.thegreattrail.data.model.db.ParsableLatLngList;

/**
 * Created by Islam Salah on 8/23/17.
 */

/**
 * This class do Json parsing of latlng points sent by the backend using multiple threads.
 * Once a thread is done with it's job the resulting points are inserted into {@link #pointsList}
 *
 * @see ParsingTask ParsingTask
 * @see ParsingFutureTask ParsingFutureTask
 *
 * @author Islam Salah
 * @version 2
 * @since 08/23/2017
 */
public class JsonParserExecutor {
    private static JsonParserExecutor sInstance;
    private static final int MAX_THREAD_COUNT = 10;

    private ExecutorService executor;
    private HashMap<Integer, ArrayList<LatLng>> pointsList;          // To be filled by each thread once the task is done

    private JsonParserExecutor(HashMap<Integer, ArrayList<LatLng>> pointsList) {
        executor = Executors.newFixedThreadPool(MAX_THREAD_COUNT);
        this.pointsList = pointsList;
    }

    public static synchronized JsonParserExecutor getInstance(HashMap<Integer, ArrayList<LatLng>> pointsList) {
        if (sInstance == null)
            sInstance = new JsonParserExecutor(pointsList);

        return sInstance;
    }

    public void executeTaskWithId(int taskId, String jsonString) {
        ParsingTask task = new ParsingTask(jsonString);
        ParsingFutureTask futureTask = new ParsingFutureTask(taskId, task);
        executor.execute(futureTask);
    }

    public void shutdown() {
        executor.shutdown();
        sInstance = null;
    }

    public void awaitsTermination() {
        try {
            executor.awaitTermination(Integer.MAX_VALUE, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class ParsingTask implements Callable<List<LatLng>> {
        private String jsonString;

        public ParsingTask(String jsonString) {
            this.jsonString = jsonString;
        }

        @Override
        public List<LatLng> call() throws Exception {
            return decodeJSON(jsonString);
        }

        private List<LatLng> decodeJSON(String jsonValue) {
            List<LatLng> points = new ArrayList<>();

            try {
                String jsonObjectList = ParsableLatLngList.constructJsonObjectList(jsonValue);
                List<ParsableLatLngList> objectsList = LoganSquare.parseList(jsonObjectList, ParsableLatLngList.class);
                ParsableLatLngList latLngList = objectsList.get(0);

                for (Double[] latlngPoint : latLngList.points) {
                    points.add(new LatLng(latlngPoint[1], latlngPoint[0]));                                 // The backend sends [Longitude, Latitude]
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            return points;
        }
    }

    private class ParsingFutureTask extends FutureTask<List<LatLng>> {
        private int taskId;

        public ParsingFutureTask(int taskId, @NonNull Callable<List<LatLng>> callable) {
            super(callable);
            this.taskId = taskId;
        }

        @Override
        protected void done() {
            List<LatLng> parsedPoints = new ArrayList<>();

            try {
                parsedPoints = get();             // blocking call awaits completion or aborts on interrupt or timeout
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }

            pointsList.put(taskId, (ArrayList<LatLng>) parsedPoints);
        }
    }
}
