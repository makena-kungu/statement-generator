package org.example;

import org.example.data.Week;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

class WeekTest {

    @Test
    void ofDate() {
        Week week = Week.ofDate(LocalDate.now());
        System.out.println(week);
        Assertions.assertTrue(week.start().isEqual(LocalDate.of(2024, 11, 17)));
        Assertions.assertTrue(week.end().isEqual(LocalDate.of(2024, 11, 23)));
    }
}