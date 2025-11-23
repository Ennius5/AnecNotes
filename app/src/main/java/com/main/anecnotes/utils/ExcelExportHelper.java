package com.main.anecnotes.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelExportHelper {
    private static final String TAG = "ExcelExportHelper";

    public static boolean exportNotesToExcel(Context context, Uri fileUri,
                                             List<Note> classroomNotes,
                                             List<Note> studentNotes,
                                             String classroomName) {
        Workbook workbook = null;
        OutputStream outputStream = null;

        try {
            Log.d(TAG, "Starting Excel export...");

            // Create workbook and sheet
            workbook = new XSSFWorkbook();
            Sheet sheet = workbook.createSheet("Anecdotal Notes");

            // Create header style
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle wrapStyle = createWrapStyle(workbook);

            // Create headers
            String[] headers = {
                    "DATE",
                    "KNOWN OR SUSPECTED SETTING EVENTS",
                    "ANTECEDENT TRIGGER",
                    "BEHAVIOR",
                    "CONSEQUENCES/RESPONSES",
                    "ACTION(S) TAKEN"
            };

            // Create header row
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;

            // Add classroom notes first
            if (classroomNotes != null && !classroomNotes.isEmpty()) {
                Row classroomHeaderRow = sheet.createRow(rowNum++);
                Cell classroomHeaderCell = classroomHeaderRow.createCell(0);
                classroomHeaderCell.setCellValue("CLASSROOM NOTES - " + classroomName);
                classroomHeaderCell.setCellStyle(createSubheaderStyle(workbook));

                for (Note note : classroomNotes) {
                    if (note != null) {
                        Row row = sheet.createRow(rowNum++);
                        createNoteRow(row, note, null, wrapStyle);
                    }
                }
                rowNum++; // Empty row
            }

            // Add student notes
            if (studentNotes != null && !studentNotes.isEmpty()) {
                Row studentHeaderRow = sheet.createRow(rowNum++);
                Cell studentHeaderCell = studentHeaderRow.createCell(0);
                studentHeaderCell.setCellValue("STUDENT NOTES");
                studentHeaderCell.setCellStyle(createSubheaderStyle(workbook));

                for (Note note : studentNotes) {
                    if (note != null) {
                        Row row = sheet.createRow(rowNum++);
                        createNoteRow(row, note, null, wrapStyle);
                    }
                }
            }

            // Set manual column widths (since autoSizeColumn doesn't work on Android)
            setManualColumnWidths(sheet);

            // Write to the provided URI using SAF
            outputStream = context.getContentResolver().openOutputStream(fileUri);
            if (outputStream == null) {
                Log.e(TAG, "Failed to open output stream for URI: " + fileUri);
                return false;
            }

            workbook.write(outputStream);
            Log.d(TAG, "Excel export completed successfully");
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Error exporting to Excel: " + e.getMessage(), e);
            return false;
        } finally {
            try {
                if (workbook != null) {
                    workbook.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error closing resources: " + e.getMessage());
            }
        }
    }

    private static void setManualColumnWidths(Sheet sheet) {
        // Set fixed column widths (in units of 1/256 of a character width)
        // These are approximate values that should work for most cases

        // DATE column - shorter
        sheet.setColumnWidth(0, 15 * 256);
        // SETTING EVENTS column - medium
        sheet.setColumnWidth(1, 25 * 256);
        // ANTECEDENT TRIGGER column - medium
        sheet.setColumnWidth(2, 25 * 256);
        // BEHAVIOR column - wider (most important)
        sheet.setColumnWidth(3, 40 * 256);
        // CONSEQUENCES column - medium
        sheet.setColumnWidth(4, 25 * 256);
        // ACTIONS TAKEN column - medium
        sheet.setColumnWidth(5, 25 * 256);
    }

    private static void createNoteRow(Row row, Note note, String studentName, CellStyle wrapStyle) {
        // Date
        Cell dateCell = row.createCell(0);
        dateCell.setCellValue(note.getDate());

        // Setting Events
        Cell settingEventsCell = row.createCell(1);
        String settingEvents = note.getSettingEvents() != null ? note.getSettingEvents() : "";
        settingEventsCell.setCellValue(settingEvents);
        settingEventsCell.setCellStyle(wrapStyle);

        // Antecedent Trigger
        Cell antecedentCell = row.createCell(2);
        String antecedent = note.getAntecedentTrigger() != null ? note.getAntecedentTrigger() : "";
        antecedentCell.setCellValue(antecedent);
        antecedentCell.setCellStyle(wrapStyle);

        // Behavior - add student name prefix if provided
        Cell behaviorCell = row.createCell(3);
        String behaviorText = note.getBehavior() != null ? note.getBehavior() : "";
        if (studentName != null && !studentName.isEmpty()) {
            behaviorText = studentName + " - " + behaviorText;
        }
        behaviorCell.setCellValue(behaviorText);
        behaviorCell.setCellStyle(wrapStyle);

        // Consequences
        Cell consequencesCell = row.createCell(4);
        String consequences = note.getConsequences() != null ? note.getConsequences() : "";
        consequencesCell.setCellValue(consequences);
        consequencesCell.setCellStyle(wrapStyle);

        // Actions Taken
        Cell actionsCell = row.createCell(5);
        String actions = note.getActionsTaken() != null ? note.getActionsTaken() : "";
        actionsCell.setCellValue(actions);
        actionsCell.setCellStyle(wrapStyle);
    }

    private static CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle createSubheaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setItalic(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private static CellStyle createWrapStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }
}