package utils;

import java.util.ArrayList;
import java.util.List;

public class Choice {

    public static final List<String> SHUTDOWN_COMMANDS_NAMES_
            = new ArrayList<>(List.of("SHUTDOWN", "EXIT", "QUIT"));
    private static final String SHUTDOWN_ = "Завершение работы";

    public enum Command {
//  Договор
/*
    Команда "SHUTDOWN" ("Завершение работы" / "Выход") всегда должна быть заключающей
    (потому как логика этого перечисления всегда обнуляет её порядковый номер)
*/

        ADD("Добавить задачу"),
        GET("Получить задачу на указанный день"),
        DEL("Удалить задачу"),
        SHUTDOWN(SHUTDOWN_);

        public static class CommandException extends Exception {

            public static final String UNKNOWN_COMMAND_ = "С указанным действием не знаком.";

            public CommandException() {
            }

            public CommandException(String message) {
                super(message);
            }
        }

        private static final String LIST_EMPTY_ = "Перечень команд пока ещё пуст.";

        public static Command get(String string) {
            if (Command.isEmpty())
                return null;

            if (!Data.isCorrect(string)
                    || string.equalsIgnoreCase("exit")
                    || string.equalsIgnoreCase("выход"))
                return SHUTDOWN;

            for (Command command :
                    Command.values()) {
                if (string.equals(Integer.toString(command.number))
                        || command.name().equalsIgnoreCase(string)
                        || command.title.equalsIgnoreCase(string))
                    return command;
            }

            return null;
        }

        public static Command byNumber(int number) {
            if (Command.isEmpty() || number < 0 || number > Command.values().length - 1)
                return null;

            for (Command command :
                    Command.values()) {
                if (command.number == number)
                    return command;
            }

            return null;
        }

        public static String[] list() {
            return list(getAll());
        }

        public static String[] list(Command... commands) {
            if (!Data.isCorrect(commands))
                return new String[]{LIST_EMPTY_};

            String[] list = new String[commands.length];
            int index = 0;

            for (Command command :
                    commands) {
                if (Data.isCorrect(command.title)) {
                    StringBuilder listItem = new StringBuilder(Integer.toString(command.number));
                    listItem.append(". ").append(command.title).append(" [").append(command.name().toLowerCase()).append("]");
                    if (command == SHUTDOWN)
                        listItem.replace(listItem.length() - 1, listItem.length(), " / exit / выход]");
                    list[index++] = listItem.toString();
                }
            }

            if (index > 0)
                return list;
            else
                return new String[]{LIST_EMPTY_};
        }

        private static Command[] getAll() {
            return getSelected(Command.values());
        }

        private static Command[] getSelected(Command... commands) {
            if (!Data.isCorrect(commands))
                return new Command[0];

            Command[] list = new Command[commands.length];
            int count = 0;

            for (Command command :
                    commands) {
                if (Data.isCorrect(command.title)) {
                    list[count] = command;
                    list[count].number = ++count;
                    if (SHUTDOWN_COMMANDS_NAMES_.contains(command.name()))
                        command.number = 0;
                }
            }

            return list;
        }

        private static boolean isEmpty() {
            try {
                return Command.values().length == 0;
            } catch (Exception e) {
                return true;
            }
        }


        private final String title;
        private int number;


        Command(String title) {
            if (Data.isCorrect(title))
                this.title = title;
            else if (Command.values().length < 2)
                this.title = SHUTDOWN_;
            else this.title = "<не указано>";

            if (Command.isEmpty())
                this.number = 1;
            else
                this.number = Command.values().length;
            if (SHUTDOWN_COMMANDS_NAMES_.contains(this.name()))
                this.number = 0;

/*
            try {
            } catch (Exception e) {
                this.number = 1;
            } finally {
                if (SHUTDOWN_COMMANDS_NAMES_.contains(this.name()))
                    this.number = 0;
            }
*/
        }


        public void execute() {
            switch (this) {
                case ADD:
                    break;
                case GET:
                    break;
                case DEL:
                    break;
                default:
                    System.out.println("\n" + CommandException.UNKNOWN_COMMAND_);
            }
        }
    }
}
