import org.example.data.WeekComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;


public class WeekComparatorTest {

    @Test
    public void testSameWeekDaysPass() {
        var d1 = LocalDate.of(2024, 11, 1);
        var d2 = LocalDate.of(2024, 11, 2);
        Assertions.assertTrue(WeekComparator.isSameWeek(d1, d2));
    }

    @Test
    public void testDiffWeekDaysFail() {
        var d1 = LocalDate.of(2024, 11, 3);
        var d2 = LocalDate.of(2024, 11, 2);
        Assertions.assertFalse(WeekComparator.isSameWeek(d1, d2));
    }
}
