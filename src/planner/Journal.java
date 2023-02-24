package planner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Map.Entry;

import utils.Data;
import utils.Text;

public abstract class Journal {

    private static final String TASK_NUMBER_NOT_FOUND = "Ошибка поиска: использованный номер задачи в Журнале отсутствует.";
    private static final String IS_EMPTY = "Журнал задач пуст; записи о задачах отсутствуют.";
    private static final String MATCHES_NOT_FOUND = "\nCовпадений не найдено.";
    private static final String MATCHES_FOUND = "\nНайдено несколько совпадений:";
    private static final String CHOOSE_REQUIRED = "Выбери, пожалуйста, требуемое:";

    private static LocalTime defEventTime = LocalTime.of(9, 0, 0);
    private static Map<Integer, Task> journal = new HashMap<>();
    private static int taskCount = 0;


    public static void show() {
        if (Data.isCorrect(getJournal()))
            showTasks("Полный перечень задач:", journal);
        else
            System.out.println(IS_EMPTY);
    }

    public static void showTasks(Map<Integer, Task> journal) {
        showTasks(null, journal);
    }

    public static void showTasks(String message) {
        showTasks(message, getJournal());
    }

    public static void showTasks(String message, Map<Integer, Task> journal) {
        if (!Data.isCorrect(journal))
            System.out.println(MATCHES_NOT_FOUND);
        else
            Text.printList(taskList(journal), Text.PrintModes.NUMBERED_LIST_PM, message);
    }

    public static void showTask(int ordinalNumber) {
        if (!Data.isCorrect(getJournal())) {
            System.out.println(IS_EMPTY);
            return;
        }

        if (ordinalNumber > 0 && ordinalNumber <= getJournal().size())
            System.out.println(journal.values().toArray()[ordinalNumber]);
        else
            System.out.println("\n" + TASK_NUMBER_NOT_FOUND);
    }

    private static Map<Integer, Task> newJournal() {
        return newJournal(new Task[0]);
    }

    private static Map<Integer, Task> newJournal(Map<Integer, Task> taskMap) {
        return newJournal(taskMap.values().toArray(new Task[0]));
    }

    private static Map<Integer, Task> newJournal(Task... tasks) {
        if (!Data.isCorrect(tasks))
            return new HashMap<>();
        Map<Integer, Task> newJournal = new HashMap<>();
        int count = 0;
        for (Task task :
                tasks) {
            if (task != null)
                newJournal.put(++count, task);
        }

        return newJournal;
    }

    //    метод для тестов
    public static void newTask(Task task) {
        if (task == null)
            return;

        Map<Integer, Task> journal = getJournal();
        if (!journal.containsValue(task))
            journal.put(task.getId(), task);
        else {

        }
    }

    public static void newTask() {
        System.out.print("Введите название задачи: ");
//        String taskName = scanner.next();
        // todo
    }

    public static String[] taskList(Map<Integer, Task> taskMap) {
        return Text.mapStrings(taskMap, ". ");
    }


    public static LocalTime getDefEventTime() {
        return defEventTime;
    }

    public static void setDefEventTime(LocalTime defEventTime) {
        Journal.defEventTime = defEventTime;
    }

    public static Map<Integer, Task> getJournal() {
        if (journal == null)
            setJournal();

        return journal;
    }

    private static void setJournal() {
        journal = new HashMap<>();
    }

    public static int getRecNumber(String taskPointer, Map<Integer, Task> journal) {
        if (!Data.isCorrect(taskPointer) || !Data.isCorrect(journal))
            return 0;

        Map<Integer, Task> matches = new HashMap<>();
        try {
            return Integer.parseInt(taskPointer);

        } catch (Exception e) {
            Task task;
            for (Entry<Integer, Task> record :
                    journal.entrySet()) {
                task = record.getValue();

                if (task != null && task.getTitle().equals(taskPointer)) {
                    matches.put(record.getKey(), task);
                }
            }
        }

        if (Data.isCorrect(matches))
            if (matches.size() > 1)
                return selectAnRecord(matches);

            else {
                var taskNumber = matches.keySet().toArray()[0];
                if (taskNumber instanceof Integer)
                    return (int) taskNumber;
            }

        return 0;
    }

    private static int selectAnRecord(Map<Integer, Task> records) {
        Map<Integer, Task> newJournal = newJournal(records);
        showTasks(MATCHES_FOUND, newJournal);
        return getRecNumber(Data.request(CHOOSE_REQUIRED), newJournal);
    }


    public static Map<Integer, Task> selection(String title, LocalDate date) {
        return selection(title, date, null, new HashMap<>());
    }

    public static Map<Integer, Task> selection(String title, LocalTime time) {
        return selection(title, LocalDate.now(), time, new HashMap<>());
    }

    public static Map<Integer, Task> selection(LocalDate date, LocalTime time) {
        return selection(null, date, time, new HashMap<>());
    }

    public static Map<Integer, Task> selection(String title, LocalDate date, LocalTime time) {
        return selection(title, date, time, new HashMap<>());
    }

    public static Map<Integer, Task> selection(String title, LocalDate date, LocalTime time, Map<Integer, Task> journal) {
        if (Data.isCorrect(journal))
            journal = getJournal();
        Map<Integer, Task> selection = new HashMap<>();

        if (Data.isCorrect(title))
            selection = selection(title, journal);

        if (date != null)
            selection = selection(date, journal);

        if (time != null)
            selection = selection(time, journal);

        return selection;
    }

    public static Map<Integer, Task> selection(String taskPointer) {
        return selection(taskPointer, new HashMap<>());
    }

    public static Map<Integer, Task> selection(String taskPointer, Map<Integer, Task> journal) {
        if (Data.isCorrect(journal))
            journal = getJournal();

        if (!Data.isCorrect(taskPointer))
            return new HashMap<>();

        try {
            return newJournal(getJournal().get(Integer.parseInt(taskPointer)));
        } catch (Exception e) {
            Map<Integer, Task> requested = new HashMap<>();
            int count = 0;
            for (Task task :
                    journal.values()) {
                if (task != null && task.getTitle().equals(taskPointer))
                    requested.put(++count, task);
            }

            return requested;
        }
    }

    public static Map<Integer, Task> selection(LocalDate date) {
        return selection(date, new HashMap<>());
    }

    public static Map<Integer, Task> selection(LocalDate date, Map<Integer, Task> journal) {
        if (Data.isCorrect(journal))
            journal = getJournal();

        if (date == null)
            return new HashMap<>();

        Map<Integer, Task> requested = new HashMap<>();
        int count = 0;
        for (Task task :
                journal.values()) {
            if (task != null && task.getDate() == date)
                requested.put(++count, task);
        }

        return requested;
    }

    public static Map<Integer, Task> selection(LocalTime time) {
        return selection(time, new HashMap<>());
    }

    public static Map<Integer, Task> selection(LocalTime time, Map<Integer, Task> journal) {
        if (Data.isCorrect(journal))
            journal = getJournal();

        if (time == null)
            return new HashMap<>();

        Map<Integer, Task> requested = new HashMap<>();
        int count = 0;
        for (Task task :
                journal.values()) {
            if (task != null && task.getTime() == time)
                requested.put(++count, task);
        }

        return requested;
    }
}