package utils;

import java.util.Map;
import java.util.HashMap;

import utils.Commands.*;

public class Menu {

    private String message, title;
    private Map<Integer, Command> commands;


    public Menu(Command... commands) {
        this(null, null, commands);
    }

    public Menu(String title, Command... commands) {
        this(null, title, commands);
    }

    public Menu(String message, String title, Command... commands) {
        setMessage(message);
        setTitle(title);
        setCommands(commands);
    }


    public void show() {
        Command command;
        do {
            if (!Data.isCorrect(commands))
                return;

            System.out.println();

            if (Data.isCorrect(message))
                System.out.println("\n" + message);

            Text.printList(Commands.toStrings(this), Text.PrintModes.SIMPLE_LIST_PM, title);

            command = Command.request();

            if (command == null)
                System.out.println("\n" + CommandException.UNKNOWN_COMMAND_);
            else if (command != Command.SHUTDOWN)
                command.execute();

        } while (command != Command.SHUTDOWN);
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message == null ? "" : message;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title == null ? "" : title;
    }

    public Map<Integer, Command> getCommands() {
        return commands;
    }

    public final void setCommands(Command... commands) {
        if (!Data.isCorrect(commands))
            this.commands = new HashMap<>(Map.of(Command.SHUTDOWN.getOrdinalNumber(), Command.SHUTDOWN));

        else {
            this.commands = new HashMap<>();
            addCommands(commands);
        }
    }

    public final void addCommands(Command... commands) {
        if (!Data.isCorrect(commands))
            return;

        if (this.commands == null)
            this.commands = new HashMap<>();

        for (Command command :
                commands) {
            if (command != null)
                this.commands.put(command.getOrdinalNumber(), command);
        }
    }
}
