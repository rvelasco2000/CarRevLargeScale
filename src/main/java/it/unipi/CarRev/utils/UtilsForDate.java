package it.unipi.CarRev.utils;

import org.springframework.security.config.annotation.authentication.configuration.GlobalAuthenticationConfigurerAdapter;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class UtilsForDate {
    private static final DateTimeFormatter FORMAT=DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static String getDate(){
        LocalDateTime date=LocalDateTime.now();
        return date.format(FORMAT);
    }
    public static String getYesterdayDate(){
        LocalDateTime date=LocalDateTime.now().minusDays(1);
        return date.format(FORMAT);
    }

}
