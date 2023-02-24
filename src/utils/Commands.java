package utils;

import java.util.*;

import planner.Journal;
import planner.Task;

public abstract class Commands {

    public static class CommandException extends Exception {

        public static final String UNKNOWN_COMMAND_ = "С указанным действием не знаком.";
        public static final String UNTITLED_TASK_ = "Заголовок задачи должен быть указан.";

        public CommandException() {
        }

        public CommandException(String message) {
            super(message);
        }
    }

    public static final List<String> SHUTDOWN_COMMANDS_NAMES_
            = new ArrayList<>(List.of("SHUTDOWN", "EXIT", "QUIT", "ВЫХОД"));
    private static final String UNKNOWN_ = "<не указано>";
    private static final String SHUTDOWN_ = "Завершение работы";
    private static final String LIST_EMPTY_ = "Перечень команд пока ещё пуст.";

    public enum Command {
//  Соглашение:
/*
    Команда "SHUTDOWN" ("EXIT" / "QUIT") всегда должна быть заключающей
    (потому как логика этого перечисления всегда обнуляет её порядковый номер)
*/

        ADD("Добавить задачу"),
        SBD("Показать задачи по календарному дню", "Show by day"),
        REN("Переименовать задачу", "Rename"),
        DEL("Удалить задачу", "Delete"),
        SHALL("Показать все задачи", "Show all"),
        SHOW("Показать все сведения по определённой задаче"),
        SHUTDOWN(SHUTDOWN_);

        public static Command request() {
            return get(new Scanner(System.in).nextLine());
        }

        public static Command get(String string) {
            if (isEmpty())
                return null;

            if (!Data.isCorrect(string)
                    || string.equalsIgnoreCase("exit")
                    || string.equalsIgnoreCase("выход"))
                return SHUTDOWN;

            for (Command command :
                    Command.values()) {
                if (string.equals(Integer.toString(command.ordinalNumber))
                        || command.name().equalsIgnoreCase(string)
                        || command.getTitle().equalsIgnoreCase(string)
                        || command.getTranslate().equalsIgnoreCase(string))
                    return command;
            }

            return null;
        }


        private final String title, translate;
        private int ordinalNumber;


        Command(String title) {
            this(title, "");
        }

        Command(String title, String translate) {
            if (Data.isCorrect(title))
                this.title = title;
            else if (Command.values().length < 2)
                this.title = SHUTDOWN_;
            else this.title = UNKNOWN_;

            this.translate = translate;

            setOrdinalNumber();
        }


        public final void execute() {
            Task task1;
            Task task1_1;
            Task task2;
            Task task3;
            Task task4;
            String title1 = "Задача 1";
            String title2 = "Задача 01";
            try {
                switch (this) {
                    case ADD:
                        task1 = new Task(title1);
                        task1_1 = new Task(title1);
                        task2 = new Task("Задача 546");
                        task3 = new Task("");
                        task4 = new Task("Вообще не задача");
                        System.out.println("task1 = " + task1 + "; id = " + task1.getId());
                        Journal.show();
                        break;
                    case SBD:
                        Journal.showTasks(Journal.selection(Data.dateRequest("\nПо какому дню следует показать задачи?")));
                        break;
                    case REN:
                        //  ? сопоставлять Задачи по Заголовку и Времени или по ИД
                        task1 = new Task(title1);
                        task1.setTitle(title2);
                        System.out.println("task1 = " + task1 + "; id = " + task1.getId());
                        Journal.show();
                        break;
                    case SHALL:
                        Journal.show();
                        break;
                    case SHOW:
                        Journal.showTasks(Journal.selection(Data.request("\nПо какой задаче следует вывести сведения?" +
                                "\n(следует указать порядковый номер либо заголовок)")));
                        break;
                    case DEL:
                        break;
                    default:
                        System.out.println("\n" + CommandException.UNKNOWN_COMMAND_);
                }
            } catch (CommandException e) {
                Text.printException(e, "Ошибка выполнения команды");
            }
        }


        public final String ordinalString() {
            StringBuilder string = new StringBuilder(Integer.toString(ordinalNumber));
            string.append(". ").append(title).append(" [").append(name().toLowerCase()).append("]");
            if (this == SHUTDOWN)
                string.replace(string.length() - (this.name().length() + 2), string.length(),
                        Arrays.toString(SHUTDOWN_COMMANDS_NAMES_.toArray()).toLowerCase());
//                string.replace(string.length() - 1, string.length(), " / exit / выход]");

            if (Data.isCorrect(translate))
                string.append(" {").append(translate).append("}");

            return string.toString();
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

        public final int getOrdinalNumber() {
            return ordinalNumber;
        }

        private final void setOrdinalNumber() {
            if (isEmpty())
                this.ordinalNumber = 1;
            else
                this.ordinalNumber = Command.values().length;
            if (SHUTDOWN_COMMANDS_NAMES_.contains(this.name()))
                this.ordinalNumber = 0;
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

        int listLength = Data.getNotNullObjectsNumber(commandMap.values().toArray());
        if (listLength < 1)
            return new String[0];

        if (listLength < 2)
            return toStrings(justShutdown());

        return toStrings(commandMap.values().toArray(new Command[0]));
    }

    public static String[] toStrings(Command... commands) {
        int length = Data.getNotNullObjectsNumber(commands);
        if (length < 1)
            return new String[]{LIST_EMPTY_};
        if (length < 2)
            return new String[]{Command.SHUTDOWN.ordinalString()};

        String[] strings = new String[length];
        int index, count = 0;
        for (index = 0; index < length; ++index) {
            if (commands[index] != null) {
                if (SHUTDOWN_COMMANDS_NAMES_.contains(commands[index].name()))
                    continue;
                strings[count++] = commands[index].ordinalString();
            }
        }
        strings[length - 1] = Command.SHUTDOWN.ordinalString();

        return strings;
    }

    public static Command[] getAll() {
        return getSelected(Command.values());
    }

    private static Command[] getSelected(Command... commands) {
        if (!Data.isCorrect(commands))
            return new Command[0];

        int length = Data.getNotNullObjectsNumber(commands);
        if (length < 1)
            return new Command[0];

        if (length < 2)
            return justShutdown();

        Command[] selected = new Command[length];
        int index, count = 0;
        for (index = 0; index < commands.length - 1; ++index) {
            if (commands[index] != null) {
                if (SHUTDOWN_COMMANDS_NAMES_.contains(commands[index].name()))
                    continue;
                selected[count] = commands[index];
                selected[count].ordinalNumber = ++count;
            }
        }
        selected[length - 1] = Command.SHUTDOWN;
        selected[length - 1].ordinalNumber = 0;
        return selected;
    }

    public static Command[] justShutdown() {
        Command[] justShutdown = new Command[]{Command.SHUTDOWN};
        justShutdown[0].ordinalNumber = 0;
        return justShutdown;
    }

    private static boolean isEmpty() {
        try {
            return Command.values().length == 0;
        } catch (Exception e) {
            return true;
        }
    }
}
