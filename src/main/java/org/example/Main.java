package org.example;

import org.example.data.*;

import java.io.FileWriter;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.abs;
import static org.example.data.Extractor.extractData;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static final String DATE_PATTERN = "yyyy/MM/dd";
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern(DATE_PATTERN);


    public static void main(String[] args) {
        try (OutputWriter writer = OutputWriter.instance()) {
            var data = extractData();
            writer.write(data);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void print(Map<Week, List<Subsection>> map) {
        var b = new StringBuilder();

        final var netProfit = new AtomicReference<>(.0);
        map.forEach((w, s) -> {
            appendln(b, "");
            appendln(b.append("Income Statement for Week Ended on "), w.start());
            var innerNetProfit = .0;
            var grossProfit = .0;
            for (Subsection subsection : s) {
                appendln(b, subsection.title());
                for (Entry e : subsection.entries()) {
                    append(e, b);
                }
                Entry summary = subsection.summary();
                grossProfit += summary.value();
                append(summary, b).append('\n');
                if (subsection.title().equalsIgnoreCase("cost of goods sold")) {
                    appendln(b.append("Gross Profit: "), grossProfit).append("\n\n");
                }
                innerNetProfit += summary.value();
                netProfit.updateAndGet(v -> v + summary.value());
            }
            appendln(b.append("Net Profit: "), innerNetProfit).append('\n');
        });
        appendln(b.append("Total Net Profit: "), netProfit.get()).append('\n');

        System.out.println(b);
        try (FileWriter writer = new FileWriter("Output.txt")) {
            writer.write(b.toString().trim());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static StringBuilder append(Entry e, StringBuilder b) {
        return b.append(e.description()).append(" : ").append(abs(e.value())).append('\n');
    }

    private static StringBuilder appendln(StringBuilder b, Object s) {
        return b.append(s).append('\n');
    }
}