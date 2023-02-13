import java.util.Scanner;

import utils.Choice.Command;
import utils.Choice.Command.CommandException;


public class Main {

    public static void main(String[] args) {

        System.out.println("\nДобро пожаловать в «Ежедневник».");
        start();

    }

    private static void start() {
//        try (Scanner scanner = new Scanner(System.in)) {

            Command command;
            do {
/*
                Text.printList(Command.list(), PrintModes.SIMPLE_LIST_PM, "\nЧто предпринять?");

                command = Command.get(scanner.nextLine());
*/
                command = Command.request("Что предпринять?", Command.getAll());

                if (command == null)
                    System.out.println("\n" + CommandException.UNKNOWN_COMMAND_);
                else if (command != Command.SHUTDOWN)
                    command.execute();

            } while (command != Command.SHUTDOWN);
//        }
    }

    private static void inputTask(Scanner scanner) {
        System.out.print("Введите название задачи: ");
        String taskName = scanner.next();
        // todo
    }
}