package org.example.data;

import java.util.*;

import static org.example.data.Classification.TYPE1;
import static org.example.data.Classification.TYPE2;

public final class Section implements Comparable<Section> {

    private final int type;
    private final Set<Subsection> subsections;

    private Section(int type, Subsection... subsections) {
        this.type = type;
        this.subsections = Set.of(subsections);
    }

    public static Section of(Subsection... subsections) {
        if (!ensureSameClass(subsections)) {
            return null;
        }
        // good to go
        Classification klass = subsections[0].klass();

        return new Section(klass.getType(), subsections);
    }

    public static Section of(Collection<Subsection> subsections) {
        return of(subsections.toArray(new Subsection[0]));
    }

    public static boolean ensureSameClass(Subsection... subs) {
        int klass = 0;
        for (Subsection sub : subs) {
            if (klass <= 0) {
                klass = sub.klass().getType();
                continue;
            }
            //make comparison
            if (klass != sub.klass().getType()) return false;
        }
        return true;
    }

    @Override
    public int compareTo(Section o) {
        return Integer.compare(type, o.type);
    }

    public Set<Subsection> subsections() {
        return new TreeSet<>(subsections);
    }

    public Entry summary() {
        return summary(0.0);
    }

    public int type() {
        return type;
    }

    public Entry summary(double grossProfit) {
        double sum = subsections.stream().mapToDouble(s -> s.summary().value()).sum();
        return switch (type) {
            case TYPE1 -> new Entry("Gross Profit", sum);
            case TYPE2 -> new Entry("Net Profit", grossProfit + sum);
            default -> throw new IllegalArgumentException("Unidentified Type: " + type);
        };
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Section) obj;
        return type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(subsections);
    }

    @Override
    public String toString() {
        return "Section[" +
                "subsections=" + subsections + ']';
    }
}
