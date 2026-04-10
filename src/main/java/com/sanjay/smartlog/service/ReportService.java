package com.sanjay.smartlog.service;

import com.sanjay.smartlog.entity.WorkLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

/**
 * Generates Excel reports using Apache POI.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final WorkLogService workLogService;

    /**
     * Parses a user-supplied month string and returns a YearMonth.
     * Supported formats:
     *  - "April 2026"
     *  - "Apr 2026"
     *  - "04 2026" or "4 2026"
     */
    public YearMonth parseMonthInput(String input) {
        if (input == null || input.isBlank()) return null;
        String trimmed = input.trim();

        // Try full month name: "April 2026"
        try {
            return YearMonth.parse(trimmed,
                    DateTimeFormatter.ofPattern("MMMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {}

        // Try abbreviated month name: "Apr 2026"
        try {
            return YearMonth.parse(trimmed,
                    DateTimeFormatter.ofPattern("MMM yyyy", Locale.ENGLISH));
        } catch (DateTimeParseException ignored) {}

        // Try numeric month: "04 2026" or "4 2026"
        try {
            String[] parts = trimmed.split("[\\s/\\-]+");
            if (parts.length == 2) {
                int month = Integer.parseInt(parts[0]);
                int year = Integer.parseInt(parts[1]);
                return YearMonth.of(year, month);
            }
        } catch (Exception ignored) {}

        return null;
    }

    /**
     * Generates an Excel workbook as byte[] for the given chatId and yearMonth.
     */
    public byte[] generateExcel(String chatId, YearMonth yearMonth) throws Exception {
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<WorkLog> logs = workLogService.getLogsForDateRange(chatId, startDate, endDate);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Work Log - " + yearMonth);

            // === Header styling ===
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            // === Data date style ===
            CellStyle dateStyle = workbook.createCellStyle();
            CreationHelper createHelper = workbook.getCreationHelper();
            dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("yyyy-MM-dd"));

            // === Header row ===
            Row headerRow = sheet.createRow(0);
            String[] columns = {"Date", "Hour Slot", "Task", "Status"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
            }

            // === Data rows ===
            int rowNum = 1;
            for (WorkLog wl : logs) {
                Row row = sheet.createRow(rowNum++);

                // Date
                Cell dateCell = row.createCell(0);
                dateCell.setCellValue(wl.getDate().toString());

                // Hour Slot
                row.createCell(1).setCellValue(wl.getHourSlot());

                // Task
                row.createCell(2).setCellValue(
                        wl.getTask() != null ? wl.getTask() : "");

                // Status
                CellStyle statusStyle = workbook.createCellStyle();
                Font statusFont = workbook.createFont();
                if (wl.getStatus() == WorkLog.Status.COMPLETED) {
                    statusFont.setColor(IndexedColors.GREEN.getIndex());
                } else {
                    statusFont.setColor(IndexedColors.RED.getIndex());
                }
                statusStyle.setFont(statusFont);
                Cell statusCell = row.createCell(3);
                statusCell.setCellValue(wl.getStatus().name());
                statusCell.setCellStyle(statusStyle);
            }

            // === Summary row ===
            if (!logs.isEmpty()) {
                Row summaryRow = sheet.createRow(rowNum + 1);
                long completed = logs.stream()
                        .filter(l -> l.getStatus() == WorkLog.Status.COMPLETED).count();
                summaryRow.createCell(0).setCellValue("Total Entries: " + logs.size());
                summaryRow.createCell(1).setCellValue("Completed: " + completed);
                summaryRow.createCell(2).setCellValue("Pending: " + (logs.size() - completed));
            }

            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            log.info("Generated Excel report: {} rows for chatId={} month={}", logs.size(), chatId, yearMonth);
            return out.toByteArray();
        }
    }
}
