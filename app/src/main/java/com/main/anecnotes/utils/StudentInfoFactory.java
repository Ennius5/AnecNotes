package com.main.anecnotes.utils;

import java.util.ArrayList;
import java.util.List;

public class StudentInfoFactory {

    public static class ColumnMapping {
        private int lrnColumn = 0;      // Column A (0-based index)
        private int lastNameColumn = 2;  // Column C
        private int firstNameColumn = 3; // Column D
        private int middleNameColumn = 4; // Column E
        private int sexColumn = 6;       // Column G
        private int startRow = 6;        // Row 7 (0-based index)

        // Getters and setters
        public int getLrnColumn() { return lrnColumn; }
        public void setLrnColumn(int lrnColumn) { this.lrnColumn = lrnColumn; }

        public int getLastNameColumn() { return lastNameColumn; }
        public void setLastNameColumn(int lastNameColumn) { this.lastNameColumn = lastNameColumn; }

        public int getFirstNameColumn() { return firstNameColumn; }
        public void setFirstNameColumn(int firstNameColumn) { this.firstNameColumn = firstNameColumn; }

        public int getMiddleNameColumn() { return middleNameColumn; }
        public void setMiddleNameColumn(int middleNameColumn) { this.middleNameColumn = middleNameColumn; }

        public int getSexColumn() { return sexColumn; }
        public void setSexColumn(int sexColumn) { this.sexColumn = sexColumn; }

        public int getStartRow() { return startRow; }
        public void setStartRow(int startRow) { this.startRow = startRow; }
    }

    public static List<Student> createStudentsFromExcelData(List<List<String>> excelData, ColumnMapping mapping) {
        List<Student> students = new ArrayList<>();

        if (excelData == null || excelData.isEmpty()) {
            return students;
        }

        for (int i = mapping.getStartRow(); i < excelData.size(); i++) {
            List<String> row = excelData.get(i);

            // Skip rows that don't have the minimum required data
            if (!isValidStudentRow(row, mapping)) {
                continue;
            }

            try {
                String lrn = getCellValue(row, mapping.getLrnColumn());
                String lastName = getCellValue(row, mapping.getLastNameColumn());
                String firstName = getCellValue(row, mapping.getFirstNameColumn());
                String middleName = getCellValue(row, mapping.getMiddleNameColumn());
                String sex = normalizeGender(getCellValue(row, mapping.getSexColumn()));

                // Create basic student

                Student student = Student.createBasicStudent(lrn, lastName, firstName);
                student.setMiddleName(middleName);
                student.setSex(sex);

                students.add(student);

            } catch (Exception e) {
                // Skip this row if there's an error
                continue;
            }
        }

        return students;
    }

    private static boolean isValidStudentRow(List<String> row, ColumnMapping mapping) {
        // Check if row has minimum required columns
        if (row.size() <= Math.max(mapping.getLrnColumn(),
                Math.max(mapping.getLastNameColumn(), mapping.getFirstNameColumn()))) {
            return false;
        }

        String lrn = getCellValue(row, mapping.getLrnColumn());
        String lastName = getCellValue(row, mapping.getLastNameColumn());
        String firstName = getCellValue(row, mapping.getFirstNameColumn());

        // Skip rows missing essential data
        if (lastName == null || lastName.trim().isEmpty() ||
                firstName == null || firstName.trim().isEmpty()) {
            return false;
        }

        return true;
    }

    private static String getCellValue(List<String> row, int columnIndex) {
        if (columnIndex < 0 || columnIndex >= row.size()) {
            return "";
        }
        String value = row.get(columnIndex);
        return value != null ? value.trim() : "";
    }

    private static String normalizeGender(String gender) {
        if (gender == null) return "Male"; // Default

        String normalized = gender.trim().toLowerCase();
        if (normalized.startsWith("f") || normalized.equals("girl") || normalized.equals("female")) {
            return "Female";
        } else if (normalized.startsWith("m") || normalized.equals("boy") || normalized.equals("male")) {
            return "Male";
        }

        return "Male"; // Default fallback
    }

    // Method to detect column mappings automatically
    public static ColumnMapping detectColumnMapping(List<List<String>> sampleData) {
        ColumnMapping mapping = new ColumnMapping();

        if (sampleData == null || sampleData.isEmpty()) {
            return mapping;
        }

        // Look for header row to detect columns
//        for (int row = 0; row < Math.min(10, sampleData.size()); row++) {
//            List<String> headerRow = sampleData.get(row);
//
//            for (int col = 0; col < headerRow.size(); col++) {
//                String cell = headerRow.get(col).toLowerCase();
//
//                if (cell.contains("lrn") || cell.contains("id") || cell.contains("student")) {
//                    mapping.setLrnColumn(col);
//                } else if (cell.contains("last") || cell.contains("surname") || cell.contains("family")) {
//                    mapping.setLastNameColumn(col);
//                } else if (cell.contains("first") || cell.contains("given")) {
//                    mapping.setFirstNameColumn(col);
//                } else if (cell.contains("middle") || cell.contains("initial")) {
//                    mapping.setMiddleNameColumn(col);
//                } else if (cell.contains("sex") || cell.contains("gender")) {
//                    mapping.setSexColumn(col);
//                }
//            }
//
//            // If we found reasonable headers, set this as start row + 1
//            if (mapping.getLastNameColumn() != 2 || mapping.getFirstNameColumn() != 3) {
//                mapping.setStartRow(row + 1);
//                break;
//            }
//        }

        return mapping;
    }
}