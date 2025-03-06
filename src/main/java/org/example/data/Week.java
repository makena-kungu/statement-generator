package org.example.data;

import java.time.LocalDate;
import java.util.Objects;

import static org.example.data.WeekComparator.fields;

public final class Week implements Comparable<Week> {
    private final LocalDate start;
    private final LocalDate end;

    private Week(LocalDate start, LocalDate end) {
        // this constructor prevents creation of week object that doesn't have a diff of 7 days
        // from the first to the last date
        this.start = start;
        this.end = end;
    }

    public static Week ofDate(LocalDate date) {
        LocalDate start = date.with(fields.dayOfWeek(), 1);
        LocalDate end = date.with(fields.dayOfWeek(), 7);
        return new Week(start, end);
    }

    @Override
    public int compareTo(Week o) {
        return start.compareTo(o.start);
    }

    public LocalDate start() {
        return start;
    }

    public LocalDate end() {
        return end;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Week) obj;
        return Objects.equals(this.start, that.start) &&
                Objects.equals(this.end, that.end);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, end);
    }

    @Override
    public String toString() {
        return "Week[" +
                "start=" + start + ", " +
                "end=" + end + ']';
    }

}
