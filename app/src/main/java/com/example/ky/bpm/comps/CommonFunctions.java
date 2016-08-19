package com.example.ky.bpm.comps;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by RockStar0116 on 2016.08.18.
 */
public class CommonFunctions {
    public static String getStringFromDateTime(Date nowdate){
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd  HH:mm");
        String sdt = df.format(new Date(System.currentTimeMillis()));
        return sdt;
    }
}
