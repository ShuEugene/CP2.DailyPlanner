package utils;

import planner.Journal;
import planner.Task;

import java.util.Comparator;

public interface Sortable {

    enum SortMode {
        ASC("Возрастание", "Ascending", Journal.ASC),
        DESC("Убывание", "Descending", Journal.DESC),
        ORD("По порядковым номерам", "By ordinal numbers", Journal.ORD);

        private final String title, translate;
        private final Comparator<Task> comparator;

        SortMode() {
            this(null, null, null);
        }

        SortMode(String title) {
            this(title, "");
        }

        SortMode(Comparator<Task> comparator) {
            this(null, null, comparator);
        }

        SortMode(String title, String translate) {
            this(title, translate, null);
        }

        SortMode(String title, Comparator<Task> comparator) {
            this(title, null, comparator);
        }

        SortMode(String title, String translate, Comparator<Task> comparator) {
            this.title = title;
            this.translate = translate;
            this.comparator = comparator == null ? Journal.ASC : comparator;
        }


        public final Comparator<Task> comparator() {
            return comparator;
        }

        public String getTitle() {
            return title;
        }

        public String getTranslate() {
            return translate;
        }
    }

    void sort();

    void sort(SortMode sortMode);
}