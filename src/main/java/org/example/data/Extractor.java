package org.example.data;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory;

import java.io.File;
import java.io.IOException;
import java.util.*;

import static org.example.data.Classification.TYPE2;
import static org.example.data.Entry.missalettes;
import static org.example.data.Entry.uji;
import static org.example.data.Resources.inputFolder;

public class Extractor {

	public static String inputFile() {
		File in = inputFolder();
		System.out.println("Select file to proceed.");
		final Map<Integer, Item> files = files(in);
		files.forEach((key, value) -> System.out.println(value.printableFilename));
		final var scanner = new Scanner(System.in);
		int key = scanner.nextInt();
		Item item = files.get(key);
		return item.file.getPath();
	}


	public static Map<Integer, Item> files(final File inputFolder) {
		File[] listFiles = inputFolder.listFiles();
		final var map = new HashMap<Integer, Item>();
		if (listFiles == null) return Collections.emptyMap();
		for (int i = 0, size = listFiles.length; i < size; i++) {
			File file = listFiles[i];
			if (file.isDirectory()) continue;
			var printableFilename = option(i, file.getName());
			map.put(i + 1, new Item(printableFilename, file));
		}
		return map;
	}

	public record Item(String printableFilename, File file) {
	}

	private static String option(int i, String s) {
		return (i + 1) + ") " + s;
	}

	public static Map<Week, Set<Section>> extractData() {
		try (var wb = XSSFWorkbookFactory.createWorkbook(XSSFWorkbook.openPackage(inputFile()))) {
//            final var revNCoGS = wb.getSheet("2024 INCOME STATEMENT (PARTLY)");
			final var revNCoGS = wb.getSheet("2025 INCOME STATEMENT");
			final var incomesNExpenses = wb.getSheet("EXPENSE - INCOME 2025");
//            final var incomesNExpenses = wb.getSheet("Sheet3");
			final var data = new TreeMap<Week, Set<Section>>();
			var k = revNCoGS.rowIterator();
			// skipping the title rows
			int j = 0;
			while (k.hasNext()) {
				var row = k.next();
				if (j++ <= 1) continue;

				var week = Week.ofDate(row.getCell(0).getLocalDateTimeCellValue().toLocalDate());
				var rM = cellValue(1, row);
				var rU = cellValue(2, row);
				var cM = cellValue(3, row) * -1;
				var cU = cellValue(4, row) * -1;

				Subsection revenue = new Subsection(Classification.REVENUE, Set.of(missalettes(rM), uji(rU)));
				Subsection cogs = new Subsection(Classification.COGS, Set.of(missalettes(cM), uji(cU)));
				data.put(week, Set.of(Objects.requireNonNull(Section.of(revenue, cogs))));
			}

			//skipping the header row
			int l = 0;
			final var type = TYPE2;
			for (Row row : incomesNExpenses) {
				if (l++ == 0) continue;

				var date = row.getCell(0).getLocalDateTimeCellValue().toLocalDate();
				var week = Week.ofDate(date);
				var description = row.getCell(1).getStringCellValue();
				System.out.printf("desc-%s: week - %s\n", description, week);
				var isExpense = !isCellBlank(row, 2);
				Classification klass;
				double amount;

				try {
					if (isExpense) {
						amount = row.getCell(2).getNumericCellValue() * -1;
						klass = Classification.EXPENSES;
					} else {
						amount = row.getCell(3).getNumericCellValue();
						klass = Classification.INCOMES;
					}
				} catch (Exception e) {
					var err = new StringBuilder("Sheet: " + incomesNExpenses.getSheetName())
							.append("\nError: ").append(e.getMessage())
							.append("\nRow: ").append(l)
							.append(" - ").append(row.getCell(1).getStringCellValue());
					Cell cell = row.getCell(2);
					err.append("\nExpenses: ").append(cell != null ? cell.getStringCellValue() : "No data found");
					Cell cell1 = row.getCell(3);
					err.append("\nIncomes: ").append(cell1 != null ? cell1.getStringCellValue() : "No data found");
					System.err.print(err);
//					e.printStackTrace();
					continue;
				}

				final TreeSet<Section> existing = new TreeSet<>(data.getOrDefault(week, Set.of()));
				// find if there's an existing section of type 2
				Entry entry1 = new Entry(description, amount);
				System.out.printf("Entry: %s\n", entry1);
				final Map<Integer, Section> map = new HashMap<>();
				for (Section section : existing) {
					map.put(section.type(), section);
				}

				Section section = map.getOrDefault(type, Section.of(new Subsection(klass, Set.of(entry1))));
				if (map.containsKey(type)) {
					// check if within the subsections in that section have the same class
					Set<Subsection> subsections = getSubsections(section, klass, entry1);
					System.out.println();
					System.out.println("Adding subsections: ");
					System.out.println(subsections);
					System.out.println();
					map.put(type, Section.of(subsections));
				} else {
					// there is no such section present, hence just save the new section
					map.put(type, section);
				}

				existing.clear();
				for (Map.Entry<Integer, Section> entry : map.entrySet()) {
					System.out.println(entry);
					existing.add(entry.getValue());
				}
				System.out.printf("Week: %s, Set: %s\n", week, existing);
				data.put(week, existing);
			}
			return data;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static Set<Subsection> getSubsections(Section section, Classification klass, Entry entry) {
		Set<Subsection> subsections = section.subsections();
		Subsection subsection1 = null;
		for (Subsection subsection : subsections) {
			if (subsection.klass() != klass) continue;
			subsection1 = subsection;
			break;
		}

		if (subsection1 == null) {
			// create a new subsection
			System.out.println(">>>>Create subsection: " + entry);
			var subsection = new Subsection(klass, Set.of(entry));
			subsections.add(subsection);
		} else {
			System.out.println(">>>>Add to subsection: " + entry);
			var existingEntries = new TreeSet<>(subsection1.entries());
			System.out.println(">>>>Existing: " + existingEntries);
			existingEntries.add(entry);
			System.out.println(">>>>After   : " + existingEntries);
//                        map.put(type, Section.of(new Subsection(klass, existingEntries)));
			// replace the data in the subsection by add the new data
			subsections.removeIf(subsection -> subsection.klass() == klass);
			if (!subsections.add(new Subsection(klass, existingEntries))) {
				throw new IllegalStateException("Couldn't replace");
			}
		}

		System.out.println("#############");
		System.out.println(subsections);
		System.out.println("#############");
		return subsections;
	}

	private static boolean isCellBlank(Row row, @SuppressWarnings("SameParameterValue") int index) {
		Cell cell = row.getCell(index);
		return cell == null || cell.getCellType() == CellType.BLANK;
	}

	private static double cellValue(int index, Row row) {
		Cell cell = row.getCell(index);
		if (cell == null) {
			return 0;
		}
		return cell.getNumericCellValue();
	}
}
