package planner;

import java.time.Duration;
import java.util.Objects;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;

import utils.Commands;
import utils.Data;
import utils.Commands.CommandException;
import utils.Text;
import utils.Time;

public class Task {

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


    private String title;
    private final LocalDateTime creationTime = LocalDateTime.now();
    private final int id;
    private int ordinal;
    private LocalDate date;
    private LocalTime time;
    private boolean defEventTimeUsing = false;
    private boolean isRepeated = false;
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
        this.id = hashCode();
        setStatus();
        setDescription(description);
    }
    //  ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~  //


    public final boolean alreadyExist() {
        return Journal.getJournal().containsKey(getId());
    }

    public void checkTime() {
        if (date == null)
            return;

        if (isDefEventTimeUsed())
            System.out.println("\n! Задача " + Text.aquo(getTitle()) + " назначена на " + this.date.format(Time.D_MMM_YYYY)
                    + ";\n\t! время задачи (" + Journal.getDefEventTime().format(Time.H_MM) + ") назначено по умолчанию.");

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
        return date == null ? null : LocalDateTime.of(date, time);
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

    public final void setDefaultTime() {
        setTime(Journal.getDefEventTime());
    }

    public final void setTime(LocalTime time) {
        if (this.date == null && time == null)
            return;

        if (this.date == null)
            this.date = LocalDate.now();

        if (time == null)
            useDefEventTime();
        else this.time = time;
    }

    public final boolean isDefEventTimeUsed() {
        return defEventTimeUsing;
    }

    public final void useDefEventTime() {
        this.time = Journal.getDefEventTime();
        defEventTimeUsing = true;
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
        if (date == null)
            this.status = Status.ACTUAL;

        if (date != null)
            if (LocalDateTime.of(date, time).isBefore(LocalDateTime.now()))
                this.status = Status.EXPIRED;
            else this.status = Status.TEMPORARY;

        if (isRepeated)
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
        string.append(" (");

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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Task)) return false;
        Task task = (Task) o;
        return getTitle().equals(task.getTitle()) && Objects.equals(getDate(), task.getDate()) && Objects.equals(getTime(), task.getTime());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getTitle(), getDate(), getTime());
    }
}