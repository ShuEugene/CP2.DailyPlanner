package planner;

import java.util.Map;
import java.util.Objects;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import utils.Commands;
import utils.Data;
import utils.Commands.CommandException;
import utils.Text;
import utils.Time.Period;
import utils.Time.Period.PeriodType;

public class Task {

    public enum Status {
        ACTUAL("действующее"),
        EXPIRED("просроченное"),
        FROZEN("приостановленное");

        private final String title;

        Status(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }


    private String title;
    private final LocalDateTime creationDateTime = LocalDateTime.now();
    private int id;
    private LocalDate date;
    private LocalTime time;
    private Status status;
    private String description;

    //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  //
    public Task(String title) throws CommandException {
        this(title, null, null, null);
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

    public Task(String title, LocalTime time, String description) throws CommandException {
        this(title, null, time, description);
    }

    public Task(String title, LocalDate date, String description) throws CommandException {
        this(title, date, null, description);
    }

    public Task(String title, LocalDate date, LocalTime time) throws CommandException {
        this(title, date, time, null);
    }

    public Task(String title, LocalDate date, LocalTime time, String description) throws CommandException {
        if (!Data.isCorrect(title))
            throw new CommandException(Commands.CommandException.UNTITLED_TASK_);

        setTitle(title);
        setDate(date);
        setTime(time);
        setId();
        setStatus();
        setDescription(description);
    }
    //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  //


//  »? private
    public final void update() {
        Map<Integer, Task> updatedJournal = Journal.getJournal();
        updatedJournal.remove(this.getId());
        setId();
        updatedJournal.put(this.getId(), this);
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
        update();
    }

    public final int getId() {
        return id;
    }

    private final void setId() {
        this.id = this.hashCode();
    }

    public final Period getRemainingTime() {
        LocalTime now = LocalTime.now();

        int period,
                hours = this.time.getHour() - now.getHour(),
                minutes = this.time.getMinute() - now.getMinute();
        PeriodType periodType;

        if (hours > 0) {
            periodType = PeriodType.HOURS;
            period = hours;
        } else {
            periodType = PeriodType.MINUTES;
            period = minutes;
        }

        if (period > 0)
            return new Period(period, periodType);
        else return null;
    }

    public final LocalDate getDate() {
        return date;
    }

    public final void setDate(LocalDate date) {
        this.date = date;
        if (date == null && this.time != null)
            this.time = null;

        if (date != null && date.isBefore(LocalDate.now()))
            System.out.println("Внимание!\nЗадача назначена на уже прошедший день.");

        update();
    }

    public final LocalTime getTime() {
        return time;
    }

    public final void setDefaultTime() {
        setTime(null);
    }

    public final void setTime(LocalTime time) {
        if (this.date == null && time == null)
            return;

        if (this.date == null)
            this.date = LocalDate.now();

        this.time = time == null ? Journal.getDefEventTime() : time;

        if (time == null)
            System.out.println("\nЗадача назначена на " + this.date + "; " +
                    "время задачи (" + Journal.getDefEventTime() + ") назначено по умолчанию.");

        if (LocalDateTime.of(this.date, this.time).isBefore(LocalDateTime.now()))
            System.out.println("Внимание!\nЗадача назначена на уже прошедшее время.");

        update();
    }

    public final String getDescription() {
        return "\"" + description + "\"";
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
        if (this.date == null && this.time == null
                || (this.date != null && this.time != null && LocalDateTime.of(this.date, this.time).isAfter(LocalDateTime.now()))
                || (this.date != null && this.time == null && this.date.isAfter(LocalDate.now()))
                || (this.date == null && this.time != null && LocalDateTime.of(LocalDate.now(), this.time).isAfter(LocalDateTime.now())))
            this.status = Status.ACTUAL;

        else this.status = Status.EXPIRED;

//  какой из них читабельнее?
/*
        if (this.date != null) {
            if (this.time != null) {
                if (LocalDateTime.of(this.date, this.time).isAfter(LocalDateTime.now()))
                    this.status = Status.ACTUAL;

            } else if (this.date.isAfter(LocalDate.now()))
                this.status = Status.ACTUAL;

            else this.status = Status.EXPIRED;

        } else this.status = Status.ACTUAL;
*/
    }

    public final String getInfo() {
        return Data.isCorrect(description) ? this + "\n" + description : this.toString();
    }

    @Override
    public final String toString() {
        StringBuilder string = new StringBuilder(Text.aquo(getTitle()));
        string.append(" (");

        if (this.date != null) {
            string.append(this.date);

            if (this.time != null)
                string.append(" ").append(this.time);

            LocalDate today = LocalDate.now();
            LocalTime now = LocalTime.now();

            if (this.date.isEqual(today) && this.time.isAfter(now)) {
                Period remainingTime = getRemainingTime();
                if (remainingTime != null)
                    string.append("(сегодня, через ").append(remainingTime).append(")");
            }

            string.append("; ");
        }

        string.append("состояние: ").append(getStatus().getTitle());
        string.append(")");

        return string.toString();
    }

    public final boolean idEquals(Task task) {
        return getId() == task.getId();
    }

    public final boolean simpleEquals(Task task) {
        return getTitle().equalsIgnoreCase(task.getTitle())
                && getDate() == task.getDate() && getTime() == task.getTime();
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return getTitle().equalsIgnoreCase(task.getTitle()) && getDescription().equalsIgnoreCase(task.getDescription())
                && getDate() == task.getDate() && getTime() == task.getTime();
    }

    @Override
    public final int hashCode() {
        return Objects.hash(creationDateTime, title, date, time);
    }
}
