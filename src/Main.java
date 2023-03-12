import planner.Journal;
import planner.Task;
import utils.Commands;
import utils.Menu;
import utils.Text;

import java.time.LocalDateTime;
import java.util.Arrays;


public class Main {

    public static void main(String[] args) {

        System.out.println("\nДобро пожаловать в «Ежедневник»! ☺");

        Journal.MAIN_MENU.show();

//        Journal.newTask();
//        Task[] tasks = Journal.getJournal().values().toArray(new Task[0]);
//        Arrays.sort(tasks, Journal.ORD);
//        Text.printList(Journal.taskList(tasks), Text.PrintModes.SIMPLE_LIST_PM, "\nTaskList:");
    }
}