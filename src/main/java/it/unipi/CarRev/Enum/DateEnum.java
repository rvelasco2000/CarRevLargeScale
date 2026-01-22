package it.unipi.CarRev.Enum;

public enum DateEnum {
    DAILY("%Y-%m-%d"),
    WEEKLY("%Y-%U"),
    MONTHLY("%Y-%m"),
    YEARLY("%Y");
    public final String format;
    DateEnum(String format){
        this.format=format;
    }
}
