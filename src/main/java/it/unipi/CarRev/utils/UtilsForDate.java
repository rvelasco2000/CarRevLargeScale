package it.unipi.CarRev.utils;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UtilsForDate {

    public static String getDate(){
        LocalDateTime date=LocalDateTime.now();
        DateTimeFormatter format=DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return date.format(format);
    }
}
