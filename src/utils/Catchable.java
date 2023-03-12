package utils;

public interface Catchable {

    enum ActResult {
        DONE ("Успех"), UNKNOWN ("<не известно>"), ERROR ("Ошибка"), FAIL ("Провал");

        private final String title;


        ActResult(String title) {
            this.title = title;
        }

        public String getTitle() {
            return title;
        }
    }
}
