package utils;

import java.util.*;

import planner.Journal;
import planner.Task;

public abstract class Commands {

    public static class CommandException extends Exception {

        public static final String UNKNOWN_COMMAND_ = "С указанным действием не знаком.";
        public static final String UNTITLED_TASK_ = "Название (заголовок) задачи не указан.";

        public CommandException() {
        }

        public CommandException(String message) {
            super(message);
        }
    }

    public static final String[] CANCEL_COMMAND_NAMES_ = {"CANCEL", "QUIT", "EXIT", "SHUTDOWN"};
    public static final String[] CANCEL_COMMAND_ALTER_NAMES_ = {"Выход", "Завершение работы"};

    private static final String UNKNOWN_ = "<не указано>";
    private static final String CANCEL_ = "Отмена", CANCELT_ = "Cancel";
    private static final String LIST_EMPTY_ = "Перечень команд пока ещё пуст.";

    public enum Command {
//  Соглашение:
/*
    Команда "CANCEL" ("SHUTDOWN" / "EXIT" / "QUIT") всегда должна быть заключающей
    (потому как логика этого перечисления всегда обнуляет её порядковый номер)
*/

        ADD("Добавить задачу"),
        EDIT("Поправить задачу"),
        REN("Переименовать", "Rename"),
        CHANGET("Сменить Тип", "Change Type"),
        EDDATE("Изменить День напоминания", "Edit Date"),
        EDTIME("Изменить Время напоминания", "Edit Time"),
        EDREP("Изменить Повторяемость", "Edit Repeatability"),
        EDDESCR("Изменить Описание", "Edit Description"),
        DELETE("Удалить задачу"),
        SHOW("Показать задачи", "Show Tasks"),
        SHOWTI("Cведения по задаче", "Show task Info"),
        TEST("Создать тестовые экземпляры задач"),
        CANCEL(CANCEL_);

        public static Command request() {
            return request(Command.values());
        }

        public static Command request(Command[] commands) {
            return get(new Scanner(System.in).nextLine(), commands);
        }

        public static Command get(String string, Command[] commands) {
            if (noCommands() || string == null)
                return null;

            if (!Data.isCorrect(string)
                    || CANCEL.getTitle().equalsIgnoreCase(string)
                    || Arrays.toString(CANCEL_COMMAND_NAMES_).toLowerCase().contains(string.toLowerCase())
                    || Arrays.toString(CANCEL_COMMAND_ALTER_NAMES_).toLowerCase().contains(string.toLowerCase()))
                return CANCEL;

            for (Command command :
                    commands) {
                if (string.equals(Integer.toString(command.ordinal))
                        || command.name().equalsIgnoreCase(string)
                        || command.getTitle().equalsIgnoreCase(string)
                        || command.getTranslate().equalsIgnoreCase(string))
                    return command;
            }

            return null;
        }


        private final String title, translate;
        private int ordinal;


        Command(String title) {
            this(title, "");
        }

        Command(String title, String translate) {
            this.ordinal = Arrays.toString(CANCEL_COMMAND_NAMES_).contains(this.name()) ? 0 : ordinal() + 1;

            this.title = ordinal == 0 ? CANCEL_
                    : Data.isCorrect(title) ? title : "<команда " + ordinal + ">";

            this.translate = ordinal == 0 ? CANCELT_
                    : Data.isCorrect(translate) ? translate : ""/*"<command " + ordinal + ">"*/;
        }


        public final void execute() {
            execute(null);
        }

        public final Task execute(Task operated) {
            try {
                switch (this) {
                    case REN:
                    case EDDATE:
                    case EDTIME:
                    case EDREP:
                    case EDDESCR:
                    case DELETE:
                        if (!Data.isCorrect(Journal.tempJournal(Journal.showMode(), Journal.targetDay()))) {
                            System.out.println(Journal.IS_EMPTY);
                            return null;
                        }

                        if (operated == null)
                            operated = Journal.requestTaskPointer(Journal.ENTER_THE_TASKPOINTER);
                        if (operated == null)
                            return null;
                }

                switch (this) {
                    case ADD:
                        Journal.newTask();
                        break;
                    case EDIT:
                        Journal.editTask();
                        break;
                    case REN:
                        operated = Journal.renTask(operated);
                        break;
                    case CHANGET:
                        Journal.changeTaskType(operated);
                        break;
                    case EDDATE:
                        operated = Journal.editTaskDate(operated);
                        operated.setStatus();
                        break;
                    case EDTIME:
                        operated = Journal.editTaskTime(operated);
                        break;
                    case EDREP:
                        Journal.changeTaskRepeat(operated);
                        operated.setStatus();
                        break;
                    case EDDESCR:
                        Journal.editTaskDescr(operated);
                        break;
                    case SHOW:
                        Journal.show();
                        break;
                    case SHOWTI:
                        Journal.showTaskInfo();
                        break;
                    case DELETE:
                        Journal.delTask(operated);
                        break;
                    case TEST:
                        Journal.createTasksSet();
                        break;
                    default:
                        throw new CommandException(CommandException.UNKNOWN_COMMAND_);
                }
            } catch (CommandException e) {
                Text.printException(e, "Ошибка выполнения команды");
                return null;
            }

            Journal.sort();
            return operated;
        }


        public final String ordinalString() {
            return ordinal + ". " + this;
        }

        public final String getTitle() {
            if (!Data.isCorrect(title))
                return UNKNOWN_;
            return title;
        }

        public final String getTranslate() {
            if (!Data.isCorrect(translate))
                return UNKNOWN_;
            return translate;
        }

        public final int getOrdinal() {
            return ordinal;
        }

        private final void setOrdinal(int number) {
            this.ordinal = Arrays.toString(CANCEL_COMMAND_NAMES_).contains(this.name()) ? 0 : number;
        }

        @Override
        public final String toString() {
            StringBuilder string = new StringBuilder(title);

            if (this == CANCEL)
                string.append(" (").append(Arrays.toString(CANCEL_COMMAND_ALTER_NAMES_)
                        .replace("[", "").replace("]", "")).append(")");

            string.append(" [").append(name().toLowerCase()).append("]");

            if (this == CANCEL)
                string.replace(string.length() - (this.name().length() + 2), string.length(),
                        Arrays.toString(CANCEL_COMMAND_NAMES_).toLowerCase());

            if (Data.isCorrect(translate))
                string.append(" {").append(translate).append("}");

            return string.toString();
        }
    }


    public static void reassignOrdinal(Map<Integer, Command> commands) {
        reassignOrdinal(commands.values().toArray(new Command[0]));
    }

    public static void reassignOrdinal(Command... commands) {
        int count = 0;
        for (Command command :
                commands) {
            if (command != null)
                if (command != Command.CANCEL)
                    command.setOrdinal(++count);
                else command.setOrdinal(0);
        }
    }


    public static String[] toStrings() {
        return toStrings(getAll());
    }

    public static String[] toStrings(Menu menu) {
        return toStrings(menu.getCommands());
    }

    public static String[] toStrings(Map<Integer, Command> commandMap) {
        if (!Data.isCorrect(commandMap))
            return new String[0];

        int listLength = Data.notNullObjectsNumber(commandMap.values().toArray());
        if (listLength < 1)
            return new String[0];

        if (listLength < 2)
            return toStrings(justShutdown());

        return toStrings(commandMap.values().toArray(new Command[0]));
    }

    public static String[] toStrings(Command... commands) {
        int length = Data.notNullObjectsNumber(commands);
        if (length < 1)
            return new String[]{LIST_EMPTY_};
        if (length < 2)
            return new String[]{Command.CANCEL.ordinalString()};

        String[] strings = new String[length];
        int index, count = 0;
        for (index = 0; index < length; ++index) {
            if (commands[index] != null) {
                if (Arrays.toString(CANCEL_COMMAND_NAMES_).contains(commands[index].name()))
                    continue;
                strings[count++] = commands[index].ordinalString();
            }
        }
        strings[length - 1] = Command.CANCEL.ordinalString();

        return strings;
    }

    public static Command[] getAll() {
        return getSelected(Command.values());
    }

    private static Command[] getSelected(Command... commands) {
        if (!Data.isCorrect(commands))
            return new Command[0];

        int length = Data.notNullObjectsNumber(commands);
        if (length < 1)
            return new Command[0];

        if (length < 2)
            return justShutdown();

        Command[] selected = new Command[length];
        int index, count = 0;
        for (index = 0; index < commands.length - 1; ++index) {
            if (commands[index] != null) {
                if (Arrays.toString(CANCEL_COMMAND_NAMES_).contains(commands[index].name()))
                    continue;
                selected[count] = commands[index];
                selected[count].ordinal = ++count;
            }
        }
        selected[length - 1] = Command.CANCEL;
        selected[length - 1].ordinal = 0;
        return selected;
    }

    public static Command[] justShutdown() {
        Command[] justShutdown = new Command[]{Command.CANCEL};
        justShutdown[0].ordinal = 0;
        return justShutdown;
    }

    private static boolean noCommands() {
        try {
            return Command.values().length == 0;
        } catch (Exception e) {
            return true;
        }
    }
}
