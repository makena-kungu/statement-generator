package org.example;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;

class OutputWriterTest {

    @Test
    public void printTest() {
        var row = 1;
        String format = MessageFormat.format("A{0}:B{0}", row);
        System.out.println("format = " + format);
        Assertions.assertEquals(format, "A1:B1");
    }


    @Test
    void printAmount() {
        var format = DecimalFormat.getCurrencyInstance(Locale.forLanguageTag("en-ke"));
        format.setMaximumFractionDigits(0);

        System.out.println(format.format(100));
        System.out.println(format.format(1000));
    }
}