package com.main.anecnotes;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

public class PasscodeAuthFragment extends Fragment {

    private EditText passcodeInput;
    private Button submitButton;
    private SharedPreferences sharedPreferences;
    private OnAuthSuccessListener onAuthSuccessListener;

    public interface OnAuthSuccessListener {
        void onAuthSuccess();
    }

    public PasscodeAuthFragment() {
        // Required empty public constructor
    }

    public void setOnAuthSuccessListener(OnAuthSuccessListener listener) {
        this.onAuthSuccessListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_passcode_auth, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        passcodeInput = view.findViewById(R.id.passcodeInput);
        submitButton = view.findViewById(R.id.submitButton);

        submitButton.setOnClickListener(v -> verifyPasscode());

        return view;
    }

    private void verifyPasscode() {
        String input = passcodeInput.getText().toString();
        String savedPasscode = sharedPreferences.getString("app_passcode", "");

        if (input.equals(savedPasscode)) {
            if (onAuthSuccessListener != null) {
                onAuthSuccessListener.onAuthSuccess();
            }
        } else {
            Toast.makeText(getContext(), "Incorrect passcode", Toast.LENGTH_SHORT).show();
            passcodeInput.setText("");
        }
    }
}