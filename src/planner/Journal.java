package planner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.*;
import java.util.Map.Entry;

import utils.*;
import utils.Sortable.SortMode;
import utils.Commands.*;

public abstract class Journal {

    //    Menus
    public static final Menu MAIN_MENU = new Menu("Что предпринять?",
            Command.ADD, Command.SBD, Command.REN, Command.DEL, Command.SHALL, Command.SHOW, Command.TEST, Command.CANCEL);

    private static final String TASK_NUMBER_NOT_FOUND = "Ошибка поиска: использованный номер задачи в Журнале отсутствует.";
    private static final String ENTER_THE_TASKPOINTER = "Введите номер или название (заголовок) задачи:";
    private static final String IS_EMPTY = "Журнал задач пуст; записи о задачах отсутствуют.";
    private static final String NO_MATCHES_FOUND = "\nCовпадений не найдено.";
    private static final String MATCHES_FOUND = "\nНайдено несколько совпадений:";
    private static final String CHOOSE_REQUIRED = "Выбери, пожалуйста, требуемое:";

    private static LocalTime defEventTime = LocalTime.of(9, 0, 0);
    private static Map<Integer, Task> journal = new HashMap<>();
    private static int taskCount = 0;

    //  Comparators
    public static final Comparator<Task> ASC = (first, second) -> {
        if (first == null && second == null)
            return 0;

        if (first == null)
            return -1;

        if (second == null)
            return 1;

        LocalDateTime firstTime = first.getDateTime(),
                secondTime = second.getDateTime();

        if (first.getStatus() != second.getStatus())
            return second.getStatus().getOrdinal() - first.getStatus().getOrdinal();
        else if (firstTime != null && secondTime != null)
            if (!firstTime.isEqual(secondTime))
                return firstTime.compareTo(secondTime);
            else if (!first.getTitle().equalsIgnoreCase(second.getTitle()))
                return first.getTitle().compareToIgnoreCase(second.getTitle());
            else return first.getCreationTime().compareTo(second.getCreationTime());

        else if (firstTime == null && secondTime != null)
            return -1;
        else if (firstTime != null)
            return 1;
        else if (!first.getTitle().equalsIgnoreCase(second.getTitle()))
            return first.getTitle().compareToIgnoreCase(second.getTitle());
        else return first.getCreationTime().compareTo(second.getCreationTime());
    };
    public static final Comparator<Task> DESC = (first, second) -> {
        if (first == null && second == null)
            return 0;
        if (first == null)
            return 1;
        if (second == null)
            return -1;

        LocalDateTime firstTime = first.getDateTime(),
                secondTime = second.getDateTime();

        if (first.getStatus() != second.getStatus())
            return first.getStatus().getOrdinal() - second.getStatus().getOrdinal();
        else if (firstTime != null && secondTime != null)
            if (!firstTime.isEqual(secondTime))
                return secondTime.compareTo(firstTime);
            else if (!first.getTitle().equalsIgnoreCase(second.getTitle()))
                return first.getTitle().compareToIgnoreCase(second.getTitle());
            else return second.getCreationTime().compareTo(first.getCreationTime());

        else if (firstTime == null && secondTime != null)
            return 1;
        else if (firstTime != null)
            return -1;
        else if (!first.getTitle().equalsIgnoreCase(second.getTitle()))
            return first.getTitle().compareToIgnoreCase(second.getTitle());
        else return second.getCreationTime().compareTo(first.getCreationTime());
    };
    public static final Comparator<Task> ORD = Comparator.comparingInt(Task::getOrdinal);
    private static SortMode defSortMode = SortMode.DESC;


    public static void showByTaskPointer() {
        if (!Data.isCorrect(getJournal())) {
            System.out.println(IS_EMPTY);
            return;
        }

        Map<Integer, Task> requested;
        do {
            requested = selection(
                    Request.string("\nПо какой задаче следует вывести сведения?" +
                            "\n(укажите порядковый номер либо название (заголовок) задачи)"));
            if (!Data.isCorrect(requested)) {
                System.out.println(NO_MATCHES_FOUND);
                return;
            }
            if (requested.size() > 1)
                showTasks(MATCHES_FOUND, requested);
        } while (requested.size() > 1);

        Task[] tasks = requested.values().toArray(new Task[0]);
        System.out.println(tasks[0].getInfo() + ".");
    }

    public static void show() {
        show(defSortMode);
    }

    public static void show(SortMode sortMode) {
        if (Data.isCorrect(getJournal()))
            showTasks("\nПолный перечень задач:", journal, sortMode);
        else
            System.out.println(IS_EMPTY);
    }

    public static void showTasks(Map<Integer, Task> journal) {
        showTasks(null, journal, null);
    }

    public static void showTasks(Map<Integer, Task> journal, SortMode sortMode) {
        showTasks(null, journal, sortMode);
    }

    public static void showTasks(String message) {
        showTasks(message, getJournal());
    }

    public static void showTasks(String message, Map<Integer, Task> journal) {
        showTasks(message, journal, null);
    }

    public static void showTasks(String message, Map<Integer, Task> journal, SortMode sortMode) {
        if (!Data.isCorrect(journal))
            System.out.println(NO_MATCHES_FOUND);

        else {
            if (sortMode == null)
                sortMode = defSortMode;
            Task[] tasks = journal.values().toArray(new Task[0]);
            Arrays.sort(tasks, sortMode.comparator());
            reassignOrdinals(tasks);
            Text.printList(message, taskList(tasks), Text.PrintMode.SIMPLE);
        }
    }

    public static String[] taskList(Task[] tasks) {
        if (!Data.isCorrect(tasks))
            return new String[0];

        String[] list = new String[Data.notNullObjectsNumber(tasks)];
        int index = -1;
        for (Task task :
                tasks) {
            if (task != null && Data.isCorrect(task.getTitle()))
                list[++index] = task.getOrdinal() + ". " + task;
        }

        return list;
    }

    public static void delTask() {
        Task deleted = requestTaskPointer(ENTER_THE_TASKPOINTER);
        if (deleted != null)
            try {
                Journal.getJournal().remove(deleted.getId());
                Text.print(1, "Задача " + deleted + " успешно удалена.");
            } catch (Exception e) {
                Text.print(1, "Удаление задачи " + deleted + " запрещено.");
            }
    }

    public static void renTask() {
        Task renamedTask = requestTaskPointer(ENTER_THE_TASKPOINTER);
        if (renamedTask == null)
            return;

        String newTitle = Request.string("Введите новое название (заголовок) задачи:");
        if (!Data.isCorrect(newTitle))
            return;

        for (Task current :
                journal.values()) {
            if (current != null && current.getTitle().equalsIgnoreCase(newTitle)
                    && current.getDate().isEqual(renamedTask.getDate()) && current.getTime().equals(renamedTask.getTime()))
                if (applyChanges("\n! Задача с указанным заголовком и сроком переименовываемой задачи уже существует." +
                                "\nЕсли продолжить уже существующая задача будет заменена переименовываемой.",
                        current, renamedTask, "Продолжить переименование?")) {
                    journal.remove(current.getId());
                    break;
                } else return;
        }


        journal.remove(renamedTask.getId());
        Task newTask = null;
        try {
            newTask = new Task(newTitle, renamedTask.getDate(), renamedTask.getTime(), renamedTask.getDescription());
        } catch (CommandException e) {
            return;
        }

        journal.put(newTask.getId(), newTask);
        Journal.sort();
    }

    private static Task requestTaskPointer(String message) {
        String userChoice = null;
        int taskNumber = -1;
        Task taskPointer = null;
        while (taskNumber < 0 && userChoice == null && taskPointer == null) {
            userChoice = Request.string(message);
            try {
                if (userChoice != null)
                    taskNumber = Math.abs(Integer.parseInt(userChoice));
            } catch (Exception e) {
                if (!Data.isCorrect(userChoice))
                    return null;
            }
            if (taskNumber == 0)
                return null;

            for (Task current :
                    getJournal().values()) {
                if (current.getOrdinal() == taskNumber || current.getTitle().equalsIgnoreCase(userChoice)) {
                    taskPointer = current;
                    break;
                }
            }

            if (taskPointer == null) {
                System.out.println(NO_MATCHES_FOUND);
                return requestTaskPointer(message);
            }
        }

        return taskPointer;
    }

    //        test
    public static void test() {
        LocalDate aug10y23 = LocalDate.of(2023, 8, 10);
        LocalDate mar10y23 = LocalDate.of(2023, 3, 10);
        LocalDate mar09y23 = LocalDate.of(2023, 3, 9);
        LocalDate dec31y22 = LocalDate.of(2022, 12, 31);
        LocalDate aug10y22 = LocalDate.of(2022, 8, 10);

        LocalTime h18m00 = LocalTime.of(18, 0);
        LocalTime h9m00 = LocalTime.of(9, 0);
        LocalTime h7m35 = LocalTime.of(7, 35);
        LocalTime h7m30 = LocalTime.of(7, 30);
        LocalTime h4m30 = LocalTime.of(4, 30);

        Task task1 = null, task2 = null, task3 = null, task4 = null, task5 = null, task6 = null;
        try {
            task1 = new Task("Задача 1", aug10y23, h7m30);
            task2 = new Task("Задача 01", aug10y23, h7m30);
            task3 = new Task("Задача 3");
            task4 = new Task("Воооопсче не задача", mar10y23);
            task5 = new Task("Задача 546", mar09y23, h18m00);
            task6 = new Task("Задача 446", dec31y22, h4m30);
        } catch (CommandException e) {
            Text.printException(e);
        }
        Journal.newTask(task1, task2, task3, task4, task5, task6);
    }

    public static void newTask() {
        String title = Request.string("\nВведите название (заголовок) задачи:" +
                "\n(или \"пустую строку\" - для отмены)");
        if (!Data.isCorrect(title))
            return;

        LocalDate date = null;
        if (Request.confirm("Назначить день напоминания?"))
            date = Request.date("Укажите, пожалуйста, день:\n(в формате: деньМесяца.номерМесяца.год)");

        LocalTime time = null;
        if (date != null)
            if (Request.confirm("Назначить время напоминания?" +
                    "\n(Если не указать время, оно будет назначено по умолчанию на " + getDefEventTime().format(Time.H_MM) + ")"))
                time = Request.time("Укажите, пожалуйста, время:\n(в формате: Час:Минута)");

        String description = null;
        if (Request.confirm("Желаете добавить описание задачи?"))
            description = Request.string("Описание:");

        try {
            newTask(new Task(title, date, time, description));
        } catch (CommandException e) {
            Text.printException(e);
        }
    }

    public static void newTask(Task... newTasks) {
        if (!Data.isCorrect(newTasks))
            return;

        for (Task newTask :
                newTasks) {
            if (newTask == null)
                continue;

            if (newTask.alreadyExist())
                if (!applyChanges("\n! Найдено сходство:",
                        getJournal().get(newTask.getId()), newTask, "Заменить существующую задачу добавляемой?" +
                                "\n(В случае отказа введёные данные будут утрачены.)"))
                    continue;

            journal.put(newTask.getId(), newTask);
            newTask.checkTime();
            newTask.setOrdinal(++taskCount);
            sort();
        }
    }

    private static boolean applyChanges(String message, Task oldTask, Task newTask, String ask) {
        Text.printList(message, new String[]{
                "\tуже существующая задача: " + oldTask.getInfo(),
                "\tдобавляемая задача: " + newTask.getInfo()}, Text.PrintMode.BULLETED);
        return Request.confirm(ask);
    }

    private static void sort() {
        sort(null, SortMode.DESC);
    }

    private static void sort(SortMode sortMode) {
        sort(null, sortMode);
    }

    private static void sort(Map<Integer, Task> journal) {
        sort(journal, SortMode.DESC);
    }

    private static void sort(Map<Integer, Task> journal, SortMode sortMode) {
        if (!Data.isCorrect(journal))
            journal = getJournal();

        int length = journal.size();
        Task[] tasks = journal.values().toArray(new Task[length]);
        Comparator<Task> comparator = sortMode == SortMode.ASC ? ASC : DESC;

        Arrays.sort(tasks, comparator);
        reassignOrdinals(tasks);
    }

    private static void reassignOrdinals(Task[] tasks) {
        int count = 0;
        for (Task task :
                tasks) {
            if (task != null)
                task.setOrdinal(++count);
        }
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
        if (!Data.isCorrect(journal))
            journal = getJournal();

        if (!Data.isCorrect(taskPointer))
            return new HashMap<>();

        int taskNumber;
        try {
            taskNumber = Integer.parseInt(taskPointer);
        } catch (Exception e) {
            taskNumber = 0;
        }

        Map<Integer, Task> requested = new HashMap<>();
        for (Task task :
                journal.values()) {
            if (task != null)
                if (taskNumber > 0 && task.getOrdinal() == taskNumber)
                    return newJournal(task);
                else if (task.getTitle().equalsIgnoreCase(taskPointer))
                    requested.put(task.getId(), task);
        }

        return newJournal(requested);
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

    public static int requestRecNumber(String taskPointer, Map<Integer, Task> journal) {
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

                if (task != null && task.getTitle().equalsIgnoreCase(taskPointer)) {
                    matches.put(record.getKey(), task);
                }
            }
        }

        if (matches.size() > 0)
            if (matches.size() > 1)
                return clarifyNumberChoice(matches);
            else
                return (int) matches.keySet().toArray()[0];

        return 0;
    }

    public static int clarifyNumberChoice(Map<Integer, Task> records) {
        Map<Integer, Task> newJournal = newJournal(records);
        showTasks(MATCHES_FOUND, newJournal);
        return requestRecNumber(Request.string(CHOOSE_REQUIRED), newJournal);
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
            if (task != null) {
                task.setOrdinal(++count);
                newJournal.put(task.getId(), task);
            }
        }

        sort(newJournal);
        return newJournal;
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

    public static SortMode getDefSortMode() {
        return defSortMode;
    }

    public static void setDefSortMode(SortMode defSortMode) {
        Journal.defSortMode = defSortMode == null ? SortMode.DESC : defSortMode;
    }
}