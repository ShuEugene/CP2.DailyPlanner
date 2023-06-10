package planner;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.Map.Entry;

import utils.*;
import utils.Sortable.SortMode;
import utils.Commands.*;

import planner.Task.Type;
import planner.Task.RepeatMode;

import static planner.Task.Status.*;

public abstract class Journal {

    //    Menus
    public static final Menu MAIN_MENU = new Menu("\t* Работа с Ежедневником *" + "\nЧто предпринять?",
            Command.ADD, Command.EDIT, Command.DELETE, Command.SHOWTI, Command.SHOW, Command.TEST, Command.CANCEL);
    public static final Menu ED_MENU = new Menu("\t~ Правка задачи ~" + "\nЧто предпринять?",
            Command.REN, Command.CHANGET, Command.EDDATE, Command.EDTIME, Command.EDREP, Command.EDDESCR, Command.CANCEL);

    private static final String TASK_NUMBER_NOT_FOUND = "Ошибка поиска: использованный номер задачи в Журнале отсутствует.";
    public static final String ENTER_THE_TASKPOINTER = "Введите номер или название (заголовок) задачи:" + Request.OR_CANCEL;
    public static final String IS_EMPTY = "Журнал задач пуст; записи о задачах отсутствуют.";
    private static final String NO_MATCHES_FOUND = "\nCовпадений не найдено.";
    private static final String MATCHES_FOUND = "\nНайдено несколько совпадений:";
    private static final String CHOOSE_REQUIRED = "Выбери, пожалуйста, требуемое:";

    private static LocalTime eventDayDefTime = LocalTime.of(9, 0, 0);
    private static Type showMode = null;
    private static LocalDate targetDay = null;

    //  использован?
    public static abstract class TempTask {
        private static String title = "", description = "";
        private static Type type = null;
        private static LocalDateTime startTime = null, endTime = null;
        private static RepeatMode repeatMode = null;
        private static Period period = null;
        private static Task.Status status = null;

        public static void clone(Task cloned) {
            if (cloned == null) {
                System.out.println("Задача для клонирования не указана;" +
                        "\nподставная задача обнулена.");
                reset();
                return;
            }
            TempTask.title = cloned.getTitle();
            TempTask.description = cloned.getDescription();
            TempTask.type = cloned.getType();
            TempTask.startTime = cloned.getStartDateTime();
            TempTask.endTime = cloned.getEndDateTime();
            TempTask.repeatMode = cloned.getRepeatMode();
            TempTask.period = cloned.getPeriod();
            setStatus();
        }

        public static void reset() {
            TempTask.title = "";
            TempTask.description = "";
            TempTask.type = null;
            TempTask.startTime = null;
            TempTask.endTime = null;
            TempTask.repeatMode = null;
            TempTask.period = null;
            setStatus();
        }

        public static String getTitle() {
            return title;
        }

        public static void setTitle(String title) {
            TempTask.title = Data.isCorrect(title) ? title : "";
        }

        public static String getDescription() {
            return description;
        }

        public static void setDescription(String description) {
            TempTask.description = Data.isCorrect(description) ? description : "";
        }

        public static Type getType() {
            return type;
        }

        public static void setType(Type type) {
            TempTask.type = type;
        }

        public static LocalDateTime getStartTime() {
            return startTime;
        }

        public static void setStartTime(LocalDateTime startTime) {
            TempTask.startTime = startTime;
        }

        public static LocalDateTime getEndTime() {
            return endTime;
        }

        public static void setEndTime(LocalDateTime endTime) {
            TempTask.endTime = endTime;
        }

        public static RepeatMode getRepeatMode() {
            return repeatMode;
        }

        public static void setRepeatMode(RepeatMode repeatMode) {
            TempTask.repeatMode = repeatMode;
        }

        public static Period getPeriod() {
            return period;
        }

        public static void setPeriod() {
            setPeriod(null);
        }

        public static void setPeriod(Period period) {
            switch (TempTask.repeatMode) {
                case SINGLE:
                    if (period != null)
                        TempTask.period = period;
                    else {
                        TempTask.period = Period.NULL;
                        TempTask.repeatMode = RepeatMode.OFF;
                    }
                    break;
                case DAILY:
                    TempTask.period = Period.DAY;
                    break;
                case WEEKLY:
                    TempTask.period = Period.WEEK;
                    break;
                case MONTHLY:
                    TempTask.period = Period.MONTH;
                    break;
                case YEARLY:
                    TempTask.period = Period.YEAR;
                    break;
                default:
                    TempTask.period = Period.NULL;
            }
        }

        public static Task.Status getStatus() {
            return status;
        }

        public static void setStatus() {
            if (TempTask.startTime == null)
                TempTask.status = Task.Status.ACTUAL;

            else if (startTime.isBefore(LocalDateTime.now()))
                TempTask.status = Task.Status.EXPIRED;
            else TempTask.status = Task.Status.TEMPORARY;

            if (repeatMode != RepeatMode.OFF)
                TempTask.status = Task.Status.REPEATED;
        }
    }

    private static Map<Integer, Task> tempJournal = getJournal();
    private static Map<Integer, Task> journal = new HashMap<>();
    private static Map<Integer, Task> personalTasks = new HashMap<>();
    private static Map<Integer, Task> businessTasks = new HashMap<>();
    private static Map<Integer, Task> temporaryTasks = new HashMap<>();
    private static Map<Integer, Task> archiveTasks = new HashMap<>();
    private static int taskCount = 0;

    //  Comparators
    public static final Comparator<Task> ASC = (first, second) -> {
        if (first == null && second == null)
            return 0;

        if (first == null)
            return -1;

        if (second == null)
            return 1;

        LocalDateTime firstTime = first.getStartDateTime(),
                secondTime = second.getStartDateTime();

        if (first.getStatus().getOrdinal() != second.getStatus().getOrdinal())
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

        LocalDateTime firstTime = first.getStartDateTime(),
                secondTime = second.getStartDateTime();

        if (first.getStatus().getOrdinal() != second.getStatus().getOrdinal())
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


    public static void showTaskInfo() {
        if (!Data.isCorrect(tempJournal(showMode, targetDay))) {
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
        tasks[0].update();
        System.out.println(tasks[0].getInfo() + ".");
    }

    public static void show() {
        show(getDefSortMode());
    }

    public static void show(SortMode sortMode) {
        Journal.update();

        targetDay = Request.date(Request.ENTER_YEARDAY.substring(0, Request.ENTER_YEARDAY.length() - 1)
                + Request.OR_CANCEL.replace("\n(", " ")
                .replace("отмены", "отображения полного перечня задач"));

        String[] taskTypesNumList = Type.numList(Text.WordForm.PLURAL),
                allItems = Arrays.copyOf(taskTypesNumList, taskTypesNumList.length + 1);
        allItems[taskTypesNumList.length] = "0. Все";

        String showedTasksType;
        do {
            do {
                Text.printList("\nВыберите тип задач, которые следует показать:" + Request.OR_CANCEL, allItems, Text.PrintMode.SIMPLE);
                showedTasksType = Request.string();
                if (!Data.isCorrect(showedTasksType))
                    return;
                if (showedTasksType.equals("0")) {
                    showMode = null;
                    break;
                }
                showMode = Type.get(showedTasksType);
                if (showMode == null)
                    System.out.println("Указанный тип отсутствует.");
            } while (showMode == null);

            String taskType;
            if (showMode == null)
                taskType = "Все";
            else {
                taskType = showMode.pfTitle();
                taskType = taskType.replace(Character.toString(taskType.charAt(0)), (Character.toString(taskType.charAt(0)).toUpperCase()));
            }

            if (Data.isCorrect(tempJournal(showMode, targetDay)))
                showTasks("\n\t" + "~~~ " + taskType + " задачи"
                        + (targetDay == null ? "" : " на " + targetDay.format(Time.D_MM_YYYY)) + " ~~~", tempJournal, sortMode);
            else if (targetDay == null)
                System.out.println(IS_EMPTY);
            else System.out.println("На " + targetDay.format(Time.D_MM_YYYY)
                        + (showMode == null ? "" : " " + taskType.toLowerCase()) + " задачи отсутствуют.");

        } while (Request.confirm("\nПоказать задачи другого типа?"));
    }

    public static void update() {
        Map<Integer, Task> allTasks = getJournal();
        if (!Data.isCorrect(allTasks))
            return;

        Task newInstance;
        LocalDateTime newStartDT, newEndDT, curTaskDateTime;
        long duration;
        ChronoUnit chronoUnit;
        Set<Integer> checked = new HashSet<>();

        checkStart:
        for (Task current :
                allTasks.values()) {

            if (current != null && !checked.contains(current.getId())) {
                checked.add(current.getId());

                curTaskDateTime = current.getStartDateTime();
                if (curTaskDateTime != null && curTaskDateTime.isBefore(LocalDateTime.now())) {
                    if (current.getEndDateTime() == null)
                        current.setRepeatMode(RepeatMode.OFF);
                    current.setStatus();

                    if (current.getRepeatMode() != RepeatMode.OFF) {
                        if (current.getRepeatMode() == RepeatMode.SINGLE) {
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
        }
    }

    public static Map<Integer, Task> tempJournal(Type showMode) {
        return tempJournal(showMode, null);
    }

    public static Map<Integer, Task> tempJournal(Type showMode, LocalDate targetDay) {
        if (showMode == null)
            tempJournal = getJournal();
        else
            switch (showMode) {
                case PERSONAL:
                    tempJournal = getPersonalTasks();
                    break;
                case BUSINESS:
                    tempJournal = getBusinessTasks();
                    break;
                default:
                    tempJournal = getJournal();
            }

        if (targetDay != null)
            tempJournal = selection(targetDay, tempJournal);

        return tempJournal;
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


    public static void editTaskDescr(Task edited) {
        if (edited == null)
            return;
        edited.setDescription(Request.string("Введите новое описание:"));
    }

    public static void changeTaskRepeat(Task edited) {
        if (edited == null)
            return;
        System.out.println("Задача " + Text.aquo(edited.getTitle()) + " на данный момент "
                + (edited.getRepeatMode() == RepeatMode.OFF ? "не имеет повторов"
                : "повторяется " + edited.getRepeatMode()).toLowerCase() + ".");

        TempTask.clone(edited);
        edited.setRepeatMode(requestRepeatMode());
    }

    public static Task editTaskTime(Task edited) {
        if (edited == null)
            return null;

        LocalDate oldDate = edited.getStartDate(),
                today = LocalDate.now(),
                newDate = oldDate != null ? oldDate : //*в.1* Допускает возможность установления днём напоминания прошедший день (при необходимости отключить эту инициализацию и включить следующую (закомментированную))
                        Request.confirm("День напоминания не назначен. Желаете назначить его самостоятельно?" + //*в.1
                                "\n(В случае отказа днём напоминания будет назначен сегодняшний день - " //*в.1
                                + LocalDate.now().format(Time.D_MM_YYYY) + ")") ? //*в.1
                                Request.date(Request.ENTER_YEARDAY.replaceFirst("день", "новый день напоминания")) //*в.1
                                : LocalDate.now(); //*в.1

//  Исключает возможность назначения прошедшего дня днём напоминания (включить данное условие и отключить следующее за ним условие и вышестоящую инициализацию newDate)
/*
        if (oldDate != null)
            newDate = oldDate;
        else {
            if (Request.confirm("День напоминания не назначен. Желаете назначить его самостоятельно?" +
                    "\n(В случае отказа днём напоминания будет назначен сегодняшний день - "
                    + LocalDate.now().format(Time.D_MM_YYYY) + ")")) {
                do {
                    newDate = Request.date(Request.ENTER_YEARDAY.replaceFirst("день", "новый день напоминания"));
                    today = LocalDate.now();
                    if (newDate == null)
                        newDate = today;
                    if (newDate.isBefore(today))
                        System.out.println("Назначение прошедшего дня днём напоминания исключено.");
                } while (newDate.isAfter(today) || newDate.isEqual(today));
            } else
                newDate = LocalDate.now();
        }
*/ //*в.2

        if (newDate == null) //*в.1
            newDate = LocalDate.now(); //*в.1

        LocalTime newDayTime = Request.time(Request.ENTER_DAYTIME.replace("время", "новое время напоминания")
                + Request.OR_CANCEL);
        StringBuilder message = new StringBuilder("Время напоминания ");
        LocalTime oldDayTime = edited.getStartTime();
        LocalDateTime oldTime = edited.getStartDateTime();
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

        LocalDateTime oldTime = edited.getStartDateTime();

        LocalDate newDate = Request.date(Request.ENTER_YEARDAY.replaceFirst("календарный день", "новый день напоминания") +
                Request.OR_CANCEL.replace("отмены", "его обнуления"));
        LocalTime newDayTime = null;
        LocalDateTime newTime;

        StringBuilder message = new StringBuilder("Время напоминания ");
        if (newDate != null) {
            if (Request.confirm("Желаете изменить время напоминания:"))
                newDayTime = Request.time(Request.ENTER_DAYTIME.replace("время", "новое время напоминания")
                        + Request.OR_CANCEL);
            newTime = newDayTime == null ? LocalDateTime.of(newDate, edited.getStartTime()) : LocalDateTime.of(newDate, newDayTime);

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

    public static void changeTaskType(Task edited) {
        if (edited == null)
            return;

        Type newType = requestType();
        if (newType == edited.getType())
            return;

        tempJournal(edited.getType(), targetDay).remove(edited.getId());
        edited.setType(newType);
        tempJournal(newType, targetDay).put(edited.getId(), edited);
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

    public static void editTask() {
        Task edited = requestTaskPointer(ENTER_THE_TASKPOINTER);
        if (edited == null)
            return;

        ED_MENU.show(edited);
    }

    private static boolean applyChanges(String message, Task oldTask, Task newTask, String ask) {
        Text.printList(message, new String[]{
                "\tуже существующая задача: " + oldTask.getInfo(),
                "\tдобавляемая задача: " + newTask.getInfo()}, Text.PrintMode.BULLETED);
        return Request.confirm(ask);
    }

    public static void delTask() {
        delTask(null);
    }

    public static void delTask(Task deleted) {
        if (deleted == null)
            deleted = requestTaskPointer(ENTER_THE_TASKPOINTER);

        if (deleted != null)
            try {
                switch (deleted.getType()) {
                    case PERSONAL:
                        getPersonalTasks().remove(deleted.getId());
                        break;
                    case BUSINESS:
                        getBusinessTasks().remove(deleted.getId());
                        break;
                }
                Journal.getJournal().remove(deleted.getId());
                Text.print(1, "Задача " + deleted + " успешно удалена.");
            } catch (Exception e) {
                Text.print(1, "Удаление задачи " + deleted + " запрещено.");
            }
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
            task1 = new Task("Задача 1", Type.BUSINESS, aug10y23, h7m30);
            task2 = new Task("Задача 01", Type.BUSINESS, aug10y23, h7m30);
            task3 = new Task("Задача 3", Type.BUSINESS);
            task4 = new Task("Воооопсче не задача", mar10y23);
            task5 = new Task("Задача 546", mar09y23, h18m00);
            task6 = new Task("Задача 446", dec31y22, h4m30);
        } catch (CommandException e) {
            Text.printException(e);
        }
        Journal.newTask(task1, task2, task3, task4, task5, task6);
    }

    private static LocalDateTime requestDateTime() {
        return requestDateTime(null);
    }

    private static LocalDateTime requestDateTime(String message) {
        if (Data.isCorrect(message))
            System.out.println(message);

        LocalDate date = null;
        while (date == null) {
            date = Request.date("Укажите, пожалуйста, день:\n(в формате: деньМесяца.номерМесяца.год)");
            if (date == null)
                return null;
        }

        LocalTime time = null;
        if (Request.confirm("Назначить время напоминания?" +
                "\n(Если не указать время, оно будет назначено по умолчанию на " + getEventDayDefTime().format(Time.H_MM) + ")"))
            time = Request.time("Укажите, пожалуйста, время:\n(в формате: Час:Минута)");
        if (time == null)
            time = Journal.getEventDayDefTime();

        return LocalDateTime.of(date, time);
    }

    private static Type requestType() {
        String string;
        Type type = null;
        while (type == null) {
            Text.printList("\nВыберите один из нижеуказанных типов:" +
                            Request.OR_CANCEL.replace("отмены", "выбора типа по умолчанию - \"личная\")"),
                    Task.Type.numList(), Text.PrintMode.SIMPLE);
            string = Request.string();
            if (Data.isCorrect(string))
                type = Type.get(string);
            else return Type.PERSONAL;
            if (type == null)
                System.out.println("Указан неизвестный мне тип.");
        }

        return type;
    }

    private static RepeatMode requestRepeatMode() {
        String string;
        RepeatMode repeatMode;
        do {
            Text.printList("Выберите период повторения:" + Request.OR_CANCEL, RepeatMode.numList(), Text.PrintMode.SIMPLE);
            string = Request.string();
            if (!Data.isCorrect(string))
                return null;

            repeatMode = RepeatMode.get(string);
            if (repeatMode == null)
                System.out.println("Указанный период не знаком.");
            else
                switch (repeatMode) {
                    case OFF:
                        break;
                    case SINGLE:
                        if (TempTask.getStartTime() == null) {
                            System.out.println("Время напоминания не указано. Повтор напоминаний отключён.");
                            return RepeatMode.OFF;
                        }
                        do {
                            TempTask.setEndTime(requestDateTime("Сейчас следует указать день и время повторного напоминания."));

                            if (TempTask.getEndTime() == null)
                                return RepeatMode.OFF;

                            else if (TempTask.getEndTime().isAfter(LocalDateTime.now())
                                    && TempTask.getEndTime().isAfter(TempTask.getStartTime()))
                                TempTask.setPeriod(new Period(TempTask.getStartTime(), TempTask.getEndTime()));

                            else {
                                System.out.println("Возможность указать время повторения напоминания" +
                                        " ранее времени напоминания либо текущего времени исключена.");
                                TempTask.setEndTime(null);
                            }
                        } while (TempTask.getEndTime() == null);
                        break;

                    default:
                        do {
                            TempTask.setEndTime(requestDateTime("Когда остановить повторение напоминаний?" +
                                    "\n(Если не указать конкретный день, я отключу повторение напоминаний.)"));
                            if (TempTask.getEndTime() == null) {
                                TempTask.setRepeatMode(RepeatMode.OFF);
                                TempTask.setPeriod();
                                TempTask.setEndTime(null);
                                return RepeatMode.OFF;
                            }
                            if (TempTask.getEndTime().isAfter(LocalDateTime.now())
                                    && TempTask.getEndTime().isAfter(TempTask.getStartTime()))
                                TempTask.setPeriod();
                            else {
                                System.out.println("Возможность указать время Повторения напоминания" +
                                        " ранее времени Напоминания либо текущего времени исключена.");
                                TempTask.setEndTime(null);
                            }
                        } while (TempTask.getEndTime() == null);
                }
        } while (repeatMode == null);

        return repeatMode;
    }

    // newTask
    public static void newTask() {
        TempTask.reset();
        TempTask.setTitle(Request.string("\nВведите название (заголовок) задачи:\n(или \"пустую строку\" - для отмены)"));
        if (!Data.isCorrect(TempTask.getTitle()))
            return;

//      замена TempTask
/*
        Task newTask;
        try {
            newTask = new Task(Request.string("\nВведите название (заголовок) задачи:\n(или \"пустую строку\" - для отмены)"));
        } catch (CommandException e) {
            return;
        }
*/

        TempTask.setType(requestType());

        if (Request.confirm("Назначить день напоминания?"))
            TempTask.setStartTime(requestDateTime());

        if (TempTask.getStartTime() != null)
            if (Request.confirm("Установить повторение напоминания для этой задачи?"))
                TempTask.setRepeatMode(requestRepeatMode());
        if (TempTask.getRepeatMode() == RepeatMode.OFF)
            System.out.println("Повторение напоминаний о задаче"
                    + (Data.isCorrect(TempTask.getTitle()) ? " " + Text.aquo(TempTask.getTitle()) : "") + " отключено.");


        if (Request.confirm("Желаете добавить описание задачи?"))
            TempTask.setDescription(Request.string("Описание:"));

        try {
            newTask(new Task(TempTask.getTitle(), TempTask.getType(), TempTask.getStartTime(),
                    TempTask.getRepeatMode(), TempTask.getPeriod(), TempTask.getEndTime(), TempTask.getDescription()));
        } catch (
                CommandException e) {
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
                                "\n(В случае отказа введённые данные будут утрачены.)"))
                    continue;

            journal.put(newTask.getId(), newTask);
            newTask.checkTime();
            newTask.setOrdinal(++taskCount);
            sort();
            switch (newTask.getType()) {
                case PERSONAL:
                    Journal.getBusinessTasks().remove(newTask.getId());
                    Journal.getPersonalTasks().put(newTask.getId(), newTask);
                    break;
                case BUSINESS:
                    Journal.getPersonalTasks().remove(newTask.getId());
                    Journal.getBusinessTasks().put(newTask.getId(), newTask);
                    break;
            }

            if (newTask.getStatus() == TEMPORARY || newTask.getStatus() == REPEATED)
                if (newTask.getStartDateTime().isBefore(LocalDateTime.now()))
                    getArchiveTasks().put(newTask.getId(), newTask);
                else
                    getTemporaryTasks().put(newTask.getId(), newTask);
        }
    }

    public static void sort() {
        sort(tempJournal, getDefSortMode());
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

    private static Task replaceTask(Task replacing, String newDescription) {
        return replaceTask(null, replacing.getTitle(), replacing.getType(), replacing, replacing.getStartDate(), replacing.getStartTime(), newDescription);
    }

    //  используется?
    private static Task replaceTask(Task replaced, LocalTime newTime, Task replacing) {
        return replaceTask(replaced, replacing.getTitle(), replacing.getType(), replacing, replacing.getStartDate(), newTime, replacing.getDescription());
    }

    private static Task replaceTask(Task replaced, LocalDateTime newTime, Task replacing) {
        LocalDate newDate = newTime == null ? null : newTime.toLocalDate();
        LocalTime newDayTime = newTime == null ? null : newTime.toLocalTime();
        return replaceTask(replaced, replacing.getTitle(), replacing.getType(), replacing, newDate, newDayTime, replacing.getDescription());
    }

    private static Task replaceTask(Task replaced, String newTitle, Task replacing) {
        return replaceTask(replaced, newTitle, replacing.getType(), replacing, replacing.getStartDate(), replacing.getStartTime(), replacing.getDescription());
    }

    private static Task replaceTask(Task replaced, String newTitle, Type type, Task replacing,
                                    LocalDate newDate, LocalTime newDayTime, String newDescription) {
        if (replacing == null)
            return replaced;

        Task newTask;
        if (!Data.isCorrect(newTitle))
            newTitle = replacing.getTitle();
        if (newDate == null)
            newDayTime = null;
        if (newDayTime == null && newDate != null)
            newDayTime = replacing.getStartTime();

        try {
            newTask = new Task(newTitle, type, newDate, newDayTime, newDescription);
            if (newTask.equals(replacing))
                return replacing;

            if (replaced != null) {
                tempJournal(replaced.getType()).remove(replaced.getId());
                getJournal().remove(replaced.getId());
            }

            tempJournal(replacing.getType()/*, targetDay*/).remove(replacing.getId());
            getJournal().remove(replacing.getId());

            journal.put(newTask.getId(), newTask);
            tempJournal(newTask.getType()).put(newTask.getId(), newTask);
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

                journalTaskTime = journalTask.getStartDateTime();
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
                    && journalTask.getStartDate().isEqual(compared.getStartDate()) && journalTask.getStartTime().equals(compared.getStartTime()))
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

            if (!Data.isCorrect(tempJournal(showMode, targetDay)))
                return null;

            for (Task current :
                    tempJournal.values()) {
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


    //  убрать неиспользуемые
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
        return selection(taskPointer, tempJournal(showMode, targetDay));
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
            return journal;

        Map<Integer, Task> requested = new HashMap<>();
        LocalDate taskDate;
        for (Task task :
                journal.values()) {
            if (task != null) {
                taskDate = task.getStartDate();
//  ? проверка Дат Повторяемости
                if (task.getRepeatMode() != RepeatMode.OFF) {
                    for (LocalDate checkDate = taskDate; ) {

                    }
                } else if (taskDate != null && taskDate.isEqual(date)) {
                    if (showMode != null && !showMode.equals(task.getType()))
                        continue;
                    requested.put(task.getId(), task);
                }
            }
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
            if (task != null && task.getStartTime() == time)
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

    public static Map<Integer, Task> getJournal() {
        if (journal == null)
            journal = new HashMap<>();

        return journal;
    }

    public static Map<Integer, Task> getTemporaryTasks() {
        if (temporaryTasks == null)
            temporaryTasks = new HashMap<>();

        return temporaryTasks;
    }

    public static Map<Integer, Task> getArchiveTasks() {
        if (archiveTasks == null)
            archiveTasks = new HashMap<>();

        return archiveTasks;
    }

    public static Map<Integer, Task> getPersonalTasks() {
        if (personalTasks == null)
            personalTasks = new HashMap<>();

        return personalTasks;
    }

    public static Map<Integer, Task> getBusinessTasks() {
        if (businessTasks == null)
            businessTasks = new HashMap<>();

        return businessTasks;
    }


    public static LocalTime getEventDayDefTime() {
        return eventDayDefTime;
    }

    public static void setEventDayDefTime(LocalTime eventDayDefTime) {
        Journal.eventDayDefTime = eventDayDefTime;
    }

    public static SortMode getDefSortMode() {
        return defSortMode;
    }

    public static void setDefSortMode(SortMode defSortMode) {
        Journal.defSortMode = defSortMode == null ? SortMode.DESC : defSortMode;
    }

    public static Type showMode() {
        return showMode;
    }

    public static LocalDate targetDay() {
        return targetDay;
    }
}