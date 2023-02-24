import utils.Commands;
import utils.Menu;

import java.util.Arrays;


public class Main {

    public static void main(String[] args) {

        System.out.println("\nДобро пожаловать в «Ежедневник»! ☺");

        Menu mainMenu = new Menu("Что предпринять?", Commands.getAll());

        mainMenu.show();
    }
}