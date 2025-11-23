package com.main.anecnotes;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.Student;
import java.util.ArrayList;
import java.util.List;

public class StudentManagementFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";
    private int classroomId;
    private DatabaseHelper dbHelper;
    private ListView studentsListView;
    private EditText searchEditText;
    private Button clearSearchBtn;
    private List<Student> studentsList;
    private List<Student> filteredStudents;
    private ArrayAdapter<String> adapter;

    public StudentManagementFragment() {
        // Required empty public constructor
    }

    public static StudentManagementFragment newInstance(int classroomId) {
        StudentManagementFragment fragment = new StudentManagementFragment();
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
        }
        dbHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_management, container, false);

        studentsListView = view.findViewById(R.id.students_list_view);
        searchEditText = view.findViewById(R.id.search_edittext);
        clearSearchBtn = view.findViewById(R.id.clear_search_btn);
        Button addStudentBtn = view.findViewById(R.id.add_student_btn);
        Button backBtn = view.findViewById(R.id.back_btn);

        setupSearchFunctionality();
        loadStudents();

        addStudentBtn.setOnClickListener(v -> {
            showAddStudentDialog();
        });

        clearSearchBtn.setOnClickListener(v -> {
            searchEditText.setText("");
        });

        backBtn.setOnClickListener(v -> {
            // Go back to classroom detail
            ClassroomDetailFragment detailFragment = ClassroomDetailFragment.newInstance(classroomId);
            ((MainActivity) requireActivity()).navigateToFragment(detailFragment);
        });

        // Set up long click for delete
        studentsListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            Student student = filteredStudents.get(position);
            showDeleteConfirmationDialog(student);
            return true;
        });

        // Set up click for edit
        studentsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Student student = filteredStudents.get(position);
            showEditStudentDialog(student);
        });

        return view;
    }

    private void setupSearchFunctionality() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterStudents(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterStudents(String query) {
        filteredStudents.clear();

        if (query.isEmpty()) {
            filteredStudents.addAll(studentsList);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Student student : studentsList) {
                boolean matches = student.getFirstName().toLowerCase().contains(lowerCaseQuery) ||
                        student.getLastName().toLowerCase().contains(lowerCaseQuery) ||
                        student.getFullName().toLowerCase().contains(lowerCaseQuery) ||
                        student.getLRN().toLowerCase().contains(lowerCaseQuery);

                if (matches) {
                    filteredStudents.add(student);
                }
            }
        }
        refreshStudentList();
    }

    private void loadStudents() {
        studentsList = dbHelper.getStudentsByClassroom(classroomId);
        filteredStudents = new ArrayList<>(studentsList);
        refreshStudentList();
    }

    private void refreshStudentList() {
        if (adapter == null) {
            adapter = new ArrayAdapter<>(
                    getActivity(),
                    android.R.layout.simple_list_item_1
            );
            studentsListView.setAdapter(adapter);
        }

        adapter.clear();

        for (Student student : filteredStudents) {
            String genderSymbol = "Male".equalsIgnoreCase(student.getSex()) ? "♂" :
                    "Female".equalsIgnoreCase(student.getSex()) ? "♀" : "?";
            String displayText = student.getFullName() + " " + genderSymbol +
                    " (Seat: " + student.getSeatNumber() +
                    ", LRN: " + student.getLRN() + ")";
            adapter.add(displayText);
        }

        adapter.notifyDataSetChanged();

        // Show empty state if no students
        if (filteredStudents.isEmpty()) {
            if (searchEditText.getText().toString().isEmpty()) {
                showEmptyState("No students in this classroom");
            } else {
                showEmptyState("No students found matching \"" + searchEditText.getText().toString() + "\"");
            }
        } else {
            // Hide empty state if we have results
            studentsListView.setEmptyView(null);
        }
    }

    private void showEmptyState(String message) {
        TextView emptyText = new TextView(getActivity());
        emptyText.setText(message);
        emptyText.setPadding(16, 16, 16, 16);
        emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emptyText.setTextSize(16);

        // Remove any existing empty view
        if (studentsListView.getEmptyView() != null) {
            ((ViewGroup) studentsListView.getParent()).removeView(studentsListView.getEmptyView());
        }

        studentsListView.setEmptyView(emptyText);
        ((ViewGroup) studentsListView.getParent()).addView(emptyText);
    }

    // Updated to include LRN uniqueness check
    private void showAddStudentDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add New Student");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_student_edit, null);
        builder.setView(dialogView);

        EditText lrnEditText = dialogView.findViewById(R.id.edit_lrn);
        EditText lastNameEditText = dialogView.findViewById(R.id.edit_last_name);
        EditText firstNameEditText = dialogView.findViewById(R.id.edit_first_name);
        EditText middleNameEditText = dialogView.findViewById(R.id.edit_middle_name);
        Spinner sexSpinner = dialogView.findViewById(R.id.edit_sex);
        EditText seatNumberEditText = dialogView.findViewById(R.id.edit_seat_number);

        // Setup gender spinner
        setupGenderSpinner(sexSpinner);

        builder.setPositiveButton("Add", (dialog, which) -> {
            // Validation will be handled in the dialog
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String lrn = lrnEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String firstName = firstNameEditText.getText().toString().trim();
            String middleName = middleNameEditText.getText().toString().trim();
            String sex = sexSpinner.getSelectedItem().toString();
            String seatNumberStr = seatNumberEditText.getText().toString().trim();

            if (validateStudentInput(lrn, lastName, firstName, true)) {
                // Check for duplicate LRN
                if (isLrnDuplicate(lrn, -1)) { // -1 for new student (no ID yet)
                    lrnEditText.setError("LRN already exists in this classroom");
                    lrnEditText.requestFocus();
                    return;
                }

                Student student = Student.createBasicStudent(lrn, lastName, firstName);
                student.setMiddleName(middleName);
                student.setSex(sex);

                try {
                    int seatNumber = seatNumberStr.isEmpty() ? 0 : Integer.parseInt(seatNumberStr);
                    student.setSeatNumber(seatNumber);
                } catch (NumberFormatException e) {
                    student.setSeatNumber(0);
                }

                long result = dbHelper.addStudent(student, classroomId);
                if (result != -1) {
                    showToast("Student added successfully");
                    loadStudents(); // Refresh the list
                    dialog.dismiss();
                } else {
                    showToast("Failed to add student");
                }
            }
        });
    }

    // Updated to include LRN uniqueness check
    private void showEditStudentDialog(Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Student");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_student_edit, null);
        builder.setView(dialogView);

        EditText lrnEditText = dialogView.findViewById(R.id.edit_lrn);
        EditText lastNameEditText = dialogView.findViewById(R.id.edit_last_name);
        EditText firstNameEditText = dialogView.findViewById(R.id.edit_first_name);
        EditText middleNameEditText = dialogView.findViewById(R.id.edit_middle_name);
        Spinner sexSpinner = dialogView.findViewById(R.id.edit_sex);
        EditText seatNumberEditText = dialogView.findViewById(R.id.edit_seat_number);

        // Setup gender spinner
        setupGenderSpinner(sexSpinner);

        // Pre-fill with current data
        lrnEditText.setText(student.getLRN());
        lastNameEditText.setText(student.getLastName());
        firstNameEditText.setText(student.getFirstName());
        middleNameEditText.setText(student.getMiddleName());
        seatNumberEditText.setText(String.valueOf(student.getSeatNumber()));

        // Set current gender in spinner
        String currentSex = student.getSex();
        if (currentSex != null && !currentSex.isEmpty()) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) sexSpinner.getAdapter();
            int position = adapter.getPosition(currentSex);
            if (position >= 0) {
                sexSpinner.setSelection(position);
            }
        }

        builder.setPositiveButton("Update", (dialog, which) -> {
            // Validation will be handled in the dialog
        });

        builder.setNegativeButton("Cancel", null);

        AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            String lrn = lrnEditText.getText().toString().trim();
            String lastName = lastNameEditText.getText().toString().trim();
            String firstName = firstNameEditText.getText().toString().trim();
            String middleName = middleNameEditText.getText().toString().trim();
            String sex = sexSpinner.getSelectedItem().toString();
            String seatNumberStr = seatNumberEditText.getText().toString().trim();

            if (validateStudentInput(lrn, lastName, firstName, false)) {
                // Check for duplicate LRN (excluding current student)
                if (!lrn.equals(student.getLRN()) && isLrnDuplicate(lrn, student.getId())) {
                    lrnEditText.setError("LRN already exists in this classroom");
                    lrnEditText.requestFocus();
                    return;
                }

                student.setLRN(lrn);
                student.setLastName(lastName);
                student.setFirstName(firstName);
                student.setMiddleName(middleName);
                student.setSex(sex);

                try {
                    int seatNumber = seatNumberStr.isEmpty() ? 0 : Integer.parseInt(seatNumberStr);
                    student.setSeatNumber(seatNumber);
                } catch (NumberFormatException e) {
                    student.setSeatNumber(0);
                }

                boolean success = dbHelper.updateStudent(student);
                if (success) {
                    showToast("Student updated successfully");
                    loadStudents(); // Refresh the list
                    dialog.dismiss();
                } else {
                    showToast("Failed to update student");
                }
            }
        });
    }

    private boolean validateStudentInput(String lrn, String lastName, String firstName, boolean isNew) {
        // Clear previous errors
        searchEditText.setError(null);

        if (lastName.isEmpty()) {
            showToast("Last name is required");
            return false;
        }

        if (firstName.isEmpty()) {
            showToast("First name is required");
            return false;
        }

        if (lrn.isEmpty()) {
            showToast("LRN is required");
            return false;
        }

        // Validate LRN format (adjust as needed for your requirements)
        if (!lrn.matches("\\d+")) {
            showToast("LRN must contain only numbers");
            return false;
        }

        if (lrn.length() < 10 || lrn.length() > 15) {
            showToast("LRN must be between 10-15 digits");
            return false;
        }

        return true;
    }

    private boolean isLrnDuplicate(String lrn, int excludeStudentId) {
        return dbHelper.isLrnExistsInClassroom(lrn, classroomId, excludeStudentId);
    }

    private void setupGenderSpinner(Spinner sexSpinner) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(adapter);
    }

    private void showDeleteConfirmationDialog(Student student) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Student")
                .setMessage("Are you sure you want to delete " + student.getFullName() + "?\n\nThis will also delete all notes associated with this student.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    boolean success = dbHelper.deleteStudent(student.getId());
                    if (success) {
                        showToast("Student deleted successfully");
                        loadStudents(); // Refresh the list
                    } else {
                        showToast("Failed to delete student");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStudents(); // Refresh when returning from other fragments
    }
}