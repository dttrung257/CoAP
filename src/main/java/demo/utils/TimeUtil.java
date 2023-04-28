package demo.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeUtil {
    private static final String PATTERN = "dd-MM-yyyy hh:mm:ss";
    public String format(Date date) {
        SimpleDateFormat formatter = new SimpleDateFormat(PATTERN);
        return formatter.format(date);
    }
}
