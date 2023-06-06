package utils;

import java.util.List;
import java.util.Map;

public class Text {

    protected static final String NO_DATA_TO_OUTPUT = "\nДанные для вывода отсутствуют.";

    public enum PrintMode {NO_PUNCT, SIMPLE, BULLETED, NUMBERED;}

    public enum WordForm {SINGULAR, PLURAL;}

    //  Quot
    public static String aquo(String string) {
        return Data.isCorrect(string) ? "«" + string + "»" : "";
    }

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


    //  Strings outputting

    public static void printList(Map<?, ?> map) {
        printList(null, mapStrings(map), null);
    }

    public static void printList(Map<?, ?> map, String title) {
        printList(null, mapStrings(map), null);
    }

    public static void printList(Map<?, ?> map, PrintMode printMode) {
        printList(null, mapStrings(map), printMode);
    }

    public static void printList(String delimiter, Map<?, ?> map) {
        printList(null, mapStrings(map, delimiter), null);
    }

    public static void printList(String delimiter, Map<?, ?> map, PrintMode printMode) {
        printList(null, mapStrings(map, delimiter), printMode);
    }

    public static void printList(String title, String delimiter, Map<?, ?> map) {
        printList(title, mapStrings(map, delimiter), null);
    }

    public static void printList(String delimiter, Map<?, ?> map, PrintMode printMode, String printTitle) {
        printList(printTitle, mapStrings(map, delimiter), printMode);
    }

    public static void printList(List<?> list, PrintMode printMode) {
        printList(null, list, printMode);
    }

    public static void printList(String printTitle, List<?> list, PrintMode printMode) {
        printList(printTitle, list.toArray(), printMode);
    }

    public static <A> void printList(A[] list) {
        printList(null, list, null);
    }

    public static <A> void printList(A[] list, PrintMode printMode) {
        printList(null, list, printMode);
    }

    public static <A> void printList(String printTitle, A[] list) {
        printList(printTitle, list, null);
    }

    public static <A> void printList(String printTitle, A[] list, PrintMode printMode) {
        if (!Data.isCorrect(list = Data.getNotNullObjects(list))) {
            System.out.println(NO_DATA_TO_OUTPUT);
            return;
        }

        if (Data.isCorrect(printTitle))
            System.out.println(printTitle);

        if (printMode == null)
            printMode = PrintMode.NO_PUNCT;
        char punctMark = ';';
        StringBuilder listItem;
        String itemString;

        for (int index = 0; index < list.length; index++) {
            if (list[index] != null) {
                listItem = new StringBuilder();
                itemString = list[index].toString();

                if (list[index].toString().startsWith("\t")) { // optionally
                    itemString = itemString.replace("\t", " ");
                    listItem.append("\t");
                }

                if (index == list.length - 1)
                    punctMark = '.';

                switch (printMode) {
                    case SIMPLE:
                        listItem.append(itemString);
                        break;
                    case BULLETED:
                        listItem.append("* ").append(itemString);
                        break;
                    case NUMBERED:
                        listItem.append(index + 1).append(itemString);
                        break;
                    default:
                        listItem.append(itemString);
                }

                if (List.of(PrintMode.values()).contains(printMode))
                    listItem.append(punctMark);

                System.out.println(listItem);
            }
        }
    }

    public static void print(int lines) {
        print(lines, null);
    }

    public static void print(String message) {
        print(0, message);
    }

    public static void print(int upperLines, String message) {
        print(upperLines, message, 0);
    }

    public static void print(int upperLines, String message, int linesDown) {
        System.out.println(Paragraph.withMargins(upperLines, message, linesDown));
    }

    //  ...toStrings

    public static <K, V> String[] mapStrings(Map<K, V> map) {
        return mapStrings(map, null);
    }

    public static <K, V> String[] mapStrings(Map<K, V> map, String delimiter) {
        if (!Data.isCorrect(map))
            return new String[0];

        if (delimiter == null)
            delimiter = " ";

        String[] strings = new String[map.size()];
        int index = 0;
        for (Map.Entry<K, V> entry :
                map.entrySet()) {
            strings[index++] = entry.getKey() + delimiter + entry.getValue();
        }

        return strings;
    }

    public static <K, V> String[] mapValuesStrings(Map<K, V> map) {
        int length = 0;
        if (!Data.isCorrect(map) || (length = Data.notNullObjectsNumber(map.values().toArray())) < 1)
            return new String[0];

        String[] strings = new String[length];
        int index = 0;
        for (V value :
                map.values()) {
            if (value != null)
                strings[index++] = value.toString();
        }

        return strings;
    }

    public static <K, V> String[] mapKeysStrings(Map<K, V> map) {
        if (!Data.isCorrect(map))
            return new String[0];

        String[] strings = new String[map.size()];
        int index = 0;
        for (K key :
                map.keySet()) {
            strings[index++] = key.toString();
        }

        return strings;
    }


    //  Exceptions outputting

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
