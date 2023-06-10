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
    private LocalDateTime startDateTime, endDateTime;
    private LocalDate startDate;
    private LocalTime startTime;
    private boolean defEventTimeUsing = false;
    private RepeatMode repeatMode = RepeatMode.OFF;
    private Period period;
    private Set<LocalDateTime> repeats = new HashSet<>();
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

    public Task(String title, LocalDate startDate) throws CommandException {
        this(title, startDate, null, null);
    }

    public Task(String title, LocalTime startTime) throws CommandException {
        this(title, null, startTime, null);
    }

    public Task(String title, Type type) throws CommandException {
        this(title, type, null, null, null);
    }

    public Task(String title, LocalTime startTime, String description) throws CommandException {
        this(title, null, startTime, description);
    }

    public Task(String title, LocalDate startDate, String description) throws CommandException {
        this(title, startDate, null, description);
    }

    public Task(String title, LocalDate startDate, LocalTime startTime) throws CommandException {
        this(title, startDate, startTime, null);
    }

    public Task(String title, Type type, LocalDate startDate, LocalTime startTime) throws CommandException {
        this(title, type, startDate, startTime, null);
    }

    public Task(String title, LocalDate startDate, LocalTime startTime, String description) throws CommandException {
        this(title, null, startDate, startTime, description);
    }

    public Task(String title, Type type, LocalDate startDate, LocalTime startTime, String description) throws CommandException {
        this(title, type, startDate, startTime, null, description);
    }

    public Task(String title, Type type, LocalDate startDate, LocalTime startTime, RepeatMode repeatMode, String description) throws CommandException {
        this(title, type, startDate, startTime, repeatMode, null, null, description);
    }

    public Task(String title, Type type, LocalDateTime startDateTime,
                RepeatMode repeatMode, Period period, LocalDateTime endRepeatTime, String description) throws CommandException {
        this(title, type, startDateTime == null ? null : startDateTime.toLocalDate(),
                startDateTime == null ? null : startDateTime.toLocalTime(), repeatMode, period, endRepeatTime, description);
    }

    public Task(String title, Type type, LocalDate startDate, LocalTime startTime,
                RepeatMode repeatMode, Period period, LocalDateTime endRepeatTime, String description) throws CommandException {
        if (!Data.isCorrect(title))
            throw new CommandException(Commands.CommandException.UNTITLED_TASK_);

        setTitle(title);
        setType(type);
        setStartDate(startDate);
        setStartTime(startTime);
        setRepeatMode(repeatMode, period, endRepeatTime);
        this.id = hashCode();
        setStatus();
        setDescription(description);
    }
    /*  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  */


    public final void update() {
        LocalDateTime startDT = getStartDateTime(), endDT = getEndDateTime();
        if (startDT != null && startDT.isBefore(LocalDateTime.now())) {
            if (endDT == null)
                setRepeatMode(RepeatMode.OFF);
            setStatus(); ?здесь или ниже?

            if (getRepeatMode() != RepeatMode.OFF) {
                if (getRepeatMode() == RepeatMode.SINGLE) {
                    try {
                        newInstance = new Task(current.getTitle(), current.getType(), current.getEndDateTime(),
                                RepeatMode.OFF, null, null, current.getDescription());
                    } catch (CommandException e) {
                        allTasks.remove(current.getId());
                        temporaryTasks.remove(current.getId());
                        break checkStart;
                    }
                    allTasks.put(newInstance.getId(), newInstance);
                    checked.add(newInstance.getId());
                    break checkStart;

                } else {
                    duration = current.getPeriod().getDuration();
                    chronoUnit = current.getPeriod().getType();

                    newStartDT = curTaskDateTime.plus(duration, chronoUnit);
                    if (newStartDT.isBefore(current.getEndDateTime())) {
                        newEndDT = newStartDT.plus(duration, chronoUnit);
                        if (newEndDT.isAfter(current.getEndDateTime()))
                            newEndDT = current.getEndDateTime();
                    } else
                        newEndDT = null;

                    try {
                        newInstance = new Task(current.getTitle(), current.getType(), newStartDT,
                                newEndDT == null ? RepeatMode.OFF : current.getRepeatMode(),
                                current.getPeriod(), newEndDT, current.getDescription());
                    } catch (CommandException e) {
                        allTasks.remove(current.getId());
                        temporaryTasks.remove(current.getId());
                        break checkStart;
                    }

                    allTasks.put(newInstance.getId(), newInstance);
                    break checkStart;
                }
            }
        }
    }

    public final boolean alreadyExist() {
        return Journal.getJournal().containsKey(getId());
    }

    public void checkTime() {
        if (startDate == null)
            return;

        if (defEventTimeUsed())
            System.out.println("\n! Задача " + Text.aquo(getTitle()) + " назначена на " + this.startDate.format(Time.D_MMM_YYYY)
                    + ";\n\t! время задачи (" + Journal.getEventDayDefTime().format(Time.H_MM) + ") назначено по умолчанию.");

        LocalDateTime dateTime = LocalDateTime.of(startDate, startTime);
        if (dateTime.isBefore(LocalDateTime.now()))
            System.out.println("\n! Внимание:\n\tдля задачи " + Text.aquo(getTitle()) + " назначено уже прошедшее время - " + dateTime.format(Time.D_MMM_YYYY_H_MM) + ".");
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

    public final Type getType() {
        return type;
    }

    public void setType(Type type) {
        if (type != null)
            this.type = type;
        else if (this.type == null)
            this.type = Type.PERSONAL;
    }

    public final LocalDateTime getCreationTime() {
        return creationTime;
    }

    public final LocalDate getStartDate() {
        return startDate;
    }

    public final void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        if (startDate == null && this.startTime != null)
            this.startTime = null;

        setStartDateTime();
    }

    public final boolean defEventTimeUsed() {
        return defEventTimeUsing;
    }

    public final void useDefEventTime() {
        this.startTime = Journal.getEventDayDefTime();
        defEventTimeUsing = true;
    }

    public final LocalTime getStartTime() {
        return startTime;
    }

    public final void setStartTime(LocalTime time) {
        if (this.startDate == null && time == null)
            return;

        if (this.startDate == null)
            this.startDate = LocalDate.now();

        if (time == null)
            useDefEventTime();
        else {
            this.startTime = time;
            defEventTimeUsing = false;
        }

        startDateTime = LocalDateTime.of(startDate, startTime);
    }

    public final void setDefaultTime() {
        setStartTime(Journal.getEventDayDefTime());
    }

    public final LocalDateTime getStartDateTime() {
        return (startDate == null || startTime == null) ? null : LocalDateTime.of(startDate, startTime);
    }

    public final void setStartDateTime() {
        startDateTime = startDate == null || startTime == null ? null : LocalDateTime.of(startDate, startTime);
    }

    public final int getId() {
        return id;
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

    public final void setRepeatMode(RepeatMode repeatMode, Period period, LocalDateTime endTime) {
        if (this.repeatMode == null)
            this.repeatMode = repeatMode == null ? RepeatMode.OFF : repeatMode;

        else if (repeatMode == null)
            return;

        setPeriod(period);
        this.endDateTime = endTime;
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
                    this.period = Period.NULL;
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
                this.period = Period.NULL;
        }
    }

    public LocalDateTime getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(LocalDateTime endDateTime) {
        this.endDateTime = endDateTime;
        if (this.endDateTime == null)
            setRepeatMode(RepeatMode.OFF);
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
        if (startDate == null)
            this.status = Status.ACTUAL;

        else if (LocalDateTime.of(startDate, startTime).isBefore(LocalDateTime.now()))
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

    @Override
    public final String toString() {
        StringBuilder string = new StringBuilder(Text.aquo(getTitle()));
        string.append(" (").append(getType()).append("; ");

        if (this.startDate != null) {
            string.append(this.startDate.format(Time.D_MMM_YYYY)).append(" ").append(this.startTime.format(Time.H_MM));

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            if (this.startDate.isEqual(today) && this.startTime.isAfter(now)) {
                Duration duration = Duration.between(LocalTime.now(), this.startTime);
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
        return Objects.hash(getTitle().toLowerCase(), getStartDate(), getStartTime());
    }
}