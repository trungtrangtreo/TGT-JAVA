package ca.thegreattrail.utlis;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Ayman Mahgoub on 8/8/17.
 */

public class DateUtils {

    /**
     * This method also assumes endDate >= startDate
     **/
    public static long daysBetween(Date startDate, Date endDate) {
        Calendar startDateCalendar = getDateCalendar(startDate);
        Calendar endDateCalendar = getDateCalendar(endDate);
        long daysBetween = 0;

        while (startDateCalendar.before(endDateCalendar)) {
            startDateCalendar.add(Calendar.DAY_OF_MONTH, 1);
            daysBetween++;
        }
        return daysBetween;
    }

    private static Calendar getDateCalendar(Date date){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar;
    }

    public static Date addMilliSecondsToCurrentTime(int milliSeconds) {
        Calendar calendar = Calendar.getInstance();       // get calendar instance
        calendar.add(Calendar.MILLISECOND, milliSeconds);
        return calendar.getTime();
    }

}
