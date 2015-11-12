package com.py.player.util;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StringTools {

	public static String generateTime(int time) {
		int totalSeconds = (int) (time / 1000);
		int seconds = totalSeconds % 60;
		int minutes = (totalSeconds / 60) % 60;
		int hours = totalSeconds / 3600;
		Locale locale = Locale.getDefault();
		return hours > 0 ? String.format(locale, "%02d:%02d:%02d", hours,
				minutes, seconds, locale) : String.format(locale, "%02d:%02d",
				minutes, seconds);
	}
	
	public final static String TAG = StringTools.class.getSimpleName();

    public static String stripTrailingSlash(String s) {
        if( s.endsWith("/") && s.length() > 1 )
            return s.substring(0, s.length() - 1);
        return s;
    }

    public static boolean StartsWith(String[] array, String text) {
        for (String item : array)
            if (text.startsWith(item))
                return true;
        return false;
    }


    public static String millisToString(long millis)
    {
        return StringTools.millisToString(millis, false);
    }

    
    public static String millisToText(long millis)
    {
        return StringTools.millisToString(millis, true);
    }

    static String millisToString(long millis, boolean text) {
        boolean negative = millis < 0;
        millis = java.lang.Math.abs(millis);

        millis /= 1000;
        int sec = (int) (millis % 60);
        millis /= 60;
        int min = (int) (millis % 60);
        millis /= 60;
        int hours = (int) millis;

        String time;
        DecimalFormat format = (DecimalFormat)NumberFormat.getInstance(Locale.US);
        format.applyPattern("00");
        if (text) {
            if (millis > 0)
                time = (negative ? "-" : "") + hours + "h" + format.format(min) + "min";
            else if (min > 0)
                time = (negative ? "-" : "") + min + "min";
            else
                time = (negative ? "-" : "") + sec + "s";
        }
        else {
            if (millis > 0)
                time = (negative ? "-" : "") + hours + ":" + format.format(min) + ":" + format.format(sec);
            else
                time = (negative ? "-" : "") + min + ":" + format.format(sec);
        }
        return time;
    }


    public static boolean nullEquals(String s1, String s2) {
        return (s1 == null ? s2 == null : s1.equals(s2));
    }

 
    public static String formatRateString(float rate) {
        return String.format(java.util.Locale.US, "%.2fx", rate);
    }
	
}
