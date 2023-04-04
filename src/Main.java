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

//        Journal.createTasksSet();
//        Journal.showTasks(Journal.getPersTasks());
//        Journal.showTasks(Journal.getBusiTasks());
    }

}