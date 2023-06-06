package planner;

import java.time.Duration;
import java.util.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import utils.*;
import utils.Commands.CommandException;
import utils.Text.WordForm;

public class Task {

    public enum RepeatMode {
        OFF("Не повторять"),
        SINGLE("Однократно"),
        DAILY("Ежедневно"),
        WEEKLY("Еженедельно"),
        MONTHLY("Ежемесячно"),
        YEARLY("Ежегодно");

        public static String[] numList() {
            RepeatMode[] modes = RepeatMode.values();
            if (!Data.isCorrect(modes))
                return new String[0];

            String[] numList = new String[modes.length];
            int index = 0;
            for (RepeatMode current :
                    modes) {
                if (current != null)
                    numList[index++] = current.ordinal() + ". " + current.title;
            }

            return numList;
        }

        public static List<String> titles() {
            RepeatMode[] modes = RepeatMode.values();
            if (!Data.isCorrect(modes))
                return new ArrayList<>();

            List<String> titles = new ArrayList<>(Data.notNullObjectsNumber(modes));
            for (RepeatMode current :
                    modes) {
                if (current != null)
                    titles.add(current.title);
            }

            return titles;
        }

        public static RepeatMode get(String string) {
            if (!Data.isCorrect(string))
                return null;

            int ordinal;
            try {
                ordinal = Integer.parseInt(string);
            } catch (Exception e) {
                ordinal = -1;
            }

            for (RepeatMode current :
                    RepeatMode.values())
                if (current != null)
                    if (current.ordinal() == ordinal
                            || string.equalsIgnoreCase(current.title) || string.equalsIgnoreCase(current.name()))
                        return current;

            return null;
        }


        private final String title;


        RepeatMode(String title) {
            this.title = title;
        }


        @Override
        public final String toString() {
            return title;
        }
    }

    public enum Type {
        PERSONAL("личная"),
        BUSINESS("деловая");

        public static Type get(String string) {
            if (!Data.isCorrect(string))
                return null;
            Type[] types = Type.values();
            if (!Data.isCorrect(types))
                return null;

            for (Type type :
                    types) {
                if (type != null && (string.equals(Integer.toString(type.ordinal))
                        || string.equalsIgnoreCase(type.title) || string.equalsIgnoreCase(type.pfTitle)))
                    return type;
            }

            return null;
        }

        public static String[] numList() {
            return numList(null);
        }

        public static String[] numList(WordForm titleForm) {
            Type[] types = Type.values();
            if (!Data.isCorrect(types))
                return new String[0];

            String[] numList = new String[Data.notNullObjectsNumber(types)];
            int index = 0;
            for (Type type :
                    types) {
                if (type != null)
                    numList[index++] = type.ordinal + ". "
                            + (titleForm == WordForm.PLURAL ? type.pfTitle() : type.toString());
            }

            return numList;
        }

        public static List<String> titles() {
            return titles(null);
        }

        public static List<String> titles(Text.WordForm titleForm) {
            if (!Data.isCorrect(Type.values()))
                return new ArrayList<>();

            List<String> titles = new ArrayList<>();
            for (Type type :
                    Type.values()) {
                if (type != null)
                    titles.add(titleForm == WordForm.PLURAL ? type.pfTitle() : type.title);
            }

            return titles;
        }

        private final String title, pfTitle /*pluralFormTitle*/;
        private final int ordinal;

        Type(String title) {
            this.ordinal = ordinal() + 1;
            this.title = Data.isCorrect(title) ? title : "<тип " + ordinal + ">";
            this.pfTitle = title.replace("ая", "ые");
        }

        public final int ord() {
            return ordinal;
        }

        public final String pfTitle() {
            return pfTitle == null ? "" : pfTitle;
        }

        @Override
        public final String toString() {
            return title;
        }
    }

    public enum Status {
        ACTUAL("безсрочная", 2),
        TEMPORARY("временная", 1),
        REPEATED("повторяющаяся", TEMPORARY.ordinal),
        EXPIRED("просрочена", 4),
        PAUSED("приостановлена", 3);

        private final String title;
        private final int ordinal;

        Status(String title, int ordinal) {
            this.title = title;
            this.ordinal = ordinal;
        }

        public String getTitle() {
            return title;
        }

        public int getOrdinal() {
            return ordinal;
        }
    }

    private Type type;
    private String title;
    private final LocalDateTime creationTime = LocalDateTime.now();
    private final int id;
    private int ordinal;
    private LocalDate date;
    private LocalTime time;
    private boolean defEventTimeUsing = false;
    private RepeatMode repeatMode = RepeatMode.OFF;
    private Period period;
    private LocalDateTime endRepeatTime;
    private String description;
    private Status status;

    /*  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  */
//  использованы?
    public Task(String title) throws CommandException {
        this(title, null, null, null, null, null);
    }

    public Task(String title, String description) throws CommandException {
        this(title, null, null, description);
    }

    public Task(String title, LocalDate date) throws CommandException {
        this(title, date, null, null);
    }

    public Task(String title, LocalTime time) throws CommandException {
        this(title, null, time, null);
    }

    public Task(String title, Type type) throws CommandException {
        this(title, type, null, null, null);
    }

    public Task(String title, LocalTime time, String description) throws CommandException {
        this(title, null, time, description);
    }

    public Task(String title, LocalDate date, String description) throws CommandException {
        this(title, date, null, description);
    }

    public Task(String title, LocalDate date, LocalTime time) throws CommandException {
        this(title, date, time, null);
    }

    public Task(String title, Type type, LocalDate date, LocalTime time) throws CommandException {
        this(title, type, date, time, null);
    }

    public Task(String title, LocalDate date, LocalTime time, String description) throws CommandException {
        this(title, null, date, time, description);
    }

    public Task(String title, Type type, LocalDate date, LocalTime time, String description) throws CommandException {
        this(title, type, date, time, null, description);
    }

    public Task(String title, Type type, LocalDate date, LocalTime time, RepeatMode repeatMode, String description) throws CommandException {
        this(title, type, date, time, repeatMode, null, null, description);
    }

    public Task(String title, Type type, LocalDate date, LocalTime time,
                RepeatMode repeatMode, Period period, LocalDateTime endRepeatTime, String description) throws CommandException {
        if (!Data.isCorrect(title))
            throw new CommandException(Commands.CommandException.UNTITLED_TASK_);

        setTitle(title);
        setType(type);
        setDate(date);
        setTime(time);
        setRepeatMode(repeatMode, period, endRepeatTime);
        this.id = hashCode();
        setStatus();
        setDescription(description);
    }
    /*  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  */


    public final boolean alreadyExist() {
        return Journal.getJournal().containsKey(getId());
    }

    public void checkTime() {
        if (date == null)
            return;

        if (defEventTimeUsed())
            System.out.println("\n! Задача " + Text.aquo(getTitle()) + " назначена на " + this.date.format(Time.D_MMM_YYYY)
                    + ";\n\t! время задачи (" + Journal.getEventDayDefTime().format(Time.H_MM) + ") назначено по умолчанию.");

        LocalDateTime dateTime = LocalDateTime.of(date, time);
        if (dateTime.isBefore(LocalDateTime.now()))
            System.out.println("\n! Внимание:\n\tдля задачи " + Text.aquo(getTitle()) + " назначено уже прошедшее время - " + dateTime.format(Time.D_MMM_YYYY_H_MM) + ".");
    }


    public final LocalDateTime getCreationTime() {
        return creationTime;
    }

    public final String getTitle() {
        if (title == null)
            title = "";

        return title;
    }

    public final void setTitle(String title) throws CommandException {
        if (!Data.isCorrect(title))
            throw new CommandException(Commands.CommandException.UNTITLED_TASK_);

        this.title = title;
    }

    public final int getId() {
        return id;
    }

    public final LocalDateTime getDateTime() {
        return (date == null || time == null) ? null : LocalDateTime.of(date, time);
    }

    public final LocalDate getDate() {
        return date;
    }

    public final void setDate(LocalDate date) {
        this.date = date;
        if (date == null && this.time != null)
            this.time = null;
    }

    public final LocalTime getTime() {
        return time;
    }

    public final void setTime(LocalTime time) {
        if (this.date == null && time == null)
            return;

        if (this.date == null)
            this.date = LocalDate.now();

        if (time == null) {
            useDefEventTime();
        } else {
            this.time = time;
            defEventTimeUsing = false;
        }
    }

    public final void setDefaultTime() {
        setTime(Journal.getEventDayDefTime());
    }

    public final boolean defEventTimeUsed() {
        return defEventTimeUsing;
    }

    public final void useDefEventTime() {
        this.time = Journal.getEventDayDefTime();
        defEventTimeUsing = true;
    }

    public final RepeatMode getRepeatMode() {
        if (repeatMode == null)
            setRepeatMode();

        return repeatMode;
    }

    public final void setRepeatMode() {
        setRepeatMode(null);
    }

    public final void setRepeatMode(RepeatMode repeatMode) {
        setRepeatMode(repeatMode, null, null);
    }

    public final void setRepeatMode(RepeatMode repeatMode, Period period, LocalDateTime endRepeatTime) {
        this.repeatMode = repeatMode == null ? RepeatMode.OFF : repeatMode;
        setPeriod(period);
        this.endRepeatTime = endRepeatTime;
    }

    public Period getPeriod() {
        if (period == null)
            setPeriod();

        return period;
    }

    public final void setPeriod() {
        setPeriod(null);
    }

    public final void setPeriod(Period period) {
        switch (this.repeatMode) {
            case SINGLE:
                if (period != null)
                    this.period = period;
                else {
                    this.period = new Period();
                    this.repeatMode = RepeatMode.OFF;
                }
                break;
            case DAILY:
                this.period = Period.DAY;
                break;
            case WEEKLY:
                this.period = Period.WEEK;
                break;
            case MONTHLY:
                this.period = Period.MONTH;
                break;
            case YEARLY:
                this.period = Period.YEAR;
                break;
            default:
                this.period = new Period();
        }
    }

    public final String getDescription() {
        if (!Data.isCorrect(description))
            description = "";
        return Data.isCorrect(description) ? "\"" + description + "\"" : description;
    }

    public final void setDescription(String description) {
        if (Data.isCorrect(description))
            this.description = description;
        else
            this.description = "";
    }

    public final Status getStatus() {
        setStatus();
        return status;
    }

    public final void setStatus() {
        if (date == null)
            this.status = Status.ACTUAL;

        else if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now()))
            this.status = Status.EXPIRED;
        else this.status = Status.TEMPORARY;

        if (repeatMode != RepeatMode.OFF)
            this.status = Status.REPEATED;
    }

    public int getOrdinal() {
        return ordinal;
    }

    public void setOrdinal(int ordinal) {
        this.ordinal = ordinal > 0 ? Math.abs(ordinal) : 0;
    }

    public final String getInfo() {
        return Data.isCorrect(description) ? this + "\n\tОписание: " + description : this.toString();
    }

    public final Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type == null ? Type.PERSONAL : type;
    }

    @Override
    public final String toString() {
        StringBuilder string = new StringBuilder(Text.aquo(getTitle()));
        string.append(" (").append(getType()).append("; ");

        if (this.date != null) {
            string.append(this.date.format(Time.D_MMM_YYYY)).append(" ").append(this.time.format(Time.H_MM));

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            if (this.date.isEqual(today) && this.time.isAfter(now)) {
                Duration duration = Duration.between(LocalTime.now(), this.time);
                if (!duration.isZero()) {
                    int hours = duration.toHoursPart(),
                            minutes = duration.toMinutesPart();
                    string.append(" (сегодня, через ");
                    if (hours > 0)
                        string.append(hours).append(" час(-а/-ов) ");
                    if (minutes > 0)
                        string.append(minutes).append(" минут(-у/-ы))");
                    else string.append(duration.toSecondsPart()).append(" секунд(-у/-ы))");
                }
            }

            string.append("; ");
        }

        string.append("состояние: ").append(getStatus().getTitle());

        if (getRepeatMode() != RepeatMode.OFF)
            string.append(" ").append(getRepeatMode().title.toLowerCase());

        return string.append(")").toString();
    }

    public final boolean idEquals(Task task) {
        return getId() == task.getId();
    }

    public final boolean identical(Task task) {
        return idEquals(task) && Objects.equals(this.getDescription(), task.getDescription());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return idEquals(task);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle().toLowerCase(), getDate(), getTime());
    }
}