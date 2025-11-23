package com.main.anecnotes;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.main.anecnotes.utils.ClassRoom;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.ExcelExportHelper;
import com.main.anecnotes.utils.Note;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ClassroomDetailFragment extends Fragment {

    private static final int CREATE_EXCEL_FILE_REQUEST_CODE = 1001;
    private DatabaseHelper dbHelper;
    private static final String TAG = "ClassroomDetailFragment";
    private static final String ARG_CLASSROOM_ID = "classroom_id";
    private static final int EXPORT_PERMISSION_REQUEST_CODE = 1001;
    private int classroomId;
    private boolean isTabularView = false;
    private AlertDialog progressDialog;
    private boolean isExportInProgress = false;

    public ClassroomDetailFragment() {
        // Required empty public constructor
    }


    public static ClassroomDetailFragment newInstance(int classroomId) {
        ClassroomDetailFragment fragment = new ClassroomDetailFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CLASSROOM_ID, classroomId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            classroomId = getArguments().getInt(ARG_CLASSROOM_ID);
            Log.d(TAG, "ClassroomDetailFragment created with classroomId: " + classroomId);
        } else {
            Log.e(TAG, "No arguments provided to ClassroomDetailFragment");
        }
        dbHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classroom_detail, container, false);

        Log.d(TAG, "onCreateView called for classroomId: " + classroomId);

        Button backBtn = view.findViewById(R.id.back_btn);
        Button manageStudentsBtn = view.findViewById(R.id.manage_students_btn);
        Button toggleViewBtn = view.findViewById(R.id.toggle_view_btn);
        Button editClassroomBtn = view.findViewById(R.id.edit_classroom_btn); // Add this button
        Button exportNotesBtn = view.findViewById(R.id.export_notes_btn);
        exportNotesBtn.setOnClickListener(v -> {
            exportNotesToExcel(); // This now uses the modern SAF approach
        });

        // Load seat plan view by default
        loadSeatPlanView();
        toggleViewBtn.setText("Switch to Table View");

        // Add click listener for edit classroom button
        editClassroomBtn.setOnClickListener(v -> {
            showEditClassroomNameDialog();
        });

        toggleViewBtn.setOnClickListener(v -> {
            isTabularView = !isTabularView;
            if (isTabularView) {
                loadTabularView();
                toggleViewBtn.setText("Switch to Seat Plan");
            } else {
                loadSeatPlanView();
                toggleViewBtn.setText("Switch to Table View");
            }
        });

        manageStudentsBtn.setOnClickListener(v -> {
            StudentManagementFragment studentManagementFragment = StudentManagementFragment.newInstance(classroomId);
            ((MainActivity) requireActivity()).navigateToFragment(studentManagementFragment);
        });

        backBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToMainMenu();
        });

        return view;
    }

    private void showEditClassroomNameDialog() {
        // Get current classroom name
        String currentName = "";
        List<ClassRoom> classrooms = dbHelper.getAllClassrooms();
        for (com.main.anecnotes.utils.ClassRoom classroom : classrooms) {
            if (classroom.getId() == classroomId) {
                currentName = classroom.getClassName();
                break;
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Classroom Name");

        // Set up the input
        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(currentName);
        input.setSelection(input.getText().length()); // Place cursor at end

        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("Save", (dialog, which) -> {
            String newName = input.getText().toString().trim();
            if (!newName.isEmpty()) {
                boolean success = dbHelper.updateClassroomName(classroomId, newName);
                if (success) {
                    showToast("Classroom name updated successfully");
                    // Refresh the current view to show updated name
                    if (isTabularView) {
                        loadTabularView();
                    } else {
                        loadSeatPlanView();
                    }
                } else {
                    showToast("Failed to update classroom name");
                }
            } else {
                showToast("Classroom name cannot be empty");
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        builder.show();
    }



    private void showDeleteConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Classroom")
                .setMessage("Are you sure you want to delete this classroom?\n\nThis will also delete all students and notes associated with this classroom.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteCurrentClassroom();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCurrentClassroom() {
        dbHelper.softDeleteClassroom(classroomId);
        showToast("Classroom deleted successfully");

        // Navigate back to main menu
//        MainMenuFragment mainMenuFragment = new MainMenuFragment(this);
//        requireActivity().getSupportFragmentManager().beginTransaction()
//                .replace(R.id.fragment_container, mainMenuFragment)
//                .commit();

        ((MainActivity) requireActivity()).navigateToMainMenu();
    }
    private void loadTabularView() {
        TabularViewFragment tabularViewFragment = TabularViewFragment.newInstance(classroomId);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.classroom_view_container, tabularViewFragment)
                .commit();
    }

    private void loadSeatPlanView() {
        SeatPlanFragment seatPlanFragment = SeatPlanFragment.newInstance(classroomId);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.classroom_view_container, seatPlanFragment)
                .commit();
    }
    private void showToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }





    private void exportNotesToExcel() {
        if (isExportInProgress) {
            showToast("Export already in progress");
            return;
        }

        // Create file name with timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String fileName = "Anecdotal_Notes_" + timeStamp + ".xlsx";

        // Get classroom name for the file name suggestion
        String classroomName = "";
        List<ClassRoom> classrooms = dbHelper.getAllClassrooms();
        for (ClassRoom classroom : classrooms) {
            if (classroom.getId() == classroomId) {
                classroomName = classroom.getClassName().replaceAll("[^a-zA-Z0-9]", "_");
                break;
            }
        }

        if (!classroomName.isEmpty()) {
            fileName = "Anecdotal_Notes_" + classroomName + "_" + timeStamp + ".xlsx";
        }

        // Use Storage Access Framework to let user choose where to save
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);

        startActivityForResult(intent, CREATE_EXCEL_FILE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CREATE_EXCEL_FILE_REQUEST_CODE) {
            if (resultCode == getActivity().RESULT_OK && data != null && data.getData() != null) {
                Uri fileUri = data.getData();
                performExcelExport(fileUri);
            } else if (resultCode == getActivity().RESULT_CANCELED) {
                // User cancelled file selection
                isExportInProgress = false;
            }
        }
    }

    private void performExcelExport(Uri fileUri) {
        isExportInProgress = true;

        // Create and show progress dialog
        showProgressDialog("Exporting notes to Excel...");

        new Thread(() -> {
            boolean success = false;
            String errorMessage = null;

            try {
                DatabaseHelper dbHelper = new DatabaseHelper(requireContext());

                // Get classroom name
                String classroomName = "";
                List<ClassRoom> classrooms = dbHelper.getAllClassrooms();
                for (ClassRoom classroom : classrooms) {
                    if (classroom.getId() == classroomId) {
                        classroomName = classroom.getClassName();
                        break;
                    }
                }

                // Get notes for export
                List<DatabaseHelper.NoteExportData> exportData = dbHelper.getNotesForExport(classroomId);

                // Separate classroom and student notes
                List<Note> classroomNotes = new ArrayList<>();
                List<Note> studentNotes = new ArrayList<>();

                for (DatabaseHelper.NoteExportData data : exportData) {
                    if (data.isClassroomNote) {
                        classroomNotes.add(data.note);
                    } else {
                        // For student notes, modify the behavior field to include student name
                        Note note = data.note;
                        String originalBehavior = note.getBehavior();
                        note.setBehavior(data.studentName + " - " + originalBehavior);
                        studentNotes.add(note);
                    }
                }

                // Export to Excel using SAF
                success = ExcelExportHelper.exportNotesToExcel(requireContext(), fileUri, classroomNotes, studentNotes, classroomName);

            } catch (Exception e) {
                success = false;
                errorMessage = e.getMessage();
                Log.e(TAG, "Export error", e);
            }

            final boolean finalSuccess = success;
            final String finalErrorMessage = errorMessage;

            // Update UI on main thread
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    dismissProgressDialog();
                    isExportInProgress = false;

                    if (finalSuccess) {
                        showExportSuccessDialog(fileUri);
                    } else {
                        String message = "Failed to export notes to Excel";
                        if (finalErrorMessage != null) {
                            message += ": " + finalErrorMessage;
                        }
                        showToast(message);
                    }
                });
            } else {
                isExportInProgress = false;
            }
        }).start();
    }

    private void showProgressDialog(String message) {
        if (getActivity() == null || !isAdded()) return;

        getActivity().runOnUiThread(() -> {
            try {
                // Dismiss existing dialog if any
                dismissProgressDialog();

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View dialogView = getLayoutInflater().inflate(R.layout.dialog_progress, null);
                TextView messageText = dialogView.findViewById(R.id.progress_message);
                messageText.setText(message);

                builder.setView(dialogView);
                builder.setCancelable(false);

                progressDialog = builder.create();
                progressDialog.show();
            } catch (Exception e) {
                Log.e(TAG, "Error showing progress dialog", e);
            }
        });
    }

    private void dismissProgressDialog() {
        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    if (progressDialog != null && progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                    progressDialog = null;
                } catch (Exception e) {
                    Log.e(TAG, "Error dismissing progress dialog", e);
                }
            });
        }
    }

    private void showExportSuccessDialog(Uri fileUri) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Export Successful")
                .setMessage("Anecdotal notes have been exported successfully.\n\nWhat would you like to do?")
                .setPositiveButton("Share File", (dialog, which) -> {
                    shareExcelFile(fileUri);
                })
                .setNegativeButton("Open File", (dialog, which) -> {
                    openExcelFile(fileUri);
                })
                .setNeutralButton("OK", (dialog, which) -> {
                    // Just dismiss
                })
                .show();
    }

    private void shareExcelFile(Uri fileUri) {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri);
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create chooser intent
            Intent chooserIntent = Intent.createChooser(shareIntent, "Share Anecdotal Notes");

            startActivity(chooserIntent);
        } catch (ActivityNotFoundException e) {
            showToast("No app available to share Excel files");
        }
    }

    private void openExcelFile(Uri fileUri) {
        try {
            Intent openIntent = new Intent(Intent.ACTION_VIEW);
            openIntent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            openIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            // Create chooser intent
            Intent chooserIntent = Intent.createChooser(openIntent, "Open Excel File");

            startActivity(chooserIntent);
        } catch (ActivityNotFoundException e) {
            showToast("No app available to open Excel files");
        }
    }
    private void shareExcelFile(Uri fileUri, String classroomName) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        shareIntent.putExtra(Intent.EXTRA_STREAM, String.valueOf(fileUri));
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        // Create chooser intent
        Intent chooserIntent = Intent.createChooser(shareIntent, "Export Anecdotal Notes for " + classroomName);

        // Add option to save to device
        Intent saveIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        saveIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        saveIntent.putExtra(Intent.EXTRA_TITLE, "Anecdotal_Notes_" + classroomName + "_" +
                new SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(new Date()) + ".xlsx");
        saveIntent.putExtra(Intent.EXTRA_STREAM, fileUri);

        // Start the chooser
        try {
            startActivity(chooserIntent);
            showToast("Excel file exported successfully");
        } catch (ActivityNotFoundException e) {
            showToast("No app available to handle Excel files");
        }
    }
//    private void checkPermissionsAndExport() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION)
//                    != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[]{Manifest.permission.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION},
//                        EXPORT_PERMISSION_REQUEST_CODE);
//            } else {
//                exportNotesToExcel();
//            }
//        } else {
//            exportNotesToExcel();
//        }
//    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dismissProgressDialog();
        isExportInProgress = false;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        dismissProgressDialog();
        isExportInProgress = false;
    }

}