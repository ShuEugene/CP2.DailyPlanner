package utils;

public abstract class Paragraph {

    private static StringBuilder content;

    public static String withMargins(String message) {
        return withMargins(0, message);
    }

    public static String withMargins(int lines) {
        return withMargins(lines, null);
    }

    public static String withMargins(int upperLines, String message) {
        return withMargins(upperLines, message, 0);
    }

    public static String withMargins(int upperLines, String string, int lowerLines) {
        if (!Data.isCorrect(string))
            if (upperLines > 0)
                return "\n".repeat(upperLines);
            else return "";

        if (upperLines < 1 && lowerLines < 1)
            return string;

        if (upperLines > 0)
            content = new StringBuilder("\n".repeat(upperLines) + string);
        if (lowerLines > 0)
            content.append("\n".repeat(lowerLines));
        return content.toString();
    }
}
