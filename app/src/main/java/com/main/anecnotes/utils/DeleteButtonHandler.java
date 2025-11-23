package com.main.anecnotes.utils;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

public class DeleteButtonHandler {
    private static final int DELETE_DELAY_MS = 2000; // 2 seconds
    private static final int UPDATE_INTERVAL_MS = 50; // Update every 50ms

    private Handler handler;
    private Runnable deleteRunnable;
    private Runnable progressRunnable;
    private boolean isDeleting = false;
    private int progress = 0;
    private OnDeleteListener deleteListener;

    public interface OnDeleteListener {
        void onDelete();
        void onDeleteCancelled();
        void onDeleteStarted();
    }

    public DeleteButtonHandler() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    public void setOnDeleteListener(OnDeleteListener listener) {
        this.deleteListener = listener;
    }

    @SuppressLint("ClickableViewAccessibility")
    public void setupDeleteButton(Button deleteButton, ProgressBar progressBar) {
        deleteButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case android.view.MotionEvent.ACTION_DOWN:
                    if (!isDeleting) {
                        startDeleteProgress(deleteButton, progressBar);
                    }
                    return true;

                case android.view.MotionEvent.ACTION_UP:
                case android.view.MotionEvent.ACTION_CANCEL:
                    if (isDeleting) {
                        cancelDeleteProgress(deleteButton, progressBar);
                    }
                    return true;
            }
            return false;
        });
    }

    public boolean isDeleting() {
        return isDeleting;
    }

    private void startDeleteProgress(Button deleteButton, ProgressBar progressBar) {
        isDeleting = true;
        progress = 0;

        // Show progress bar
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);

        // Update button text
        deleteButton.setText("Deleting...");

        // Notify listener
        if (deleteListener != null) {
            deleteListener.onDeleteStarted();
        }

        // Start progress updates
        progressRunnable = new Runnable() {
            @Override
            public void run() {
                if (!isDeleting) return;

                progress += (UPDATE_INTERVAL_MS * 100) / DELETE_DELAY_MS;
                if (progress > 100) progress = 100;

                progressBar.setProgress(progress);

                if (progress < 100 && isDeleting) {
                    handler.postDelayed(this, UPDATE_INTERVAL_MS);
                }
            }
        };
        handler.post(progressRunnable);

        // Schedule the actual deletion
        deleteRunnable = () -> {
            if (isDeleting && deleteListener != null) {
                deleteListener.onDelete();
            }
            resetState(deleteButton, progressBar);
        };
        handler.postDelayed(deleteRunnable, DELETE_DELAY_MS);
    }

    private void cancelDeleteProgress(Button deleteButton, ProgressBar progressBar) {
        if (!isDeleting) return;

        // Remove callbacks
        if (deleteRunnable != null) {
            handler.removeCallbacks(deleteRunnable);
        }
        if (progressRunnable != null) {
            handler.removeCallbacks(progressRunnable);
        }

        if (progress < 100 && deleteListener != null) {
            deleteListener.onDeleteCancelled();
        }

        resetState(deleteButton, progressBar);
    }

    private void resetState(Button deleteButton, ProgressBar progressBar) {
        isDeleting = false;
        progress = 0;
        progressBar.setVisibility(View.GONE);
        progressBar.setProgress(0);
        deleteButton.setText("Hold to Delete");
    }

    public void cleanup() {
        isDeleting = false;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }
}