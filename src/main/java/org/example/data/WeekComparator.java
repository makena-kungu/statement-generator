package org.example.data;

import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Locale;

import static org.example.Main.DATE_FORMAT;

public class WeekComparator {
    public static final WeekFields fields = WeekFields.of(Locale.US);

    public static boolean isSameWeek(String d1, String d2) {
        return isSameWeek(date(d1), date(d1));
    }

    public static boolean isSameWeek(LocalDate d1, LocalDate d2) {

        int week1 = d1.get(fields.weekOfWeekBasedYear());
        int week2 = d2.get(fields.weekOfWeekBasedYear());

        return d1.getYear() == d2.getYear() && week1 == week2;
    }

    private static LocalDate date(String date) {
        return LocalDate.parse(date, DATE_FORMAT);
    }
}
