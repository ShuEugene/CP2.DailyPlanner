package utils;

import java.util.*;

import planner.Task;
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
        show(null);
    }

    public void show(Task operated) {
        if (!Data.isCorrect(commands))
            return;

        Commands.reassignOrdinal(commands);
        Command command;

        do {
            System.out.println();
            if (Data.isCorrect(message))
                System.out.println("\n" + message);
            Text.printList(title, Commands.toStrings(this), Text.PrintMode.SIMPLE);

            command = Command.request(commands.values().toArray(new Command[0]));

            if (command == null)
                System.out.println("\n" + CommandException.UNKNOWN_COMMAND_);
            else if (command != Command.CANCEL)
                operated = command.execute(operated);

        } while (command != Command.CANCEL);
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
            this.commands = new HashMap<>(Map.of(Command.CANCEL.getOrdinal(), Command.CANCEL));

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

        else {
            List<Command> newCommandList = new ArrayList<>(List.of(this.commands.values().toArray(new Command[0])));
            newCommandList.addAll(List.of(commands));
            Command[] fullList = newCommandList.toArray(new Command[0]);
            Commands.reassignOrdinal(fullList);
        }

        for (Command command :
                commands) {
            if (command != null)
                this.commands.put(command.getOrdinal(), command);
        }
    }
}
