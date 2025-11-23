package com.main.anecnotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.fragment.app.Fragment;
import com.main.anecnotes.utils.ClassRoom;
import com.main.anecnotes.utils.DatabaseHelper;

public class ClassroomCreationFragment extends Fragment {

    private DatabaseHelper dbHelper;
    public MainActivity mainActivity;

    public ClassroomCreationFragment() {
        // Required empty public constructor
    }
    public ClassroomCreationFragment(MainActivity mainActivity) {
        // Required empty public constructor
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classroom_creation, container, false);
        dbHelper = new DatabaseHelper(getActivity());

        EditText classNameEditText = view.findViewById(R.id.class_name_edittext);
        Button createClassroomBtn = view.findViewById(R.id.create_classroom_btn);
        Button backBtn = view.findViewById(R.id.back_btn);

        createClassroomBtn.setOnClickListener(v -> {
            String className = classNameEditText.getText().toString().trim();
            if (!className.isEmpty()) {
                ClassRoom newClassroom = new ClassRoom(className);
                long classroomId = dbHelper.addClassroom(newClassroom);

                // Navigate to student addition
                StudentAdditionFragment studentAdditionFragment = StudentAdditionFragment.newInstance((int) classroomId);
                ((MainActivity) requireActivity()).navigateToFragment(studentAdditionFragment);
            }
        });

        backBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToMainMenu();
        });

        return view;
    }
}