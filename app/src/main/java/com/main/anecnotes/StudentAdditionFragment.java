package com.main.anecnotes;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import androidx.fragment.app.Fragment;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.Student;

public class StudentAdditionFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";
    private int classroomId;
    private DatabaseHelper dbHelper;
    private LinearLayout studentsContainer;

    public StudentAdditionFragment() {
        // Required empty public constructor
    }

    public static StudentAdditionFragment newInstance(int classroomId) {
        StudentAdditionFragment fragment = new StudentAdditionFragment();
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
        View view = inflater.inflate(R.layout.fragment_student_addition, container, false);

        Button excelImportBtn = view.findViewById(R.id.excel_import_btn);
        Button manualAddBtn = view.findViewById(R.id.manual_add_btn);
        Button finishBtn = view.findViewById(R.id.finish_btn);
        Button backBtn = view.findViewById(R.id.back_btn);

        excelImportBtn.setOnClickListener(v -> {
            ExcelReaderFragment excelFragment = new ExcelReaderFragment();
            excelFragment.setTargetClassroom(classroomId);
            ((MainActivity) requireActivity()).navigateToFragment(excelFragment);
        });

        manualAddBtn.setOnClickListener(v -> {
            // Show manual addition dialog
            showManualAddStudentDialog();
        });

        finishBtn.setOnClickListener(v -> {
            ClassroomDetailFragment detailFragment = ClassroomDetailFragment.newInstance(classroomId);
            ((MainActivity) requireActivity()).navigateToFragment(detailFragment);
        });

        backBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToMainMenu();
        });

        return view;
    }


    private void showManualAddStudentDialog() {
        // Your existing manual student addition dialog code
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Students can be added in the classroom");
    }
    private void addStudentField() {//TODO Replace with the excel sheet view switch
        View studentField = getLayoutInflater().inflate(R.layout.student_field_item, studentsContainer, false);

        // Setup gender spinner
        Spinner sexSpinner = studentField.findViewById(R.id.student_sex);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.gender_options,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sexSpinner.setAdapter(adapter);

        studentsContainer.addView(studentField);



    }

    private void saveAllStudents() {
        for (int i = 0; i < studentsContainer.getChildCount(); i++) {
            View studentField = studentsContainer.getChildAt(i);
            EditText lastName = studentField.findViewById(R.id.student_last_name);
            EditText firstName = studentField.findViewById(R.id.student_first_name);
            EditText lrn = studentField.findViewById(R.id.student_lrn);
            Spinner sexSpinner = studentField.findViewById(R.id.student_sex);

            if (!lastName.getText().toString().trim().isEmpty() &&
                    !firstName.getText().toString().trim().isEmpty()) {

                String sex = sexSpinner.getSelectedItem().toString();

                // Use the basic constructor
                Student student = Student.createBasicStudent(
                        lrn.getText().toString(),
                        lastName.getText().toString(),
                        firstName.getText().toString()
                );
                student.setSex(sex); // Set the gender
                student.setSeatNumber(i + 1);
                dbHelper.addStudent(student, classroomId);
            }
        }
    }
}