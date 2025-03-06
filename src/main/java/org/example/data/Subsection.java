package org.example.data;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public record Subsection(Classification klass, Set<Entry> entries) implements Comparable<Subsection> {

    public Entry summary() {
        double d = 0;
        for (Entry entry : entries) d += entry.value();
        return new Entry("Total " + title(), d);
    }

    @Override
    public int compareTo(Subsection o) {
        return klass.compareTo(o.klass);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Subsection subsection = (Subsection) o;
        return klass.equals(subsection.klass);
    }

    @Override
    public int hashCode() {
        return klass.hashCode();
    }

    @Override
    public String toString() {
        return "Section{" +
                "title='" + title() + '\'' +
                ", entries=" + Arrays.toString(entries.toArray(new Entry[0])) +
                '}';
    }

    public String title() {
        return klass.getTitle();
    }
}
