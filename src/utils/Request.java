package utils;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.Year;
import java.time.temporal.ChronoUnit;
import java.util.*;

public class Request {
    public static final String INCORRECT_VALUE = "Введённое значение не соответствует допустимому.";
    public static final String ENTER_DAYTIME = "Укажите время (в формате - час:минута):";
    public static final String ENTER_YEARDAY = "Укажите календарный день (в формате - день.месяц.год):" +
            "\n(можно использовать слова \"сегодня\", \"завтра\" и т.п.)";
    public static final String OR_CANCEL = "\n(или нажмите «Enter» для отмены)";
    private static final String[][] CONFIRM_POINTERS = {
            {"да", "нет"}, {"yes", "no"},
            {"д", "н"}, {"y", "n"},
            {"+", "-"}, {"1", "0"}};

    public static boolean confirm() {
        return confirm(null);
    }

    public static boolean confirm(String ask) {
        List<String> validAnswers = yesNoAnswers();
        if (!Data.isCorrect(validAnswers))
            return false;

        if (Data.isCorrect(ask))
            System.out.println(ask);

        String answer = null;
        boolean wrongAnswer = true;
        while (wrongAnswer) {
            System.out.println(yesNoAnswersStr());
            answer = string();
            if (!Data.isCorrect(answer))
                return false;

            wrongAnswer = !validAnswers.contains(answer);
            if (wrongAnswer)
                System.out.println("Введите один из допустимых ответов или нажмите «Enter» для отмены.");
        }

        validAnswers = yesAnswers();
        if (Data.isCorrect(validAnswers))
            return validAnswers.contains(answer);
        else return false;
    }

    public static String yesNoAnswersStr() {
        List<String> answers = yesNoAnswers();
        if (!Data.isCorrect(answers))
            return null;

        StringBuilder words = new StringBuilder("(");
        for (int index = 0; index < answers.size(); ++index) {
            String word = answers.get(index);
            if (word != null) {
                words.append(word);
                if (index < answers.size() - 1)
                    if (index % 2 == 0)
                        words.append("/");
                    else words.append("; ");
            }
        }

        words.append(")");
        return words.toString();
    }

    public static List<String> yesAnswers() {
        List<String> yesNoAnswers = yesNoAnswers();
        if (!Data.isCorrect(yesNoAnswers))
            return null;

        List<String> answers = new ArrayList<>(yesNoAnswers.size() / 2);
        for (int index = 0; index < yesNoAnswers.size(); ++index) {
            if (yesNoAnswers.get(index) != null && index % 2 == 0)
                answers.add(yesNoAnswers.get(index));
        }

        return answers;
    }

    public static List<String> yesNoAnswers() {
        if (Data.notNullObjectsNumber(CONFIRM_POINTERS) <= 0)
            return null;
        return Arrays.asList(Arrays.deepToString(CONFIRM_POINTERS)
                .replace("[", "").replace("]", "").split(", "));
    }

    public static LocalTime time() {
        return time(null);
    }

    public static LocalTime time(String ask) {
        String requestString = null;
        LocalTime time = null;
        while (time == null) {
            try {
                requestString = string(ask);
                if (!Data.isCorrect(requestString) || requestString.equals("0"))
                    return null;

                time = LocalTime.parse(requestString, Time.H_MM);

            } catch (Exception e) {
                if (!Data.isCorrect(requestString))
                    return null;
                try {
                    time = LocalTime.parse(requestString, Time.HH_MM);
                } catch (Exception ex) {
                    Text.print(1, INCORRECT_VALUE);
                }
            }
        }

        return time;
    }

    public static LocalTime timeDivided(String ask) {
        if (Data.isCorrect(ask))
            System.out.println(ask);
        int hour = integer("Укажите час:");
        int minute = integer("Укажите минуту:");
        LocalTime time;
        if (hour > 0 && minute > 0)
            time = LocalTime.of(hour, minute);
        else time = null;

        return time;
    }

    public static LocalDate date() {
        return date(null);
    }

    public static LocalDate date(String ask) {
        String requestString = null;
        LocalDate date = null;
        while (date == null) {
            final String YEAR_UoM = Data.isCorrect(Time.YEAR_UoM) ? " " + Time.YEAR_UoM : "";
            try {
                requestString = string(ask);
                if (!Data.isCorrect(requestString) || requestString.equals("0"))
                    return null;
                switch (requestString.toLowerCase()) {
                    case "позавчера":
                        return LocalDate.now().minusDays(2);
                    case "вчера":
                        return LocalDate.now().minusDays(1);
                    case "сегодня":
                        return LocalDate.now();
                    case "завтра":
                    case "через день":
                    case "спустя день":
                        return LocalDate.now().plusDays(1);
                    case "послезавтра":
                    case "через два дня":
                    case "спустя два дня":
                        return LocalDate.now().plusDays(2);
                    case "через три дня":
                    case "спустя три дня":
                        return LocalDate.now().plusDays(3);
                    case "через четыре дня":
                    case "спустя четыре дня":
                        return LocalDate.now().plusDays(4);
                    case "через пять дней":
                    case "спустя пять дней":
                        return LocalDate.now().plusDays(5);
                    case "через шесть дней":
                    case "спустя шесть дней":
                        return LocalDate.now().plusDays(6);
                    case "через семь дней":
                    case "через неделю":
                    case "спустя семь дней":
                    case "спустя неделю":
                        return LocalDate.now().plus(1, ChronoUnit.WEEKS);
                    case "через две недели":
                    case "спустя две недели":
                        return LocalDate.now().plus(2, ChronoUnit.WEEKS);
                    case "через три недели":
                    case "спустя три недели":
                        return LocalDate.now().plus(4, ChronoUnit.WEEKS);
                    case "через четыре недели":
                    case "спустя четыре недели":
                    case "через месяц":
                    case "спустя месяц":
                        return LocalDate.now().plus(1, ChronoUnit.MONTHS);
                    case "через два месяца":
                    case "спустя два месяца":
                        return LocalDate.now().plus(2, ChronoUnit.MONTHS);
                    case "через три месяца":
                    case "спустя три месяца":
                    case "через квартал":
                    case "спустя квартал":
                        return LocalDate.now().plus(3, ChronoUnit.MONTHS);
                    case "через год":
                    case "спустя год":
                        return LocalDate.now().plus(1, ChronoUnit.YEARS);
                }

                date = LocalDate.parse(requestString + YEAR_UoM, Time.D_MM_YYYY);

            } catch (Exception e) {
                if (!Data.isCorrect(requestString))
                    return null;
                try {
                    date = LocalDate.parse(requestString + YEAR_UoM, Time.DD_MM_YYYY);
                } catch (Exception ex) {
                    Text.print(1, INCORRECT_VALUE);
                }
            }
        }

        return date;
    }

    public static LocalDate dateDivided(String ask) {
        if (Data.isCorrect(ask))
            System.out.println(ask);

        boolean wrong = true;
        int year = 0;
        while (wrong) {
            try {
                year = integer("Укажите год:");
                if (year == 0)
                    return null;
                if (year < 1)
                    Text.print(1, INCORRECT_VALUE);
                else wrong = false;
            } catch (Exception e) {
                Text.print(1, INCORRECT_VALUE);
            }
        }

        wrong = true;
        int month = 0;
        while (wrong) {
            try {
                month = integer("Укажите месяц:");
                if (month == 0)
                    return null;
                if (month < 0 || month > 12)
                    Text.print(1, INCORRECT_VALUE);
                else wrong = false;
            } catch (Exception e) {
                Text.print(1, INCORRECT_VALUE);
            }
        }

        wrong = true;
        int day = 0;
        while (wrong) {
            try {
                day = integer("Укажите день месяца:");
                if (day == 0)
                    return null;

                Calendar date = new GregorianCalendar();
                date.set(Calendar.YEAR, year);
                date.set(Calendar.MONTH, month);
                if (day < 0 || day > date.getActualMaximum(Calendar.MONTH))
                    Text.print(1, INCORRECT_VALUE);
                else wrong = false;
            } catch (Exception e) {
                Text.print(1, INCORRECT_VALUE);
            }
        }

        return LocalDate.of(year, month, day);
    }

    public static int integer() {
        return integer(null);
    }

    public static int integer(String ask) {
        if (Data.isCorrect(ask))
            System.out.println(ask);

        try {
            return Math.abs(Integer.parseInt(string()));
        } catch (Exception e) {
            System.out.println("Вводимое значение должно быть числом.");
            return integer(ask);
        }
    }

    public static String string() {
        return string(null);
    }

    public static String string(String ask) {
        if (Data.isCorrect(ask))
            System.out.println(ask);

        try {
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine();
        } catch (Exception e) {
            return null;
        }
    }
}
