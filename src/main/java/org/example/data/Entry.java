package org.example.data;

public record Entry(String description, double value) implements Comparable<Entry> {

    public static Entry missalettes(double value) {
        return new Entry("Missalettes", value);
    }

    public static Entry uji(double value) {
        return new Entry("uji", value);
    }

    @Override
    public int compareTo(Entry o) {
        return description().compareTo(o.description());
    }
}
