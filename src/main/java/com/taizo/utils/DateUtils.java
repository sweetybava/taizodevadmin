package com.taizo.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Date utils contain the helper function for date.
 *
 */
public class DateUtils {

	public static Date today() {		
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	public static Date dataAdd(Date date, int days) {		
		Calendar cal = Calendar.getInstance();
		cal.setTimeZone(TimeZone.getTimeZone("UTC"));
		cal.setTime(date);		
		cal.add(Calendar.DATE, days);
		return cal.getTime();
	}	
}
