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
            Command.ADD, Command.SBD, Command.EDIT, Command.DEL, Command.SHALL, Command.SHOW, Command.TEST, Command.CANCEL);
    public static final Menu ED_MENU = new Menu("~ Правка задачи ~" + "\nЧто предпринять?",
            Command.REN, Command.EDDATE, Command.EDTIME, Command.EDDESCR, Command.CANCEL);

    private static final String TASK_NUMBER_NOT_FOUND = "Ошибка поиска: использованный номер задачи в Журнале отсутствует.";
    public static final String ENTER_THE_TASKPOINTER = "Введите номер или название (заголовок) задачи:";
    private static final String IS_EMPTY = "Журнал задач пуст; записи о задачах отсутствуют.";
    private static final String NO_MATCHES_FOUND = "\nCовпадений не найдено.";
    private static final String MATCHES_FOUND = "\nНайдено несколько совпадений:";
    private static final String CHOOSE_REQUIRED = "Выбери, пожалуйста, требуемое:";

    private static LocalTime defEventDayTime = LocalTime.of(9, 0, 0);
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
    private static SortMode defSortMode = SortMode.ASC;


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
        show(getDefSortMode());
    }

    public static void show(SortMode sortMode) {
        if (Data.isCorrect(getJournal()))
            showTasks("\n\tПолный перечень задач:", journal, sortMode);
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
            Task[] tasks = journal.values().toArray(new Task[0]);
            Arrays.sort(tasks, sortMode == null ? getDefSortMode().comparator() : sortMode.comparator());
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

    public static void editTask() {
        Task edited = requestTaskPointer(ENTER_THE_TASKPOINTER);
        if (edited == null)
            return;

        ED_MENU.show(edited);
    }

    public static Task editTaskDescr(Task edited) {
        if (edited == null)
            return null;
        edited.setDescription(Request.string("Введите новое описание:"));
        return edited;
    }

    public static Task editTaskDayTime(Task edited) {
        if (edited == null)
            return null;

        LocalDate oldDate = edited.getDate(),
                newDate = oldDate != null ? oldDate :
                        Request.confirm("День напоминания не назначен. Желаете назначить его самостоятельно?" +
                                "\n(В случае отказа днём напоминания будет назначен сегодняшний день - "
                                + LocalDate.now().format(Time.D_MM_YYYY) + ")") ?
                                Request.date(Request.ENTER_YEARDAY.replaceFirst("день", "новый день напоминания"))
                                : LocalDate.now();
        if (newDate == null)
            newDate = LocalDate.now();

        LocalTime newDayTime = Request.time(Request.ENTER_DAYTIME.replace("время", "новое время напоминания")
                + "\n(или «Enter» - для отмены)");
        StringBuilder message = new StringBuilder("Время напоминания ");
        LocalTime oldDayTime = edited.getTime();
        LocalDateTime oldTime = edited.getDateTime();
        if (newDayTime == null) {
            if (oldDayTime == null)
                message.append("не назначено.");
            else message.append("осталось прежним.");
            System.out.println(message.append(" Изменение отклонено пользователем."));
            return edited;
        }

        LocalDateTime newTime = LocalDateTime.of(newDate, newDayTime);
        Task firstMatch = firstMatch(edited, newTime),
                correctedTask = firstMatch != null && applyChanges("\n! Задача с указанным временем напоминания и заголовком корректируемой задачи уже существует." +
                                "\nЕсли продолжить уже существующая задача будет заменена корректируемой.",
                        firstMatch, edited, "Продолжить смену времени напоминания?") ?
                        replaceTask(firstMatch, newTime, edited) : replaceTask(null, newTime, edited);
        if (correctedTask == null)
            message = new StringBuilder("! Ошибка изменения времени напоминания.");
        else {
            if (oldTime == null)
                message.append("назначено на ");
            else message.append("переназначено с ").append(oldTime.format(Time.D_MMM_YYYY_H_MM)).append(" на ");
        }

        if (oldDate == null && newDate.isEqual(LocalDate.now()))
            System.out.println("День напоминания по умолчанию назначен сегодняшним (" + newDate.format(Time.D_MM_YYYY) + ").");
        System.out.println(message.append(newTime.format(Time.D_MMM_YYYY_H_MM)).append("."));
        return correctedTask;
    }

    public static Task editTaskDate(Task edited) {
        if (edited == null)
            return null;
        Task correctedTask = edited;

        LocalDateTime oldTime = edited.getDateTime();

        LocalDate newDate = Request.date(Request.ENTER_YEARDAY.replaceFirst("день", "новый день напоминания") +
                "\n(или нажмите «Enter» для его обнуления)");
        LocalTime newDayTime = null;
        LocalDateTime newTime;

        StringBuilder message = new StringBuilder("Время напоминания ");
        if (newDate != null) {
            if (Request.confirm("Желаете изменить время напоминания:"))
                newDayTime = Request.time(Request.ENTER_DAYTIME.replace("время", "новое время напоминания")
                        + "\n(или «Enter» - для отмены)");
            newTime = newDayTime == null ? LocalDateTime.of(newDate, edited.getTime()) : LocalDateTime.of(newDate, newDayTime);

            Task firstMatch = firstMatch(edited, newTime);
            correctedTask = firstMatch != null && applyChanges(
                    "\n! Задача с указанным днём напоминания и заголовком корректируемой задачи уже существует." +
                            "\nЕсли продолжить уже существующая задача будет заменена корректируемой.",
                    firstMatch, edited, "Продолжить смену дня напоминания?") ?
                    replaceTask(firstMatch, newTime, edited) : replaceTask(null, newTime, edited);

            if (oldTime == null)
                message.append("назначено на ").append(newTime.format(Time.D_MMM_YYYY_H_MM));
            else if (newTime.isEqual(oldTime))
                message.append("осталось прежним");
            else message.append("переназначено c ").append(oldTime.format(Time.D_MMM_YYYY_H_MM))
                        .append(" на ").append(newTime.format(Time.D_MMM_YYYY_H_MM));
            message.append(".");

        } else {
            if (oldTime == null) {
                message.append("не назначено.");
            } else {
                message.append("обнулено; напоминание отключено.");
                newTime = null;
                correctedTask = replaceTask(null, newTime, edited);
            }
        }

        if (Data.isCorrect(message))
            System.out.println(message);
        else System.out.println("Изменение времени напоминания отменено.");

        return correctedTask;
    }

    public static Task renTask(Task renamed) {
        if (renamed == null)
            return null;

        String newTitle = Request.string("Введите новое название (заголовок) задачи:");
        if (!Data.isCorrect(newTitle))
            return renamed;

        Task firstMatch = firstMatch(renamed, newTitle),
                newTask = firstMatch != null && applyChanges(
                        "\n! Задача с указанным заголовком и сроком переименовываемой задачи уже существует." +
                                "\nЕсли продолжить уже существующая задача будет заменена переименовываемой.",
                        firstMatch, renamed, "Продолжить переименование?") ?
                        replaceTask(firstMatch, newTitle, renamed) : replaceTask(null, newTitle, renamed);
        System.out.println("Задача «" + renamed.getTitle() + "» переименована в «" + newTitle + "».");
        return newTask;
    }

    public static void delTask() {
        delTask(null);
    }

    public static void delTask(Task deleted) {
        if (deleted == null)
            deleted = requestTaskPointer(ENTER_THE_TASKPOINTER);

        if (deleted != null)
            try {
                Journal.getJournal().remove(deleted.getId());
                Text.print(1, "Задача " + deleted + " успешно удалена.");
            } catch (Exception e) {
                Text.print(1, "Удаление задачи " + deleted + " запрещено.");
            }
    }

    private static Task replaceTask(Task replacing, String newDescription) {
        return replaceTask(null, replacing.getTitle(), replacing, replacing.getDate(), replacing.getTime(), newDescription);
    }

    //  используется?
    private static Task replaceTask(Task replaced, LocalTime newTime, Task replacing) {
        return replaceTask(replaced, replacing.getTitle(), replacing, replacing.getDate(), newTime, replacing.getDescription());
    }

    private static Task replaceTask(Task replaced, LocalDateTime newTime, Task replacing) {
        LocalDate newDate = newTime == null ? null : newTime.toLocalDate();
        LocalTime newDayTime = newTime == null ? null : newTime.toLocalTime();
        return replaceTask(replaced, replacing.getTitle(), replacing, newDate, newDayTime, replacing.getDescription());
    }

    private static Task replaceTask(Task replaced, String newTitle, Task replacing) {
        return replaceTask(replaced, newTitle, replacing, replacing.getDate(), replacing.getTime(), replacing.getDescription());
    }

    private static Task replaceTask(Task replaced, String newTitle, Task replacing,
                                    LocalDate newDate, LocalTime newDayTime, String newDescription) {
        if (replacing == null)
            return replaced;

        Task newTask;
        if (!Data.isCorrect(newTitle))
            newTitle = replacing.getTitle();
        if (newDate == null)
            newDayTime = null;
        if (newDayTime == null && newDate != null)
            newDayTime = replacing.getTime();

        try {
            newTask = new Task(newTitle, newDate, newDayTime, newDescription);
            if (newTask.equals(replacing))
                return replacing;
            if (replaced != null && getJournal().containsKey(replaced.getId()))
                journal.remove(replaced.getId());
            journal.remove(replacing.getId());

            journal.put(newTask.getId(), newTask);
            Journal.sort();
            return newTask;

        } catch (CommandException e) {
            Text.printException(e, "Ошибка замены задачи");
            return replaced;
        }
    }

    private static Task firstMatch(Task compared, LocalDateTime comparedTime) {
        if (compared == null || comparedTime == null)
            return null;

        LocalDateTime journalTaskTime;
        for (Task journalTask :
                getJournal().values()) {

            if (journalTask != null) {
                if (!journalTask.getTitle().equalsIgnoreCase(compared.getTitle()))
                    continue;

                journalTaskTime = journalTask.getDateTime();
                if (journalTaskTime != null && journalTaskTime.isEqual(comparedTime))
                    return journalTask;
            }
        }
        return null;
    }

    private static Task firstMatch(Task compared, String comparedTitle) {
        if (compared == null || !Data.isCorrect(comparedTitle))
            return null;

        for (Task journalTask :
                getJournal().values()) {
            if (journalTask != null && journalTask.getTitle().equalsIgnoreCase(comparedTitle)
                    && journalTask.getDate().isEqual(compared.getDate()) && journalTask.getTime().equals(compared.getTime()))
                return journalTask;
        }

        return null;
    }

    public static Task requestTaskPointer(String message) {
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

    //  TasksSetCreation for Test
    public static void createTasksSet() {
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
                    "\n(Если не указать время, оно будет назначено по умолчанию на " + getDefEventDayTime().format(Time.H_MM) + ")"))
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

    public static void sort() {
        sort(null, getDefSortMode());
    }

    public static void sort(SortMode sortMode) {
        sort(null, sortMode);
    }

    private static void sort(Map<Integer, Task> journal) {
        sort(journal, getDefSortMode());
    }

    public static void sort(Map<Integer, Task> journal, SortMode sortMode) {
        if (!Data.isCorrect(journal))
            journal = getJournal();

        int length = journal.size();
        Task[] tasks = journal.values().toArray(new Task[length]);

        Arrays.sort(tasks, sortMode == null ? getDefSortMode().comparator() : sortMode.comparator());
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


    public static LocalTime getDefEventDayTime() {
        return defEventDayTime;
    }

    public static void setDefEventDayTime(LocalTime defEventDayTime) {
        Journal.defEventDayTime = defEventDayTime;
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