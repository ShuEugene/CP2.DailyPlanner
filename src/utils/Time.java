package utils;

import java.time.format.DateTimeFormatter;

public class Time {

    public static final DateTimeFormatter D_MMMM_YYYY_H_MM = DateTimeFormatter.ofPattern("d MMMM yyyy H:mm");
    public static final DateTimeFormatter D_MMM_YYYY_H_MM = DateTimeFormatter.ofPattern("d MMM yyyy H:mm");
    public static final DateTimeFormatter D_MM_YYYY_H_MM = DateTimeFormatter.ofPattern("d.MM.yyyy H:mm");
    public static final DateTimeFormatter D_MMMM_YYYY = DateTimeFormatter.ofPattern("d MMMM yyyy");
    public static final DateTimeFormatter D_MMM_YYYY = DateTimeFormatter.ofPattern("d MMM yyyy");
    public static final DateTimeFormatter DD_MM_YYYY = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter D_MM_YYYY = DateTimeFormatter.ofPattern("d.MM.yyyy");
    public static final DateTimeFormatter HH_MM = DateTimeFormatter.ofPattern("HH:mm");
    public static final DateTimeFormatter H_MM = DateTimeFormatter.ofPattern("H:mm");
}
