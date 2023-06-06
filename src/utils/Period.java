package utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;

import static utils.Period.Type.*;

public class Period {

    public enum Type {YEARS, MONTHS, DAYS, UNITS, HOURS, MINUTES;}

    public final static Period NULL = new Period(0, UNITS);
    public final static Period MINUTE = new Period(1, MINUTES);
    public final static Period HOUR = new Period(1, HOURS);
    public final static Period DAY = new Period(1, DAYS);
    public final static Period WEEK = new Period(7, DAYS);
    public final static Period MONTH = new Period(1, MONTHS);
    public final static Period YEAR = new Period(1, YEARS);

    private final Type type;
    private final long duration;


    public Period() {
        this(0, null);
    }

    public Period(Type type) {
        this(0, type);
    }

    public Period(long duration) {
        this(duration, null);
    }

    public Period(long duration, Type type) {
        this.type = type == null ? UNITS : type;
        this.duration = Math.abs(duration);
    }

    public Period(LocalDateTime initialTime, LocalDateTime endTime) {
        if (initialTime != null && endTime != null) {
            this.type = MINUTES;
            this.duration = Math.abs(endTime.toEpochSecond(ZoneOffset.UTC) - initialTime.toEpochSecond(ZoneOffset.UTC)) / 60;
        } else {
            this.type = UNITS;
            this.duration = 0;
        }
    }


    public Type getType() {
        return type;
    }

    public long getDuration() {
        return duration;
    }
}
