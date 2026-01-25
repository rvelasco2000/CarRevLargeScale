package it.unipi.CarRev.Enum;

public enum DateEnum {
    DAILY("%Y-%m-%d"),
    WEEKLY("%G-%V"),
    MONTHLY("%Y-%m"),
    YEARLY("%Y");
    public final String format;
    DateEnum(String format){
        this.format=format;
    }
}
