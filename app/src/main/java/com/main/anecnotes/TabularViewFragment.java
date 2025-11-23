package com.main.anecnotes;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.Student;
import java.util.ArrayList;
import java.util.List;

public class TabularViewFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";
    private int classroomId;
    private DatabaseHelper dbHelper;
    private TableLayout tableLayout;
    private EditText searchEditText;
    private List<Student> allStudents;
    private List<Student> filteredStudents;

    public TabularViewFragment() {
        // Required empty public constructor
    }

    public static TabularViewFragment newInstance(int classroomId) {
        TabularViewFragment fragment = new TabularViewFragment();
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
        View view = inflater.inflate(R.layout.fragment_tabular_view, container, false);

        tableLayout = view.findViewById(R.id.students_table);
        searchEditText = view.findViewById(R.id.search_edittext);

        setupSearchFunctionality();
        loadStudentsIntoTable();

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
            filteredStudents.addAll(allStudents);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            for (Student student : allStudents) {
                if (student.getFirstName().toLowerCase().contains(lowerCaseQuery) ||
                        student.getLastName().toLowerCase().contains(lowerCaseQuery) ||
                        student.getLRN().toLowerCase().contains(lowerCaseQuery) ||
                        student.getFullName().toLowerCase().contains(lowerCaseQuery)) {
                    filteredStudents.add(student);
                }
            }
        }
        refreshTable();
    }

    private void loadStudentsIntoTable() {
        allStudents = dbHelper.getStudentsByClassroom(classroomId);
        filteredStudents = new ArrayList<>(allStudents);
        refreshTable();
    }

    private void refreshTable() {
        tableLayout.removeAllViews();
        createTableHeader();

        for (Student student : filteredStudents) {
            TableRow studentRow = createStudentRow(student);
            tableLayout.addView(studentRow);
        }

        // Show empty state if no students found
        if (filteredStudents.isEmpty()) {
            showEmptyState();
        }
    }

    private void createTableHeader() {
        TableRow headerRow = new TableRow(getActivity());
        headerRow.setBackgroundResource(android.R.color.darker_gray);

        headerRow.addView(createHeaderTextView("Seat #", 1f));
        headerRow.addView(createHeaderTextView("LRN", 1.5f));
        headerRow.addView(createHeaderTextView("Last Name", 1.2f));
        headerRow.addView(createHeaderTextView("First Name", 1.2f));
        headerRow.addView(createHeaderTextView("Gender", 0.8f)); // Add gender column
        headerRow.addView(createHeaderTextView("Actions", 1.5f));

        tableLayout.addView(headerRow);
    }

    private TableRow createStudentRow(Student student) {
        TableRow studentRow = new TableRow(getActivity());

        // Alternate row colors for better readability
        if (filteredStudents.indexOf(student) % 2 == 0) {
            studentRow.setBackgroundResource(android.R.color.background_light);
        } else {
            studentRow.setBackgroundResource(android.R.color.white);
        }

        studentRow.addView(createTableCell(String.valueOf(student.getSeatNumber()), 1f));
        studentRow.addView(createTableCell(student.getLRN(), 1.5f));
        studentRow.addView(createTableCell(student.getLastName(), 1.2f));
        studentRow.addView(createTableCell(student.getFirstName(), 1.2f));

        // Add gender cell with symbol
        String genderSymbol = "Male".equalsIgnoreCase(student.getSex()) ? "♂" :
                "Female".equalsIgnoreCase(student.getSex()) ? "♀" : "?";
        studentRow.addView(createTableCell(genderSymbol, 0.8f));

        // Actions container
        LinearLayout actionsLayout = createActionsLayout(student);
        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1.5f);
        actionsLayout.setLayoutParams(params);
        studentRow.addView(actionsLayout);

        // Add click listener for the entire row
        studentRow.setOnClickListener(v -> {
            showStudentQuickActions(student);
        });

        return studentRow;
    }

    private LinearLayout createActionsLayout(Student student) {
        LinearLayout actionsLayout = new LinearLayout(getActivity());
        actionsLayout.setOrientation(LinearLayout.HORIZONTAL);
        actionsLayout.setPadding(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        // Add Note button
        TextView addNoteButton = createActionButton("Add Note", android.R.color.holo_blue_light);
        addNoteButton.setOnClickListener(v -> {
            openNoteFragment(student);
        });

        // View Notes button
        TextView viewNotesButton = createActionButton("View Notes", android.R.color.holo_green_light);
        viewNotesButton.setOnClickListener(v -> {
            openNotesFragment(student);
        });

        actionsLayout.addView(addNoteButton);
        actionsLayout.addView(viewNotesButton);

        return actionsLayout;
    }

    private TextView createActionButton(String text, int colorResource) {
        TextView button = new TextView(getActivity());
        button.setText(text);
        button.setPadding(dpToPx(8), dpToPx(4), dpToPx(8), dpToPx(4));
        button.setBackgroundResource(colorResource);
        button.setTextColor(getResources().getColor(android.R.color.black));
        button.setTextSize(10);
        button.setLayoutParams(new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f));
        button.setMinWidth(dpToPx(4));

        // Make it look more like a button
        button.setClickable(true);
        button.setFocusable(true);
        button.setBackground(getResources().getDrawable(android.R.drawable.btn_default));

        return button;
    }

    private TextView createHeaderTextView(String text, float weight) {
        TextView textView = new TextView(getActivity());
        textView.setText(text);
        textView.setPadding(dpToPx(8), dpToPx(12), dpToPx(8), dpToPx(12));
        textView.setTextColor(getResources().getColor(android.R.color.white));
        textView.setTextSize(14);
        textView.setTypeface(null, android.graphics.Typeface.BOLD);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        textView.setLayoutParams(params);

        return textView;
    }

    private TextView createTableCell(String text, float weight) {
        TextView textView = new TextView(getActivity());
        textView.setText(text != null ? text : "");
        textView.setTextColor(Color.BLACK);
        textView.setPadding(dpToPx(8), dpToPx(8), dpToPx(8), dpToPx(8));
        textView.setTextSize(12);
        textView.setMaxLines(2);
        textView.setEllipsize(android.text.TextUtils.TruncateAt.END);

        TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, weight);
        textView.setLayoutParams(params);

        return textView;
    }

    private void showStudentQuickActions(Student student) {
        String[] actions = {
                "Add Note",
                "View All Notes",
                "Student Details",
                "Move to Seat"
        };

        new android.app.AlertDialog.Builder(getActivity())
                .setTitle(student.getFullName())
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0:
                            openNoteFragment(student);
                            break;
                        case 1:
                            openNotesFragment(student);
                            break;
                        case 2:
                            showStudentDetails(student);
                            break;
                        case 3:
                            showMoveStudentDialog(student);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void openNoteFragment(Student student) {
        NoteFragment noteFragment = NoteFragment.newInstance(
                student.getId(),
                student.getFullName(),
                -1
        );
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, noteFragment)
//                .addToBackStack(null)
                .commit();
    }

    private void openNotesFragment(Student student) {
        StudentNotesFragment notesFragment = StudentNotesFragment.newInstance(
                student.getId(),
                student.getFullName()
        );
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, notesFragment)
//                .addToBackStack(null)
                .commit();
    }

    private void showStudentDetails(Student student) {
        new android.app.AlertDialog.Builder(getActivity())
                .setTitle("Student Details")
                .setMessage(
                        "Name: " + student.getFullName() + "\n" +
                                "LRN: " + student.getLRN() + "\n" +
                                "Seat: " + student.getSeatNumber() + "\n" +
                                "Gender: " + student.getSex() + "\n" +
                                "Birthday: " + student.getBirthday() + "\n" +
                                "Contact: " + student.getContactInfo() + "\n" +
                                "Modality: " + student.getLearningModality()
                )
                .setPositiveButton("OK", null)
                .show();
    }

    private void showMoveStudentDialog(Student student) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_move_student, null);
        EditText seatNumberEditText = dialogView.findViewById(R.id.seat_number_edittext);

        new android.app.AlertDialog.Builder(getActivity())
                .setTitle("Move " + student.getFirstName())
                .setView(dialogView)
                .setPositiveButton("Move", (dialog, which) -> {
                    try {
                        int newSeat = Integer.parseInt(seatNumberEditText.getText().toString());
                        if (newSeat > 0 && newSeat <= (getClassroomRows() * getClassroomColumns())) {
                            student.setSeatNumber(newSeat);
                            dbHelper.updateStudent(student);
                            loadStudentsIntoTable();
                            showToast("Moved " + student.getFirstName() + " to seat " + newSeat);
                        } else {
                            showToast("Please enter a valid seat number (1-" + (getClassroomRows() * getClassroomColumns()) + ")");
                        }
                    } catch (NumberFormatException e) {
                        showToast("Please enter a valid seat number");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private int getClassroomRows() {
        int[] layout = dbHelper.getClassroomLayout(classroomId);
        return layout[0];
    }

    private int getClassroomColumns() {
        int[] layout = dbHelper.getClassroomLayout(classroomId);
        return layout[1];
    }

    private void showEmptyState() {
        TableRow emptyRow = new TableRow(getActivity());
        TextView emptyText = new TextView(getActivity());
        emptyText.setText("No students found matching your search.");
        emptyText.setPadding(dpToPx(16), dpToPx(24), dpToPx(16), dpToPx(24));
        emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
        emptyText.setTextSize(14);

        TableRow.LayoutParams params = new TableRow.LayoutParams(
                TableRow.LayoutParams.MATCH_PARENT,
                TableRow.LayoutParams.WRAP_CONTENT
        );
        params.span = 6; // Span across all columns
        emptyText.setLayoutParams(params);

        emptyRow.addView(emptyText);
        tableLayout.addView(emptyRow);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showToast(String message) {
        android.widget.Toast.makeText(getActivity(), message, android.widget.Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadStudentsIntoTable(); // Refresh when returning from other fragments
    }
}