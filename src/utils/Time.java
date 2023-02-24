package utils;

import java.util.Objects;

public class Time {

    public static class Period {

        public enum PeriodType {
            HOURS("час(а/ов)"),
            MINUTES("минут(а/ы)"),
            SECOND("секунд(а/у)");

            private final String title;

            PeriodType(String title) {
                this.title = title;
            }

            public final String getTitle() {
                return title;
            }
        }


        private int value;
        private PeriodType type;


        public Period(int value) {
            this(value, null);
        }

        public Period(int value, PeriodType type) {
            setValue(value);
            setType(type);
        }


        public final int getValue() {
            return value;
        }

        public final void setValue(int value) {
            this.value = Math.abs(value);
        }

        public final PeriodType getType() {
            return type;
        }

        public final void setType(PeriodType type) {
            this.type = type == null ? PeriodType.SECOND : type;
        }

        @Override
        public final String toString() {
            return value + " " + getType();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Period)) return false;
            Period period = (Period) o;
            return getValue() == period.getValue() && getType() == period.getType();
        }

        @Override
        public int hashCode() {
            return Objects.hash(getValue(), getType());
        }
    }
}
