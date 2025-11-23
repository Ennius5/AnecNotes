package com.main.anecnotes;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import com.main.anecnotes.utils.ClassRoom;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.ExcelReader;
import com.main.anecnotes.R;
import com.main.anecnotes.utils.Student;
import com.main.anecnotes.utils.StudentInfoFactory;

import java.util.ArrayList;
import java.util.List;

public class ExcelReaderFragment extends Fragment {
    private static final String TAG = "ExcelReaderFragment";
    private Button btnImportStudents;
    private int targetClassroomId = -1;
    private StudentInfoFactory.ColumnMapping columnMapping = new StudentInfoFactory.ColumnMapping();

    private Button btnSelectFile, btnLoadSheets, btnReadData;
    private ListView listViewData;
    private ProgressBar progressBar;
    private Spinner spinnerSheets;
    private EditText etStartRow, etEndRow;
    private TextView tvFileInfo;

    private List<List<String>> excelData = new ArrayList<>();
    private List<String> sheetNames = new ArrayList<>();
    private Uri currentFileUri;
    private ArrayAdapter<String> sheetAdapter;
    private Button backBtn;



    private final ActivityResultLauncher<Intent> filePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getData() != null && result.getData().getData() != null) {
                            currentFileUri = result.getData().getData();

                            // Take persistent permission
                            try {
                                requireContext().getContentResolver().takePersistableUriPermission(
                                        currentFileUri,
                                        Intent.FLAG_GRANT_READ_URI_PERMISSION
                                );
                            } catch (SecurityException e) {
                                Log.w(TAG, "Could not take persistent permission: " + e.getMessage());
                            }

                            displayFileInfo();
                            resetSheetSelection();

                            // Debug info
                            Log.d(TAG, "Selected URI: " + currentFileUri.toString());
                            Log.d(TAG, "URI Scheme: " + currentFileUri.getScheme());
                            Log.d(TAG, "URI Authority: " + currentFileUri.getAuthority());
                            Log.d(TAG, "URI Path: " + currentFileUri.getPath());
                        }
                    });

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_excel_reader, container, false);
        initViews(view);
        setupClickListeners();
        setupSpinner();
        return view;
    }

    private void initViews(View view) {
        backBtn = view.findViewById(R.id.back_btn);
        btnSelectFile = view.findViewById(R.id.btnSelectFile);
        btnLoadSheets = view.findViewById(R.id.btnLoadSheets);
        btnReadData = view.findViewById(R.id.btnReadData);
        listViewData = view.findViewById(R.id.listViewData);
        progressBar = view.findViewById(R.id.progressBar);
        spinnerSheets = view.findViewById(R.id.spinnerSheets);
        etStartRow = view.findViewById(R.id.etStartRow);
        etEndRow = view.findViewById(R.id.etEndRow);
        tvFileInfo = view.findViewById(R.id.tvFileInfo);
        btnImportStudents = view.findViewById(R.id.btnImportStudents);
        btnImportStudents.setVisibility(View.GONE); // Hide by default
    }

    private void setupImportFunctionality() {
        btnImportStudents.setOnClickListener(v -> {
            if (excelData != null && !excelData.isEmpty()) {
                showImportDialog();
            } else {
                showToast("Please load Excel data first");
            }
        });
    }

    private void setupSpinner() {
        sheetAdapter = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, new ArrayList<>());
        sheetAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSheets.setAdapter(sheetAdapter);
    }

    private void setupClickListeners() {
        btnSelectFile.setOnClickListener(v -> openFilePicker());
        btnLoadSheets.setOnClickListener(v -> loadSheetNames());
        btnReadData.setOnClickListener(v -> readSelectedData());
        backBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToMainMenu();
        });
    }

    private void openFilePicker() {
        try {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            intent.setType("*/*");

            String[] mimeTypes = {
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                    "application/vnd.ms-excel.sheet.macroEnabled.12",
                    "application/vnd.ms-excel.template.macroEnabled.12",
                    "application/octet-stream" // Fallback
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            // Add flags for persistent permission
            intent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            filePickerLauncher.launch(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error with OPEN_DOCUMENT intent, falling back", e);
            // Fallback to GET_CONTENT
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);

            String[] mimeTypes = {
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
            intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);

            filePickerLauncher.launch(intent);
        }
    }

    private void displayFileInfo() {
        if (currentFileUri != null) {
            String fileName = getFileNameFromUri(currentFileUri);
            String displayText = "Selected: " + (fileName != null ? fileName : "Unknown file") +
                    "\nURI: " + currentFileUri.toString();

            tvFileInfo.setText(displayText);
            tvFileInfo.setVisibility(View.VISIBLE);
        }
    }

    private String getFileNameFromUri(Uri uri) {
        String fileName = null;
        try {
            // Try to get display name from content resolver
            try (android.database.Cursor cursor = requireContext().getContentResolver().query(
                    uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        fileName = cursor.getString(nameIndex);
                    }
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not get file name from URI", e);
        }

        // Fallback to last path segment
        if (fileName == null) {
            fileName = uri.getLastPathSegment();
        }

        return fileName;
    }

    private void resetSheetSelection() {
        sheetAdapter.clear();
        sheetAdapter.notifyDataSetChanged();
        btnLoadSheets.setEnabled(true);
        btnReadData.setEnabled(false);
    }

    private void loadSheetNames() {
        if (currentFileUri == null) {
            showToast("Please select a file first");
            return;
        }

        showLoading(true);
        btnLoadSheets.setEnabled(false);

        ExcelReader.getSheetNames(requireContext(), currentFileUri, new ExcelReader.ExcelReadListener() {
            @Override
            public void onSheetsListLoaded(List<String> sheets) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    sheetNames = sheets;
                    updateSheetSpinner(sheets);
                    btnReadData.setEnabled(true);
                    showToast("Loaded " + sheets.size() + " sheets");
                });
            }

            @Override
            public void onExcelDataRead(List<List<String>> data) {
                // Not used here
            }

            @Override
            public void onExcelReadError(String errorMessage) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    btnLoadSheets.setEnabled(true);
                    showToast("Error: " + errorMessage);
                    Log.e(TAG, "Error loading sheets: " + errorMessage);
                });
            }
        });
    }

    private void updateSheetSpinner(List<String> sheets) {
        sheetAdapter.clear();
        if (sheets != null && !sheets.isEmpty()) {
            sheetAdapter.addAll(sheets);
            spinnerSheets.setSelection(0);
        } else {
            showToast("No sheets found in the file");
        }
        sheetAdapter.notifyDataSetChanged();
    }

    private void readSelectedData() {
        if (currentFileUri == null || sheetNames.isEmpty()) {
            showToast("Please load sheet names first");
            return;
        }

        String selectedSheet = (String) spinnerSheets.getSelectedItem();
        if (selectedSheet == null) {
            showToast("Please select a sheet");
            return;
        }

        int startRow = parseRowInput(etStartRow.getText().toString(), 0);
        int endRow = parseRowInput(etEndRow.getText().toString(), Integer.MAX_VALUE);

        if (startRow < 0 || endRow < 0) {
            showToast("Please enter valid row numbers (0 or greater)");
            return;
        }

        if (startRow > endRow) {
            showToast("Start row cannot be greater than end row");
            return;
        }

        showLoading(true);

        ExcelReader.readExcelFile(requireContext(), currentFileUri, selectedSheet,
                startRow, endRow, new ExcelReader.ExcelReadListener() {
                    @Override
                    public void onExcelDataRead(List<List<String>> data) {
                        requireActivity().runOnUiThread(() -> {
                            showLoading(false);
                            excelData = data;
                            displayExcelData();
                            String message = String.format("Read %d rows from sheet '%s'",
                                    data.size(), selectedSheet);
                            showToast(message);
                        });
                    }

                    @Override
                    public void onExcelReadError(String errorMessage) {
                        requireActivity().runOnUiThread(() -> {
                            showLoading(false);
                            showToast("Error: " + errorMessage);
                            Log.e(TAG, "Error reading data: " + errorMessage);
                        });
                    }

                    @Override
                    public void onSheetsListLoaded(List<String> sheetNames) {
                        // Not used here
                    }
                });
    }

    private int parseRowInput(String input, int defaultValue) {
        if (input == null || input.trim().isEmpty()) {
            return defaultValue;
        }
        try {
            int value = Integer.parseInt(input.trim());
            return Math.max(value, 0); // Ensure non-negative
        } catch (NumberFormatException e) {
            return -1; // Indicates invalid input
        }
    }

    private void displayExcelData() {
        if (excelData == null || excelData.isEmpty()) {
            listViewData.setAdapter(null);
            showToast("No data to display");
            btnImportStudents.setVisibility(View.GONE);
            return;
        }

        List<String> displayList = new ArrayList<>();


        columnMapping = StudentInfoFactory.detectColumnMapping(excelData);

        for (int i = 0; i < Math.min(excelData.size(), 100); i++) { // Show first 100 rows max
            List<String> row = excelData.get(i);
            StringBuilder rowText = new StringBuilder("Row " + (i + 1) + ": ");

            // Highlight detected student data
            if (i >= columnMapping.getStartRow()) {
                if (isValidStudentRow(row)) {
                    String lastName = getCellValue(row, columnMapping.getLastNameColumn());
                    String firstName = getCellValue(row, columnMapping.getFirstNameColumn());
                    rowText.append("👤 ");
                    rowText.append(firstName).append(" ").append(lastName);

                    String lrn = getCellValue(row, columnMapping.getLrnColumn());
                    if (!lrn.isEmpty()) {
                        rowText.append(" [LRN:").append(lrn).append("]");
                    }
                } else {
                    rowText.append("❌ Invalid/Empty");
                }
            } else {
                rowText.append("📋 Header");
            }

            // Show column mapping info for first few rows
            if (i < 3) {
                rowText.append("\n      Mapping: LRN=").append(columnMapping.getLrnColumn() + 1)
                        .append(", Last=").append(columnMapping.getLastNameColumn() + 1)
                        .append(", First=").append(columnMapping.getFirstNameColumn() + 1)
                        .append(", Middle=").append(columnMapping.getMiddleNameColumn() + 1)
                        .append(", Sex=").append(columnMapping.getSexColumn() + 1)
                        .append(", Start=").append(columnMapping.getStartRow() + 1);
            }

            displayList.add(rowText.toString());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_list_item_1,
                displayList
        );

        listViewData.setAdapter(adapter);

        // Show import button if we have valid classroom context
        if (targetClassroomId != -1) {
            btnImportStudents.setVisibility(View.VISIBLE);
            setupImportFunctionality();
        }
    }

    private boolean isValidStudentRow(List<String> row) {
        String lastName = getCellValue(row, columnMapping.getLastNameColumn());
        String firstName = getCellValue(row, columnMapping.getFirstNameColumn());
        return !lastName.isEmpty() && !firstName.isEmpty();
    }

    private String getCellValue(List<String> row, int column) {
        if (column < 0 || column >= row.size()) return "";
        String value = row.get(column);
        return value != null ? value.trim() : "";
    }

    private void showImportDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Import Students");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_import_students, null);
        builder.setView(dialogView);

        // Get classroom list for selection
        DatabaseHelper dbHelper = new DatabaseHelper(requireContext());
        List<ClassRoom> classrooms = dbHelper.getAllClassrooms();

        Spinner classroomSpinner = dialogView.findViewById(R.id.classroom_spinner);
        EditText startRowEditText = dialogView.findViewById(R.id.start_row_edittext);
        CheckBox skipInvalidRowsCheckbox = dialogView.findViewById(R.id.skip_invalid_checkbox);
        TextView mappingInfoText = dialogView.findViewById(R.id.mapping_info_text);
        Button configureMappingBtn = dialogView.findViewById(R.id.configure_mapping_btn);

        // Setup classroom spinner
        ArrayAdapter<ClassRoom> classroomAdapter = new ArrayAdapter<ClassRoom>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                classrooms
        ) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getView(position, convertView, parent);
                textView.setText(getItem(position).getClassName());
                return textView;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                TextView textView = (TextView) super.getDropDownView(position, convertView, parent);
                textView.setText(getItem(position).getClassName());
                return textView;
            }
        };
        classroomSpinner.setAdapter(classroomAdapter);

        // Set default values
        startRowEditText.setText(String.valueOf(columnMapping.getStartRow() + 1));

        // Show mapping info
        mappingInfoText.setText(String.format(
                "Current Mapping:\nLRN: Column %d\nLast Name: Column %d\nFirst Name: Column %d\nMiddle Name: Column %d\nGender: Column %d",
                columnMapping.getLrnColumn() + 1,
                columnMapping.getLastNameColumn() + 1,
                columnMapping.getFirstNameColumn() + 1,
                columnMapping.getMiddleNameColumn() + 1,
                columnMapping.getSexColumn() + 1
        ));

        configureMappingBtn.setOnClickListener(v -> {
            showColumnMappingDialog();
        });

        builder.setPositiveButton("Import", (dialog, which) -> {
            ClassRoom selectedClassroom = (ClassRoom) classroomSpinner.getSelectedItem();
            int startRow = Integer.parseInt(startRowEditText.getText().toString()) - 1; // Convert to 0-based
            boolean skipInvalid = skipInvalidRowsCheckbox.isChecked();

            if (selectedClassroom != null) {
                importStudentsToClassroom(selectedClassroom.getId(), startRow, skipInvalid);
            } else {
                showToast("Please select a classroom");
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showColumnMappingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Configure Column Mapping");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_column_mapping, null);
        builder.setView(dialogView);

        EditText lrnColumnEdit = dialogView.findViewById(R.id.lrn_column_edit);
        EditText lastNameColumnEdit = dialogView.findViewById(R.id.last_name_column_edit);
        EditText firstNameColumnEdit = dialogView.findViewById(R.id.first_name_column_edit);
        EditText middleNameColumnEdit = dialogView.findViewById(R.id.middle_name_column_edit);
        EditText sexColumnEdit = dialogView.findViewById(R.id.sex_column_edit);
        EditText startRowEdit = dialogView.findViewById(R.id.start_row_edit);

        // Set current values (convert to 1-based for user)
        lrnColumnEdit.setText(String.valueOf(columnMapping.getLrnColumn() + 1));
        lastNameColumnEdit.setText(String.valueOf(columnMapping.getLastNameColumn() + 1));
        firstNameColumnEdit.setText(String.valueOf(columnMapping.getFirstNameColumn() + 1));
        middleNameColumnEdit.setText(String.valueOf(columnMapping.getMiddleNameColumn() + 1));
        sexColumnEdit.setText(String.valueOf(columnMapping.getSexColumn() + 1));
        startRowEdit.setText(String.valueOf(columnMapping.getStartRow() + 1));

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                columnMapping.setLrnColumn(Integer.parseInt(lrnColumnEdit.getText().toString()) - 1);
                columnMapping.setLastNameColumn(Integer.parseInt(lastNameColumnEdit.getText().toString()) - 1);
                columnMapping.setFirstNameColumn(Integer.parseInt(firstNameColumnEdit.getText().toString()) - 1);
                columnMapping.setMiddleNameColumn(Integer.parseInt(middleNameColumnEdit.getText().toString()) - 1);
                columnMapping.setSexColumn(Integer.parseInt(sexColumnEdit.getText().toString()) - 1);
                columnMapping.setStartRow(Integer.parseInt(startRowEdit.getText().toString()) - 1);

                showToast("Column mapping updated");
                displayExcelData(); // Refresh display
            } catch (NumberFormatException e) {
                showToast("Please enter valid column numbers");
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void importStudentsToClassroom(int classroomId, int startRow, boolean skipInvalid) {
        showLoading(true);

        new Thread(() -> {
            try {
                DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

                // Update the column mapping with the user-specified start row
                columnMapping.setStartRow(startRow);

                List<Student> students = StudentInfoFactory.createStudentsFromExcelData(
                        excelData,
                        columnMapping
                );

                int successCount = 0;
                int errorCount = 0;
                int duplicateCount = 0;
                List<String> duplicateLRNs = new ArrayList<>();

                for (Student student : students) {
                    try {
                        // Check for duplicate LRN before adding
                        if (dbHelper.isLrnExistsInClassroom(student.getLRN(), classroomId, -1)) {
                            duplicateCount++;
                            duplicateLRNs.add(student.getLRN() + " - " + student.getFullName());
                            if (skipInvalid) {
                                continue; // Skip this student and move to next
                            } else {
                                errorCount++;
                                continue;
                            }
                        }

                        // Assign sequential seat numbers starting from 1
                        student.setSeatNumber(successCount + 1);

                        long studentId = dbHelper.addStudent(student, classroomId);
                        if (studentId != -1) {
                            successCount++;
                        } else {
                            errorCount++;
                        }
                    } catch (Exception e) {
                        errorCount++;
                    }
                }

                final int finalSuccessCount = successCount;
                final int finalErrorCount = errorCount;
                final int finalDuplicateCount = duplicateCount;
                final List<String> finalDuplicateLRNs = new ArrayList<>(duplicateLRNs);

                requireActivity().runOnUiThread(() -> {
                    showLoading(false);

                    StringBuilder message = new StringBuilder();
                    message.append(String.format("Import complete:\n%d students added\n%d errors",
                            finalSuccessCount, finalErrorCount));

                    if (finalDuplicateCount > 0) {
                        message.append(String.format("\n%d duplicate LRNs skipped", finalDuplicateCount));

                        // Show duplicate details in a dialog
                        if (!finalDuplicateLRNs.isEmpty()) {
                            showDuplicateLRNsDialog(finalDuplicateLRNs);
                        }
                    }

                    showToast(message.toString());

                    // Navigate back to classroom detail if students were added
                    if (finalSuccessCount > 0) {
                        ClassroomDetailFragment detailFragment = ClassroomDetailFragment.newInstance(classroomId);
                        ((MainActivity) requireActivity()).navigateToFragment(detailFragment);
                    }
                });

            } catch (Exception e) {
                requireActivity().runOnUiThread(() -> {
                    showLoading(false);
                    showToast("Import failed: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showDuplicateLRNsDialog(List<String> duplicateLRNs) {
        StringBuilder message = new StringBuilder("Duplicate LRNs found and skipped:\n\n");
        for (int i = 0; i < Math.min(duplicateLRNs.size(), 10); i++) { // Show first 10
            message.append("• ").append(duplicateLRNs.get(i)).append("\n");
        }

        if (duplicateLRNs.size() > 10) {
            message.append("\n... and ").append(duplicateLRNs.size() - 10).append(" more");
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Duplicate LRNs")
                .setMessage(message.toString())
                .setPositiveButton("OK", null)
                .show();
    }

    // Add method to set target classroom (call this when navigating from classroom)
    public void setTargetClassroom(int classroomId) {
        this.targetClassroomId = classroomId;
        if (btnImportStudents != null) {
            btnImportStudents.setVisibility(classroomId != -1 ? View.VISIBLE : View.GONE);
        }
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnSelectFile.setEnabled(!show);
        btnLoadSheets.setEnabled(!show && currentFileUri != null);
        btnReadData.setEnabled(!show && !sheetNames.isEmpty());
    }

    private void showToast(String message) {
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }
}