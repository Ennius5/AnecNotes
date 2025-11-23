package com.main.anecnotes.utils;
import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import android.util.Log;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.util.IOUtils;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/*The intent for this Object is to be used as a reader for excel sheets*/
public class ExcelReader {
    private static final String TAG = "ExcelReader";


    public interface ExcelReadListener {
        void onExcelDataRead(List<List<String>> data);
        void onExcelReadError(String errorMessage);
        void onSheetsListLoaded(List<String> sheetNames);
    }

    public static void getSheetNames(Context context, Uri fileUri, ExcelReadListener listener) {
        readExcelContent(context, fileUri, null, 0, Integer.MAX_VALUE, listener, true);
    }

    public static void readExcelFile(Context context, Uri fileUri, String sheetName,
                                     int startRow, int endRow, ExcelReadListener listener) {
        readExcelContent(context, fileUri, sheetName, startRow, endRow, listener, false);
    }

    private static void readExcelContent(Context context, Uri fileUri, String sheetName,
                                         int startRow, int endRow, ExcelReadListener listener,
                                         boolean sheetsOnly) {
        InputStream inputStream = null;
        Workbook workbook = null;

        try {
            ContentResolver contentResolver = context.getContentResolver();

            // Try to open the input stream with the content resolver
            inputStream = contentResolver.openInputStream(fileUri);
            if (inputStream == null) {
                listener.onExcelReadError("Cannot open file stream - input stream is null");
                return;
            }

            // For better debugging
            String mimeType = contentResolver.getType(fileUri);
            Log.d(TAG, "File MIME type: " + mimeType);
            Log.d(TAG, "File URI: " + fileUri.toString());

            // Try to create workbook using WorkbookFactory for auto-detection
            try {
                workbook = WorkbookFactory.create(inputStream);
            } catch (Exception e) {
                Log.e(TAG, "Error creating workbook: " + e.getMessage(), e);
                listener.onExcelReadError("Not a valid Excel file or corrupted file: " + e.getMessage());
                return;
            }

            if (sheetsOnly) {
                // Return sheet names only
                List<String> sheetNames = new ArrayList<>();
                int numberOfSheets = workbook.getNumberOfSheets();

                for (int i = 0; i < numberOfSheets; i++) {
                    sheetNames.add(workbook.getSheetName(i));
                }

                listener.onSheetsListLoaded(sheetNames);
            } else {
                // Read actual data
                if (sheetName == null) {
                    listener.onExcelReadError("Sheet name cannot be null");
                    return;
                }

                Sheet sheet = workbook.getSheet(sheetName);
                if (sheet == null) {
                    listener.onExcelReadError("Sheet not found: " + sheetName);
                    return;
                }

                List<List<String>> excelData = readSheetData(sheet, startRow, endRow);
                listener.onExcelDataRead(excelData);
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Security Exception - Permission denied", e);
            listener.onExcelReadError("Permission denied to read the file");
        } catch (Exception e) {
            Log.e(TAG, "Error reading Excel file: " + e.getMessage(), e);
            listener.onExcelReadError("Error reading file: " + e.getMessage());
        } finally {
            // Close resources safely
            closeQuietly(workbook);
            closeQuietly(inputStream);
        }
    }

    private static List<List<String>> readSheetData(Sheet sheet, int startRow, int endRow) {
        List<List<String>> excelData = new ArrayList<>();

        int firstRowNum = sheet.getFirstRowNum();
        int lastRowNum = sheet.getLastRowNum();

        int actualStartRow = Math.max(startRow, firstRowNum);
        int actualEndRow = Math.min(endRow, lastRowNum);

        Log.d(TAG, String.format("Reading rows %d to %d from sheet", actualStartRow, actualEndRow));

        for (int i = actualStartRow; i <= actualEndRow; i++) {
            Row row = sheet.getRow(i);
            List<String> rowData = new ArrayList<>();

            if (row != null) {
                for (Cell cell : row) {
                    rowData.add(getCellValueAsString(cell));
                }
            }
            excelData.add(rowData);
        }

        return excelData;
    }

    private static String getCellValueAsString(Cell cell) {
        if (cell == null) return "";

        try {
            switch (cell.getCellType()) {
                case STRING:
                    return cell.getStringCellValue().trim();
                case NUMERIC:
                    if (DateUtil.isCellDateFormatted(cell)) {
                        return cell.getDateCellValue().toString();
                    } else {
                        double num = cell.getNumericCellValue();
                        if (num == Math.floor(num) && !Double.isInfinite(num)) {
                            return String.valueOf((int) num);
                        } else {
                            return String.valueOf(num);
                        }
                    }
                case BOOLEAN:
                    return String.valueOf(cell.getBooleanCellValue());
                case FORMULA:
                    // Try to evaluate formula
                    try {
                        switch (cell.getCachedFormulaResultType()) {
                            case NUMERIC:
                                return String.valueOf(cell.getNumericCellValue());
                            case STRING:
                                return cell.getStringCellValue();
                            case BOOLEAN:
                                return String.valueOf(cell.getBooleanCellValue());
                            default:
                                return cell.getCellFormula();
                        }
                    } catch (Exception e) {
                        return cell.getCellFormula();
                    }
                default:
                    return "";
            }
        } catch (Exception e) {
            Log.w(TAG, "Error reading cell value", e);
            return "";
        }
    }

    private static void closeQuietly(AutoCloseable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Exception e) {
                Log.w(TAG, "Error closing resource", e);
            }
        }
    }
}