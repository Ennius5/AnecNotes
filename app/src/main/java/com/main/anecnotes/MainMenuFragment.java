package com.main.anecnotes;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;

public class MainMenuFragment extends Fragment {

    public MainActivity mainActivity;
    public MainMenuFragment() {
        // Required empty public constructor

    }
    public MainMenuFragment(MainActivity mainActivity) {
        // Required empty public constructor
        this.mainActivity = mainActivity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main_menu, container, false);

        Button newClassroomBtn = view.findViewById(R.id.new_classroom_btn);
        Button loadClassroomBtn = view.findViewById(R.id.load_classroom_btn);


        newClassroomBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToFragment(new ClassroomCreationFragment(mainActivity));
        });

        loadClassroomBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToFragment(new ClassroomListFragment(mainActivity));
        });

        return view;
    }
}