package com.uet.CoAPapi.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class TimeUtil {
    public static final String PATTERN = "dd-MM-yyyy HH:mm:ss";

    public static String format(long timestamp) {
        final Date date = new Date(timestamp);
        final SimpleDateFormat formatter = new SimpleDateFormat(PATTERN);
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+7"));
        return formatter.format(date);
    }
}

