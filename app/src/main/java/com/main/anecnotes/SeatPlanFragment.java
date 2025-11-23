package com.main.anecnotes;

import android.os.Bundle;
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

public class SeatPlanFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";

    public int classroomId;
    private DatabaseHelper dbHelper;
    private LinearLayout seatContainer;
    private List<Student> students;
    private Student selectedStudent = null;

    // Configurable rows and columns
    private int rows = 5;
    private int columns = 6;
    private String classroomName = "Classroom";

    // Seat dimensions in dp - fixed size for pannable layout
    private final int SEAT_WIDTH_DP = 120;
    private final int SEAT_HEIGHT_DP = 120;
    private final int SEAT_MARGIN_DP = 4;

    public SeatPlanFragment() {
        // Required empty public constructor
    }

    public static SeatPlanFragment newInstance(int classroomId) {
        SeatPlanFragment fragment = new SeatPlanFragment();
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

        // Get classroom layout from database
        int[] layout = dbHelper.getClassroomLayout(classroomId);
        rows = layout[0];
        columns = layout[1];

        // Get classroom name
        List<com.main.anecnotes.utils.ClassRoom> classrooms = dbHelper.getAllClassrooms();
        for (com.main.anecnotes.utils.ClassRoom classroom : classrooms) {
            if (classroom.getId() == classroomId) {
                classroomName = classroom.getClassName();
                break;
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_seat_plan, container, false);

        seatContainer = view.findViewById(R.id.seat_container);
        Button configBtn = view.findViewById(R.id.config_btn);
        Button resetBtn = view.findViewById(R.id.reset_btn);

        configBtn.setOnClickListener(v -> showConfigurationDialog());
        resetBtn.setOnClickListener(v -> resetSeatSelection());

        loadSeatPlan();
        return view;
    }

    private void showConfigurationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Configure Classroom Layout");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seat_config, null);
        builder.setView(dialogView);

        EditText rowsEditText = dialogView.findViewById(R.id.edit_rows);
        EditText columnsEditText = dialogView.findViewById(R.id.edit_columns);

        rowsEditText.setText(String.valueOf(rows));
        columnsEditText.setText(String.valueOf(columns));

        builder.setPositiveButton("Apply", (dialog, which) -> {
            try {
                int newRows = Integer.parseInt(rowsEditText.getText().toString());
                int newColumns = Integer.parseInt(columnsEditText.getText().toString());

                if (newRows > 0 && newColumns > 0) {
                    rows = newRows;
                    columns = newColumns;

                    // Save layout to database
                    dbHelper.updateClassroomLayout(classroomId, rows, columns);

                    loadSeatPlan();
                    showToast("Layout updated: " + rows + " rows × " + columns + " columns");
                } else {
                    showToast("Rows and columns must be greater than 0");
                }
            } catch (NumberFormatException e) {
                showToast("Please enter valid numbers");
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void loadSeatPlan() {
        seatContainer.removeAllViews();
        students = dbHelper.getStudentsByClassroom(classroomId);

        // Add blackboard at the top
        addBlackboard();

        if (students.isEmpty()) {
            TextView emptyText = new TextView(getActivity());
            emptyText.setText("No students in this classroom");
            emptyText.setPadding(16, 16, 16, 16);
            emptyText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            seatContainer.addView(emptyText);
            return;
        }

        // Create the grid layout with fixed seat sizes
        for (int row = 0; row < rows; row++) {
            LinearLayout rowLayout = createRowLayout();

            for (int col = 0; col < columns; col++) {
                int seatNumber = (row * columns) + col + 1;
                Student studentInSeat = getStudentBySeatNumber(seatNumber);
                View seatView = createSeatView(seatNumber, studentInSeat);
                rowLayout.addView(seatView);
            }

            seatContainer.addView(rowLayout);
        }

        // Add teacher's desk area
        addTeachersDesk();
    }

    private void addBlackboard() {
        View blackboardView = getLayoutInflater().inflate(R.layout.blackboard_layout, null);
        TextView blackboardText = blackboardView.findViewById(R.id.blackboard_text);

        // Set classroom name on the blackboard
        blackboardText.setText(classroomName + "\nSeat Plan\nThis is the blackboard side");

        // Count unassigned students
        int unassignedCount = 0;
        for (Student student : students) {
            if (student.getSeatNumber() == 0) {
                unassignedCount++;
            }
        }

        // INSTRUCTIONS!
        blackboardText.append("\n\n" + getCurrentDate() +
                "\nStudents: " + students.size() +
                "\nUnassigned: " + unassignedCount +
                "\n\nTap empty seat: Choose from " + unassignedCount + " unassigned students" +
                "\nTap student: Show actions" +
                "\nLong press student: Select for swapping" +
                "\nLong press another seat: Swap students");

        // Set fixed width for blackboard to match the seat grid
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                calculateTotalGridWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, 0, 0, dpToPx(16));
        blackboardView.setLayoutParams(params);

        seatContainer.addView(blackboardView);
    }

    private void addTeachersDesk() {
        View teachersDeskView = getLayoutInflater().inflate(R.layout.teachers_desk_layout, null);

        // Set fixed width for teacher's desk to match the seat grid
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                calculateTotalGridWidth(),
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(0, dpToPx(16), 0, 0);
        teachersDeskView.setLayoutParams(params);

        // Add click listener to teacher's desk for classroom-level actions
        teachersDeskView.setOnClickListener(v -> showClassroomActions());

        seatContainer.addView(teachersDeskView);
    }

    private int calculateTotalGridWidth() {
        // Calculate total width needed for the grid
        int seatWidth = dpToPx(SEAT_WIDTH_DP);
        int seatMargin = dpToPx(SEAT_MARGIN_DP);
        return (seatWidth + (2 * seatMargin)) * columns;
    }

    private void showClassroomActions() {
        String[] actions = {
                "Add Classroom Note",
                "View All Notes",
                "Classroom Settings",
                "Print Seat Plan"
        };

        new AlertDialog.Builder(getActivity())
                .setTitle("Classroom Actions")
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Add Classroom Note
                            addClassroomNote();
                            break;
                        case 1: // View All Notes
                            viewAllClassroomNotes();
                            break;
                        case 2: // Classroom Settings
                            showConfigurationDialog();
                            break;
                        case 3: // Print Seat Plan
                            showToast("Seat plan print feature coming soon");
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void addClassroomNote() {
        NoteFragment noteFragment = NoteFragment.newInstanceForClassroom(
                classroomId,
                classroomName,
                -1
        );
        noteFragment.classroomId = classroomId;
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, noteFragment)
//                .addToBackStack(null)
                .commit();
    }

    private void viewAllClassroomNotes() {
        // Navigate to ClassroomNotesFragment
        ClassroomNotesFragment classroomNotesFragment = ClassroomNotesFragment.newInstance(
                classroomId, classroomName);
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, classroomNotesFragment)
//                .addToBackStack(null)
                .commit();
    }

    private String getCurrentDate() {
        return new java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    private LinearLayout createRowLayout() {
        LinearLayout rowLayout = new LinearLayout(getActivity());
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        return rowLayout;
    }

    private Student getStudentBySeatNumber(int seatNumber) {
        for (Student student : students) {
            if (student.getSeatNumber() == seatNumber) {
                return student;
            }
        }
        return null;
    }

    private View createSeatView(int seatNumber, Student student) {
        View seatView = getLayoutInflater().inflate(R.layout.seat_item, null);
        TextView seatNumberText = seatView.findViewById(R.id.seat_number);
        TextView studentNameText = seatView.findViewById(R.id.student_name);

        // Set fixed size for each seat
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                dpToPx(SEAT_WIDTH_DP),
                dpToPx(SEAT_HEIGHT_DP)
        );
        params.setMargins(dpToPx(SEAT_MARGIN_DP), dpToPx(SEAT_MARGIN_DP),
                dpToPx(SEAT_MARGIN_DP), dpToPx(SEAT_MARGIN_DP));
        seatView.setLayoutParams(params);

        seatNumberText.setText("Seat " + seatNumber);

        if (student != null) {
            String displayName = student.getFirstName() + "\n" + student.getLastName();
            if (displayName.length() > 15) {
                displayName = student.getFirstName().substring(0, Math.min(6, student.getFirstName().length())) +
                        "\n" +
                        student.getLastName().substring(0, Math.min(6, student.getLastName().length()));
            }
            studentNameText.setText(displayName);

            // Set background color based on gender
            if ("Female".equalsIgnoreCase(student.getSex())) {
                seatView.setBackgroundResource(R.drawable.seat_female);
            } else {
                // Default to male (blue) for existing data and new male students
                seatView.setBackgroundResource(R.drawable.seat_male);
            }

            // Single click for student actions
            seatView.setOnClickListener(v -> {
                showStudentActions(student);
            });

            // Long press for seat selection (swapping)
            seatView.setOnLongClickListener(v -> {
                if (selectedStudent == null) {
                    // First long press - select this student for swapping
                    selectedStudent = student;
                    seatView.setBackgroundResource(R.drawable.seat_selected);
                    showToast("Selected " + student.getFirstName() + " for swapping. Long press destination seat.");
                    return true; // Consume the event
                } else if (selectedStudent.getId() != student.getId()) {
                    // Second long press on different student - swap seats
                    swapSeats(selectedStudent, student);
                    selectedStudent = null;
                    loadSeatPlan(); // Refresh the view
                    return true; // Consume the event
                } else {
                    // Long press on same student - deselect
                    selectedStudent = null;
                    loadSeatPlan(); // Refresh to remove highlight
                    return true; // Consume the event
                }
            });

        } else {
            studentNameText.setText("Empty");
            seatView.setBackgroundResource(R.drawable.seat_empty);

            // Single click on empty seat - show option to assign student
            seatView.setOnClickListener(v -> {
                // NEW: Show unassigned students list when clicking empty seat
                showUnassignedStudentsDialog(seatNumber);
            });

            // Long press on empty seat to assign selected student
            seatView.setOnLongClickListener(v -> {
                if (selectedStudent != null) {
                    moveStudentToSeat(selectedStudent, seatNumber);
                    selectedStudent = null;
                    loadSeatPlan(); // Refresh the view
                    return true; // Consume the event
                } else {
                    // NEW: Also show unassigned list on long press if no student selected
                    showUnassignedStudentsDialog(seatNumber);
                    return true; // Consume the event
                }
            });
        }

        return seatView;
    }

    // NEW METHOD: Show dialog with list of unassigned students
    private void showUnassignedStudentsDialog(int seatNumber) {
        // Get all unassigned students (seat number = 0)
        List<Student> unassignedStudents = new ArrayList<>();
        for (Student student : students) {
            if (student.getSeatNumber() == 0) {
                unassignedStudents.add(student);
            }
        }

        if (unassignedStudents.isEmpty()) {
            showToast("No unassigned students available");
            return;
        }

        // Create array of student names for the dialog
        String[] studentNames = new String[unassignedStudents.size()];
        for (int i = 0; i < unassignedStudents.size(); i++) {
            Student student = unassignedStudents.get(i);
            String genderSymbol = "Male".equalsIgnoreCase(student.getSex()) ? "♂" :
                    "Female".equalsIgnoreCase(student.getSex()) ? "♀" : "?";
            studentNames[i] = student.getFullName() + " " + genderSymbol + " (LRN: " + student.getLRN() + ")";
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Assign student to Seat " + seatNumber)
                .setItems(studentNames, (dialog, which) -> {
                    Student selectedStudent = unassignedStudents.get(which);
                    moveStudentToSeat(selectedStudent, seatNumber);
                    loadSeatPlan(); // Refresh the view
                    showToast("Assigned " + selectedStudent.getFirstName() + " to seat " + seatNumber);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void moveStudentToSeat(Student student, int newSeatNumber) {
        // Check if the target seat is already occupied
        Student currentOccupant = getStudentBySeatNumber(newSeatNumber);
        if (currentOccupant != null) {
            showToast("Seat " + newSeatNumber + " is already occupied by " + currentOccupant.getFirstName());
            return;
        }

        int oldSeat = student.getSeatNumber();
        student.setSeatNumber(newSeatNumber);
        boolean success = dbHelper.updateStudent(student);

        if (success) {
            showToast("Moved " + student.getFirstName() + " from seat " + oldSeat + " to seat " + newSeatNumber);
        } else {
            showToast("Error moving student");
            student.setSeatNumber(oldSeat); // Revert on error
        }
    }

    private void swapSeats(Student student1, Student student2) {
        int tempSeat = student1.getSeatNumber();
        student1.setSeatNumber(student2.getSeatNumber());
        student2.setSeatNumber(tempSeat);

        boolean success1 = dbHelper.updateStudent(student1);
        boolean success2 = dbHelper.updateStudent(student2);

        if (success1 && success2) {
            showToast("Swapped " + student1.getFirstName() + " and " + student2.getFirstName());
        } else {
            showToast("Error swapping seats");
            // Revert changes in case of error
            student1.setSeatNumber(tempSeat);
            student2.setSeatNumber(student1.getSeatNumber());
        }

        loadSeatPlan(); // Refresh the view
    }

    private void showStudentActions(Student student) {
        String[] actions = {"Add Note", "View Notes", "View Details", "Unassign Seat"};

        new AlertDialog.Builder(getActivity())
                .setTitle(student.getFullName())
                .setItems(actions, (dialog, which) -> {
                    switch (which) {
                        case 0: // Add Note
                            NoteFragment noteFragment = NoteFragment.newInstance(
                                    student.getId(),
                                    student.getFullName(),
                                    -1
                            );
                            noteFragment.classroomId=classroomId;
                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, noteFragment)
//                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case 1: // View Notes
                            StudentNotesFragment notesFragment = StudentNotesFragment.newInstance(
                                    student.getId(),
                                    student.getFullName()
                            );
                            notesFragment.classroomID = classroomId; //Hell yeah I hope this works out!

                            requireActivity().getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.fragment_container, notesFragment)
//                                    .addToBackStack(null)
                                    .commit();
                            break;
                        case 2: // View Details
                            showStudentDetails(student);
                            break;
                        case 3: // Unassign Seat
                            unassignSeat(student);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showStudentDetails(Student student) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Student Details")
                .setMessage(
                        "Name: " + student.getFullName() + "\n" +
                                "LRN: " + student.getLRN() + "\n" +
                                "Seat: " + student.getSeatNumber() + "\n" +
                                "Gender: " + student.getSex() + "\n" +
                                "Contact: " + student.getContactInfo()
                )
                .setPositiveButton("OK", null)
                .show();
    }

    private void unassignSeat(Student student) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Unassign Seat")
                .setMessage("Remove " + student.getFullName() + " from seat " + student.getSeatNumber() + "?")
                .setPositiveButton("Unassign", (dialog, which) -> {
                    student.setSeatNumber(0);
                    dbHelper.updateStudent(student);
                    loadSeatPlan();
                    showToast("Seat unassigned");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void resetSeatSelection() {
        selectedStudent = null;
        loadSeatPlan();
        showToast("Selection cleared");
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSeatPlan(); // Refresh when returning from other fragments
    }
}