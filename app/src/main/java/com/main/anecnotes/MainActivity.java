package com.main.anecnotes;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {

    private boolean doubleBackToExitPressedOnce = false;
    private static final int DOUBLE_BACK_PRESS_INTERVAL = 2000;
    private static final String PASSCODE_KEY = "app_passcode";
    private static final String PREFS_NAME = "MyAppPrefs";
    private SharedPreferences sharedPreferences;
    private boolean isAuthenticated = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        // Check if this is first launch (no passcode set)
        if (!isPasscodeSet()) {
            // No passcode set, show setup
            showPasscodeSetup();
        } else {
            // Passcode is set, show authentication
            showAuthentication();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // If not authenticated and passcode is set, show auth when returning to app
        if (!isAuthenticated && isPasscodeSet()) {
            showAuthentication();
        }
    }

    private boolean isPasscodeSet() {
        return sharedPreferences.contains(PASSCODE_KEY);
    }

    private void showPasscodeSetup() {
        // Set content view first
        setContentView(R.layout.activity_main);

        PasscodeSetupFragment setupFragment = new PasscodeSetupFragment();
        setupFragment.setOnSetupCompleteListener(new PasscodeSetupFragment.OnSetupCompleteListener() {
            @Override
            public void onSetupComplete() {
                // After setup, show main app
                showMainApp();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, setupFragment)
                .commit();
    }

    private void showAuthentication() {
        // Set content view first
        setContentView(R.layout.activity_main);

        // Offer biometric first, then fallback to passcode
        if (isBiometricAvailable()) {
            showBiometricAuth();
        } else {
            showPasscodeAuth();
        }
    }

    private void showBiometricAuth() {
        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        // Fallback to passcode if biometric fails
                        showPasscodeAuth();
                    }

                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        isAuthenticated = true;
                        showMainApp();
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(MainActivity.this, "Authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock AnecNotes")
                .setSubtitle("Use your biometric to continue")
                .setNegativeButtonText("Use Passcode")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build();

        biometricPrompt.authenticate(promptInfo);
    }

    private void showPasscodeAuth() {
        PasscodeAuthFragment authFragment = new PasscodeAuthFragment();
        authFragment.setOnAuthSuccessListener(new PasscodeAuthFragment.OnAuthSuccessListener() {
            @Override
            public void onAuthSuccess() {
                isAuthenticated = true;
                showMainApp();
            }
        });

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, authFragment)
                .commit();
    }

    private void showMainApp() {
        isAuthenticated = true;

        // Make sure content view is set
        setContentView(R.layout.activity_main);

        // Apply edge-to-edge
        EdgeToEdge.enable(this);

        // Initialize the main app UI
        initializeMainAppUI();

        // Set the main menu as the initial fragment
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainMenuFragment(this))
                .commit();
    }

    private void initializeMainAppUI() {
        // Set up window insets listener for edge-to-edge
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private boolean isBiometricAvailable() {
        androidx.biometric.BiometricManager biometricManager = androidx.biometric.BiometricManager.from(this);
        int result = biometricManager.canAuthenticate(
                androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG |
                        androidx.biometric.BiometricManager.Authenticators.DEVICE_CREDENTIAL
        );
        return result == androidx.biometric.BiometricManager.BIOMETRIC_SUCCESS;
    }

    @Override
    public void onBackPressed() {
        if (!isAuthenticated) {
            // Don't allow back press during authentication
            // This prevents users from bypassing security
            return;
        }

        // Get current fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_container);

        // If we're already at the main menu, implement double-tap to exit
        if (currentFragment instanceof MainMenuFragment) {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, DOUBLE_BACK_PRESS_INTERVAL);
        } else {
            // For all other fragments, navigate back to main menu directly
//            navigateToMainMenu();
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(() -> doubleBackToExitPressedOnce = false, DOUBLE_BACK_PRESS_INTERVAL);

        }
    }

    /**
     * Navigate directly to main menu without using back stack
     */
    public void navigateToMainMenu() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new MainMenuFragment(this))
                .commit();
    }
    /**
     * Navigate to a new fragment without adding to back stack
     */
    public void navigateToFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}