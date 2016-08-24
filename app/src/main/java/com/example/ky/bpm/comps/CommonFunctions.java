package com.example.ky.bpm.comps;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by RockStar0116 on 2016.08.18.
 */
public class CommonFunctions {
    public static int DATA_VIEW_DAY = 0;
    public static int DATA_VIEW_WEEK = 1;
    public static int DATA_VIEW_MONTH = 2;
    public static int DATA_VIEW_YEAR = 3;

    public static String getStringFromDateTime(Date nowdate){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String sdt = df.format(new Date(System.currentTimeMillis()));
        return sdt;
    }
    public static String convertStringForHeaderItemMonth(String dateTime){
        String ret_date = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateTime);
            DateFormat df = new SimpleDateFormat("MMM yyyy", Locale.US);
            ret_date = df.format(convertedDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret_date;
    }
    public static String convertStringFromMonthDate(String month_date){
        String ret_date = "";

        SimpleDateFormat dateFormat = new SimpleDateFormat("MMM yyyy");
        Date convertedDate = new Date();

        try {
            convertedDate = dateFormat.parse(month_date);
            SimpleDateFormat dateFormat1 = new SimpleDateFormat("yyyy-MM");

            ret_date = dateFormat1.format(convertedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return ret_date;
    }

    public static String convertStringForHeaderItemWeek(String dateTime){
        String ret_date = "";
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        Date convertedDate = new Date();
        try {
            convertedDate = dateFormat.parse(dateTime);
            DateFormat df = new SimpleDateFormat("yyyy", Locale.US);
            ret_date = df.format(convertedDate);
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ret_date;
    }
}
