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

public class PasscodeSetupFragment extends Fragment {

    private EditText passcodeInput, confirmPasscodeInput;
    private Button setupButton;
    private SharedPreferences sharedPreferences;
    private OnSetupCompleteListener onSetupCompleteListener;

    public interface OnSetupCompleteListener {
        void onSetupComplete();
    }

    public PasscodeSetupFragment() {
        // Required empty public constructor
    }

    public void setOnSetupCompleteListener(OnSetupCompleteListener listener) {
        this.onSetupCompleteListener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_passcode_setup, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE);
        passcodeInput = view.findViewById(R.id.passcodeInput);
        confirmPasscodeInput = view.findViewById(R.id.confirmPasscodeInput);
        setupButton = view.findViewById(R.id.setupButton);

        setupButton.setOnClickListener(v -> setupPasscode());

        return view;
    }

    private void setupPasscode() {
        String passcode = passcodeInput.getText().toString();
        String confirmPasscode = confirmPasscodeInput.getText().toString();

        if (passcode.length() < 4) {
            Toast.makeText(getContext(), "Passcode must be at least 4 digits", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!passcode.equals(confirmPasscode)) {
            Toast.makeText(getContext(), "Passcodes don't match", Toast.LENGTH_SHORT).show();
            return;
        }

        sharedPreferences.edit().putString("app_passcode", passcode).apply();
        Toast.makeText(getContext(), "Passcode set successfully", Toast.LENGTH_SHORT).show();

        if (onSetupCompleteListener != null) {
            onSetupCompleteListener.onSetupComplete();
        }
    }
}