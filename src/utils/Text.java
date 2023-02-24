package utils;

import java.util.List;
import java.util.Map;

public class Text {

    protected static final String NO_DATA_TO_OUTPUT = "\nДанные для вывода отсутствуют.";

    public enum PrintModes {NO_PUNCTUATION, SIMPLE_LIST_PM, BULLETED_LIST, NUMBERED_LIST_PM;}


    //  Quot
    public static String aquo(String string) {
        return "«" + string + "»";
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
        printList(mapStrings(map), null, null);
    }

    public static void printList(Map<?, ?> map, String title) {
        printList(mapStrings(map), null, title);
    }

    public static void printList(Map<?, ?> map, PrintModes printModes) {
        printList(mapStrings(map), printModes, null);
    }

    public static void printList(String delimiter, Map<?, ?> map) {
        printList(mapStrings(map, delimiter), null, null);
    }

    public static void printList(String delimiter, Map<?, ?> map, PrintModes printModes) {
        printList(mapStrings(map, delimiter), printModes, null);
    }

    public static void printList(String delimiter, Map<?, ?> map, String title) {
        printList(mapStrings(map, delimiter), null, title);
    }

    public static void printList(String delimiter, Map<?, ?> map, PrintModes printModes, String title) {
        printList(mapStrings(map, delimiter), printModes, title);
    }

    public static void printList(List<?> list, PrintModes printMode) {
        printList(list, printMode, null);
    }

    public static void printList(List<?> list, PrintModes printMode, String printTitle) {
        printList(list.toArray(), printMode, printTitle);
    }

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
        if (!Data.isCorrect(map) || (length = Data.getNotNullObjectsNumber(map.values().toArray())) < 1)
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
