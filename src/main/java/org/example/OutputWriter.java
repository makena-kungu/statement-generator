package org.example;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.apache.poi.xssf.usermodel.*;
import org.example.data.*;
import org.openxmlformats.schemas.spreadsheetml.x2006.main.CTColor;

import java.awt.Color;
import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.text.*;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static org.example.data.Classification.TYPE1;
import static org.example.data.Classification.TYPE2;

public class OutputWriter implements Closeable {

	private final XSSFWorkbook wb;
	private final XSSFSheet sheet;
	private final AtomicInteger row = new AtomicInteger();
	private final AtomicInteger maxCharsCol1 = new AtomicInteger(0);
	private final AtomicInteger maxCharsCol2 = new AtomicInteger(0);
	private final XSSFCellStyle heading;
	private final XSSFCellStyle sectionHeading;
	private final XSSFCellStyle sectionFooter;
	private final XSSFCellStyle ultimateFooter;
	private final XSSFCellStyle bodyStyleEven;
	private final XSSFCellStyle bodyStyleOdd;
	private final BorderStyle BORDER_STYLE = BorderStyle.THIN;

	private final NumberFormat FORMAT = DecimalFormat.getCurrencyInstance(Locale.forLanguageTag("en-ke"));
//    private final NumberFormat FORMAT = DecimalFormat.getNumberInstance(Locale.forLanguageTag("en-ke"));

	{
		FORMAT.setMaximumFractionDigits(0);
	}

	private OutputWriter() {
		wb = new XSSFWorkbook();
		sheet = wb.createSheet("Income Statements 2025");

		final var borderStyle = BORDER_STYLE;

		heading = wb.createCellStyle();
		var headingFont = heading.getFont();
		headingFont.setBold(true);
		heading.setFont(headingFont);
		heading.setAlignment(HorizontalAlignment.CENTER);
		heading.setWrapText(true);
		heading.setVerticalAlignment(VerticalAlignment.CENTER);

		short indentHNF = 2;
		short indentBody = 4;

		sectionHeading = wb.createCellStyle();
		sectionHeading.setIndention(indentHNF);
		var sectionHeadingFont = wb.createFont();
		sectionHeadingFont.setBold(true);
		sectionHeading.setFont(sectionHeadingFont);
		sectionHeading.setBorderLeft(borderStyle);
		sectionHeading.setBorderRight(borderStyle);

		sectionFooter = wb.createCellStyle();
		var sectionFooterFont = wb.createFont();
		sectionFooterFont.setBold(true);
		sectionFooter.setFont(sectionFooterFont);
		sectionFooter.setBorderLeft(borderStyle);
		sectionFooter.setIndention(indentHNF);
		sectionFooter.setBorderRight(borderStyle);

		bodyStyleEven = wb.createCellStyle();
		var bodyFont = wb.createFont();
		bodyFont.setBold(false);
		bodyStyleEven.setFont(bodyFont);
		bodyStyleEven.setIndention(indentBody);
		bodyStyleEven.setBorderLeft(borderStyle);
		bodyStyleEven.setBorderRight(borderStyle);
		bodyStyleOdd = wb.createCellStyle();
		bodyStyleOdd.cloneStyleFrom(bodyStyleEven);
		bodyStyleOdd.setFillPattern(FillPatternType.SOLID_FOREGROUND);
//        XSSFColor.from()

		bodyStyleOdd.setFillForegroundColor(new XSSFColor(new byte[]{(byte) 240, (byte) 248, (byte) 255}).getIndex());

		ultimateFooter = wb.createCellStyle();
		ultimateFooter.cloneStyleFrom(sectionFooter);
		ultimateFooter.setBorderTop(borderStyle);
		ultimateFooter.setBorderBottom(borderStyle);
	}

	private static String path() throws IOException {
		var folder = Resources.outputFolder().getPath();
		var format = new SimpleDateFormat("yyyyMMdd_hhmmss");
		var date = format.format(new Date());
		return folder + "\\Weekly Income Statement " + date + ".xlsx";
	}

	public static OutputWriter instance() throws IOException {
		return new OutputWriter();
	}

	public void write(Map<Week, Set<Section>> data) throws IOException {
		data.forEach(this::weeklyData);

		// Ultimate Footer
		AtomicReference<Double> netProfit = new AtomicReference<>(.0);
		data.forEach((w, ss) -> ss.forEach(section -> netProfit.updateAndGet(v -> (v + section.summary().value()))));
		XSSFRow footer = printFooter(new Entry("Net Profit/Loss", netProfit.get()));

		// add border to footer
		for (Cell cell : footer) {
			cell.setCellStyle(ultimateFooter);
		}

		final int maxCharsCol2 = this.maxCharsCol2.get();
		final int maxWidthCol2 = width(maxCharsCol2);
		sheet.setColumnWidth(1, maxWidthCol2);
		int column2WidthInPixels = Math.round(sheet.getColumnWidthInPixels(1));
		short width = WidthConverter.pixel2WidthUnits(WidthConverter.EXCEL_MAX_WIDTH_IN_PXS - column2WidthInPixels);
		sheet.setColumnWidth(0, width);

		var setup = sheet.getPrintSetup();
		setup.setPaperSize(PaperSize.A4_PAPER);


		try (FileOutputStream fileOutputStream = new FileOutputStream(path())) {
			wb.write(fileOutputStream);
		}
	}

	private void weeklyData(Week week, Set<Section> weeklyData) {
		//print heading with borders at the top,
		printHeading(week);
		printBody(weeklyData);
	}

	private final DateTimeFormatter formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM);

	private void printHeading(Week week) {
		final var rowId = row();
		var row = sheet.createRow(rowId);
		short height = (short) (row.getHeight() + 4);
		row.setHeight(height);
		var cell = row.createCell(0, CellType.STRING);
		var h = MessageFormat.format("Income Statement for Week End at {0}", week.end().format(formatter));
		cell.setCellValue(h);
		cell.setCellStyle(heading);
		var region = new CellRangeAddress(rowId, rowId, 0, 1);
		sheet.addMergedRegion(region);
		addBorders(region);
	}

	private void printBody(Set<Section> sections) {
		double profit = .0;
		for (Section section : sections) {
			int type = section.type();
			var entry = switch (type) {
				case TYPE1 -> section.summary();
				case TYPE2 -> section.summary(profit);
				default -> throw new IllegalArgumentException();
			};
			profit = entry.value();

			for (Subsection subsection : section.subsections()) {
				printSection(subsection);
			}
			printFooter(entry);
		}
		System.out.println("Net Profit/Loss: " + profit);
	}

    /*private void printBody(List<Subsection> subsections) {
        double netProfit = .0;
        double grossProfit = .0;
        var done = false;
        for (Subsection subsection : subsections) {
            printSection(subsection);

            netProfit += subsection.summary().value();

            boolean canCalcGrossProfit = subsection.calcGrossProfit();
            if (canCalcGrossProfit) {
                grossProfit += subsection.summary().value();
                continue;
            } else if (done) continue;
            //print gross profit
            done = true;
            printFooter(new Entry("Gross Profit", grossProfit));
        }
        printFooter(new Entry("Net Profit", netProfit));
    }*/

	private void printSection(Subsection subsection) {
		// print section header
		printSectionHeader(subsection.title());
		//print body
		for (Entry entry : subsection.entries()) printEntry(entry);
		// print section footer
		printFooter(subsection.summary());
	}

	private void printSectionHeader(String title) {
		max1(title);
		var row = sheet.createRow(row());
		for (int i = 0; i < 2; i++) {
			var cell = row.createCell(i, i == 0 ? CellType.STRING : CellType.BLANK);
			cell.setCellStyle(sectionHeading);
			if (i == 0) cell.setCellValue(title);
		}
	}

	private void printEntry(Entry entry) {
		String desc = entry.description();
		String value = format(entry.value());
		max1(desc);
		max2(value);
		int rownum = row();
		var row = sheet.createRow(rownum);
		//first column contains the description, the second the value
		String[] values = new String[]{desc, value};
		for (int i = 0, len = values.length; i < len; i++)
			cell(row, i, bodyStyleEven).setCellValue(values[i]);

	}

	private XSSFRow printFooter(Entry entry) {
		String desc = entry.description();
		String value = format2(entry.value());
		max1(desc);
		max2(value);
		XSSFRow row = sheet.createRow(row());
		var vals = new String[]{desc, value};
		for (int i = 0; i < vals.length; i++) {
			XSSFCell cell = row.createCell(i, CellType.STRING);
			cell.setCellStyle(sectionFooter);
			cell.setCellValue(vals[i]);
		}
		return row;
	}

	private String format(double amount) {
		return FORMAT.format(abs(amount));
	}

	private String format2(double amount) {
		String s = format(amount);
		if (amount < 0) {
			s = String.format("(%s)", s);
		}
		return s;
	}

	private XSSFCell cell(XSSFRow row, int index, CellStyle style) {
		var cell = row.createCell(index, CellType.STRING);
		cell.setCellStyle(style);
		return cell;
	}

	private void addBorders(CellRangeAddress region) {
		BorderStyle style = BORDER_STYLE;
		RegionUtil.setBorderLeft(style, region, sheet);
		RegionUtil.setBorderTop(style, region, sheet);
		RegionUtil.setBorderBottom(style, region, sheet);
		RegionUtil.setBorderRight(style, region, sheet);
	}

	private void max1(String s) {
		int le = s.length();
		maxCharsCol1.set(max(le, maxCharsCol1.get()));
	}

	private void max2(String s) {
		maxCharsCol2.set(max(s.length(), maxCharsCol2.get()));
	}

	private int row() {
		return row.getAndIncrement();
	}

	private int width(int count) {
		return ((int) (count + 4 * 1.14388) * 256);
	}

	private int width(AtomicInteger count) {
		return width(count.get());
	}

	@Override
	public void close() throws IOException {
		wb.close();
	}
}
