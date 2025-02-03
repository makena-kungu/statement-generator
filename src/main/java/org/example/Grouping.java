package org.example;

import org.example.data.Subsection;
import org.example.data.Week;
import org.example.data.WeekComparator;

import java.util.*;

public class Grouping {

    public static Map<Week, List<Subsection>> groupWeekly(Map<String, List<Subsection>> rC, Map<String, List<Subsection>> eI) {
        Set<String> dates = eI.keySet();
        var list = new LinkedList<String>();

        for (String date : rC.keySet()) {
            for (String s : dates) {
                if (!WeekComparator.isSameWeek(date, s)) continue;

                dates.remove(s);
            }
        }
        return Collections.emptyMap();
    }
}
