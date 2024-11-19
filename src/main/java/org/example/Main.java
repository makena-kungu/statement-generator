package org.example;

import org.apache.commons.math3.stat.inference.OneWayAnova;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    private static final String FILE_PATH = "C:\\Users\\kiari\\Downloads\\Outstation Weekly Income Statements.xlsx";
    private static final String DATE_PATTERN = "yyyy/MM/dd";

    public static void main(String[] args) throws IOException {
        try (var wb = XSSFWorkbookFactory.createWorkbook(XSSFWorkbook.openPackage(FILE_PATH))) {
            final var revNCoGS = wb.getSheet("Sheet2");
            final var incomesNExpenses = wb.getSheet("Sheet3");
            final var map = new TreeMap<String, List<Section>>();
//            final var map = new HashMap<String, List<Section>>();

            var iterator = revNCoGS.rowIterator();

            // skipping the title rows
            int j = 0;
            while (iterator.hasNext() && j <= 1) {
                iterator.next();
                j++;
            }

            while (iterator.hasNext()) {
                Row row = iterator.next();
                var date = date(row.getCell(0).getDateCellValue());
                var mR = cellValue(1, row);
                var uR = cellValue(2, row);
                var mC = cellValue(3, row) * -1;
                var uC = cellValue(4, row) * -1;

                Section revenue = new Section("Revenue", List.of(new Missalette(mR), new Uji(uR)));
                Section cogs = new Section("Cost of Goods Sold", List.of(new Missalette(mC), new Uji(uC)));
                List<Section> sections = List.of(revenue, cogs);

                map.put(date, sections);
            }



            System.out.println(map);
        }
    }

    private static double cellValue(int index, Row row) {
        Cell cell = row.getCell(index);
        if (cell == null) {
            return 0;
        }
        return cell.getNumericCellValue();
    }

    private static String date(Date date) {
        var formatter = new SimpleDateFormat(DATE_PATTERN);
        return formatter.format(date);
    }

    private record Section(String title, List<Entry> entries) {
        @Override
        public String toString() {
            return "Section{" +
                    "title='" + title + '\'' +
                    ", entries=" + Arrays.toString(entries.toArray(new Entry[0])) +
                    '}';
        }
    }

    private static abstract class Entry {
        private final String name;
        private final double amount;

        public Entry(String name, double amount) {
            this.name = name;
            this.amount = amount;
        }

        public String getName() {
            return name;
        }

        public double getAmount() {
            return amount;
        }

        @Override
        public String toString() {
            return "Entry{" +
                    "name='" + name + '\'' +
                    ", amount=" + amount +
                    '}';
        }
    }

    private static class Missalette extends Entry {

        public Missalette(double amount) {
            super("Missalettes", amount);
        }
    }

    private static class Uji extends Entry {
        public Uji(double amount) {
            super("Uji", amount);
        }
    }
}