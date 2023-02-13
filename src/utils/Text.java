package utils;

import java.util.List;

public class Text {

    protected static final String NO_DATA_TO_OUTPUT = "\nДанные для вывода отсутствуют.";

    public enum PrintModes {NO_PUNCTUATION, SIMPLE_LIST_PM, BULLETED_LIST, NUMBERED_LIST_PM;}


    //  Work with Letters and Words
    public static boolean isLetters(String string) {
        int noLetterMatchesCount = 0;
        char[] chars = string.toCharArray();
        for (char curSymbol :
                chars) {
            if (!Character.isLetter(curSymbol)) {
                ++noLetterMatchesCount;
            }
        }
        return noLetterMatchesCount == 0;
    }

    public static String startWithCapLetter(String string) {
        if (!Data.isCorrect(string)) {
            return string;
        }
        if (string.length() < 2) {
            return string.toUpperCase();
        }
        int index;
        String firstLetter = null;
        for (index = 0; index < string.length(); ++index) {
            firstLetter = string.substring(index, index + 1);
            if (isLetters(firstLetter)) {
                break;
            }
        }
        firstLetter = firstLetter.toUpperCase();
        if (index == 0) {
            return firstLetter + string.substring(1);
        } else {
            String firstSubstring = string.substring(0, index);
            return firstSubstring + firstLetter + string.substring(index + 1);
        }
    }


    //  Output

    //    * Lists outputting
    public static <A> void printList(A[] list) {
        printList(list, null, null);
    }

    public static <A> void printList(A[] list, PrintModes printModes) {
        printList(list, printModes, null);
    }
    public static <A> void printList(A[] list, String printTitle) {
        printList(list, null, printTitle);
    }

    public static <A> void printList(A[] list, PrintModes printMode, String printTitle) {
        if (!Data.isCorrect(list)) {
            System.out.println(NO_DATA_TO_OUTPUT);
            return;
        }
        if (printMode == null) {
            printMode = PrintModes.NO_PUNCTUATION;
        }
        if (Data.isCorrect(printTitle))
            System.out.println(printTitle);
        String listItem;
        char itemSeparator = ';';
        int indexOfLastNotNullObject = Data.getIndexOfLastNotNullObject(list);
        for (int index = 0; index < list.length; index++) {
            if (list[index] != null) {
                if (index == indexOfLastNotNullObject) {
                    itemSeparator = '.';
                }
                switch (printMode) {
                    case SIMPLE_LIST_PM:
                        listItem = String.format("%s%c", list[index], itemSeparator);
                        break;
                    case BULLETED_LIST:
                        listItem = String.format("* %s%c", list[index], itemSeparator);
                        break;
                    case NUMBERED_LIST_PM:
                        listItem = String.format("%d. %s%c", index + 1, list[index], itemSeparator);
                        break;
                    default:
                        listItem = String.format("%s", list[index]);
                }
                System.out.println(listItem);
            }
        }
    }

    public static void printList(List<?> list, PrintModes printMode, String printTitle) {
        if (!Data.isCorrect(list)) {
            System.out.println(NO_DATA_TO_OUTPUT);
            return;
        }
        if (printMode == null)
            printMode = PrintModes.NO_PUNCTUATION;
        if (Data.isCorrect(printTitle))
            System.out.println(printTitle);
        String listItem;
        char itemSeparator = ';';
        for (int index = 0; index < list.size(); ++index) {
            if (list.get(index) != null) {
                if (index == list.size() - 1)
                    itemSeparator = '.';
                switch (printMode) {
                    case SIMPLE_LIST_PM:
                        listItem = String.format("%s%c", list.get(index), itemSeparator);
                        break;
                    case BULLETED_LIST:
                        listItem = String.format("* %s%c", list.get(index), itemSeparator);
                        break;
                    case NUMBERED_LIST_PM:
                        listItem = String.format("%d. %s%c", index + 1, list.get(index), itemSeparator);
                        break;
                    default:
                        listItem = String.format("%s", list.get(index));
                }
                System.out.println(listItem);
            }
        }
    }

    public static void printList(List<?> list, PrintModes printMode) {
        printList(list, printMode, null);
    }

//    * Exceptions outputting

    public static String getExceptionClassTitle(Exception exception) {
        if (exception == null)
            return "Пустая ошибка.";

        StringBuilder message = new StringBuilder();
        switch (exception.getClass().getSimpleName()) {
            case "CommandException":
                message.append("Ошибка выбранной команды");
                break;
            default:
                message.append("Неизвестный тип ошибки");
        }
        message.append(": ").append(exception.getMessage());

        return message.toString();
    }

    public static void printException(Exception e) {
        printException(e, e.getClass().getSimpleName());
    }

    public static void printException(Exception e, String exceptionClass) {
        System.out.println("\n" + exceptionClass + ": " + e.getMessage());
    }
}
