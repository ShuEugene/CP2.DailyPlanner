package utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static java.time.temporal.ChronoUnit.*;

public class Period {

    public final static Period NULL = new Period(0, null);
    public final static Period MINUTE = new Period(1, MINUTES);
    public final static Period HOUR = new Period(1, HOURS);
    public final static Period DAY = new Period(1, DAYS);
    public final static Period WEEK = new Period(1, WEEKS);
    public final static Period MONTH = new Period(1, MONTHS);
    public final static Period YEAR = new Period(1, YEARS);

    private final ChronoUnit type;
    private final long duration;


//  ?использованы
    public Period(ChronoUnit type) {
        this(0, type);
    }

    public Period(long duration) {
        this(duration, null);
    }

    public Period(long duration, ChronoUnit type) {
        this.type = type;
        this.duration = Math.abs(duration);
    }

    public Period(LocalDateTime initialTime, LocalDateTime endTime) {
        if (initialTime != null && endTime != null) {
            this.type = MINUTES;
            this.duration = Math.abs(endTime.toEpochSecond(ZoneOffset.UTC) - initialTime.toEpochSecond(ZoneOffset.UTC)) / 60;
        } else {
            this.type = null;
            this.duration = 0;
        }
    }


    public ChronoUnit getType() {
        return type;
    }

    public long getDuration() {
        return duration;
    }
}
