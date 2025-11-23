package com.main.anecnotes;

import android.Manifest;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.ImageAdapter;
import com.main.anecnotes.utils.Note;
import com.main.anecnotes.utils.NoteImage;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class NoteFragment extends Fragment {


    private static final String ARG_STUDENT_ID = "student_id";
    private static final String ARG_STUDENT_NAME = "student_name";
    private static final String ARG_NOTE_ID = "note_id";
    private static final String ARG_CLASSROOM_ID = "classroom_id";
    private List<NoteImage> tempNoteImages = new ArrayList<>();

    private static final int CAMERA_PERMISSION_REQUEST = 1003;
    private static final int STORAGE_PERMISSION_REQUEST = 1004;

    private int studentId;
    private String studentName;
    private int noteId; // -1 for new note, existing ID for edit
    public Integer classroomId;
    private DatabaseHelper dbHelper;
    private boolean isEditMode = false;

    private EditText dateEditText;
    private EditText settingEventsEditText;
    private EditText antecedentTriggerEditText;
    private EditText behaviorEditText;
    private EditText consequencesEditText;
    private EditText actionsTakenEditText;
    private ScrollView scrollView;
    private Map<EditText, Integer> editTextPositions = new HashMap<>();
    private RecyclerView imagesRecyclerView;
    private ImageAdapter imageAdapter;
    private List<NoteImage> noteImages = new ArrayList<>();
    private Button addImageBtn;
    private static final int PICK_IMAGE_REQUEST = 1001;
    private static final int CAPTURE_IMAGE_REQUEST = 1002;
    private Uri capturedImageUri;

    public NoteFragment() {
        // Required empty public constructor
    }

    public static NoteFragment newInstance( int studentId, String studentName, int noteId) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STUDENT_ID, studentId);
        args.putString(ARG_STUDENT_NAME, studentName);
        args.putInt(ARG_NOTE_ID, noteId);
        fragment.setArguments(args);
        return fragment;
    }

    public static NoteFragment newInstanceForClassroom(int classroomId, String classroomName, int noteId) {
        NoteFragment fragment = new NoteFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CLASSROOM_ID, classroomId);
        args.putInt(ARG_STUDENT_ID, -1);  // Explicitly set studentId to -99
        args.putString(ARG_STUDENT_NAME, classroomName);
        args.putInt(ARG_NOTE_ID, noteId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            classroomId = getArguments().getInt(ARG_CLASSROOM_ID, -1);
            studentId = getArguments().getInt(ARG_STUDENT_ID, -1);
            studentName = getArguments().getString(ARG_STUDENT_NAME);
            noteId = getArguments().getInt(ARG_NOTE_ID, -1);
            isEditMode = noteId != -1;
        }
        dbHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_note_detailed, container, false);

        TextView studentNameTextView = view.findViewById(R.id.student_name_textview);
        TextView titleTextView = view.findViewById(R.id.title_textview);
        Button saveNoteBtn = view.findViewById(R.id.save_note_btn);
        Button cancelBtn = view.findViewById(R.id.cancel_btn);

        // Initialize the new fields
        dateEditText = view.findViewById(R.id.date_edittext);
        settingEventsEditText = view.findViewById(R.id.setting_events_edittext);
        antecedentTriggerEditText = view.findViewById(R.id.antecedent_trigger_edittext);
        behaviorEditText = view.findViewById(R.id.behavior_edittext);
        consequencesEditText = view.findViewById(R.id.consequences_edittext);
        actionsTakenEditText = view.findViewById(R.id.actions_taken_edittext);
        scrollView = view.findViewById(R.id.scroll_view); // Make sure your ScrollView has this ID
        setupAutoScroll();

        // Set the title based on context
        if (classroomId != -1) {
            studentNameTextView.setText("Classroom: " + studentName);
        } else {
            studentNameTextView.setText("Student: " + studentName);
        }

        if (isEditMode) {
            titleTextView.setText("Edit Anecdotal Note");
            // Load existing note data
            Note existingNote = dbHelper.getNoteById(noteId);
            if (existingNote != null) {
                dateEditText.setText(existingNote.getDate());
                settingEventsEditText.setText(existingNote.getSettingEvents());
                antecedentTriggerEditText.setText(existingNote.getAntecedentTrigger());
                behaviorEditText.setText(existingNote.getBehavior());
                consequencesEditText.setText(existingNote.getConsequences());
                actionsTakenEditText.setText(existingNote.getActionsTaken());
            }
            saveNoteBtn.setText("Update Note");
        } else {
            titleTextView.setText("Add New Anecdotal Note");
            saveNoteBtn.setText("Save Note");
            // Set current date as default
            String currentDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
            dateEditText.setText(currentDate);
        }

        saveNoteBtn.setOnClickListener(v -> {
            if (validateInputs()) {
                String date = dateEditText.getText().toString().trim();
                String settingEvents = settingEventsEditText.getText().toString().trim();
                String antecedentTrigger = antecedentTriggerEditText.getText().toString().trim();
                String behavior = behaviorEditText.getText().toString().trim();
                String consequences = consequencesEditText.getText().toString().trim();
                String actionsTaken = actionsTakenEditText.getText().toString().trim();
                String creationDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                if (isEditMode) {
                    Note note = new Note(noteId, date, settingEvents, antecedentTrigger, behavior, consequences, actionsTaken, creationDate);
                    boolean success = dbHelper.updateNote(note);
                    if (success) {
                        // FIX: Check studentId FIRST, then classroomId
                        if (studentId != -1) {
                            // This is a STUDENT note - go to StudentNotesFragment
                            StudentNotesFragment notesFragment = StudentNotesFragment.newInstance(
                                    studentId,
                                    studentName
                            );
                            ((MainActivity) requireActivity()).navigateToFragment(notesFragment);
                        } else if (classroomId != -1) {
                            // This is a CLASSROOM note - go to ClassroomNotesFragment
                            ClassroomNotesFragment notesFragment = ClassroomNotesFragment.newInstance(
                                    classroomId,
                                    studentName  // This is actually classroom name in this context
                            );
                            ((MainActivity) requireActivity()).navigateToFragment(notesFragment);
                        } else {
                            // Fallback
                            ((MainActivity) requireActivity()).navigateToMainMenu();
                        }
                    }
                } else {
                    Note note = new Note(0, date, settingEvents, antecedentTrigger, behavior, consequences, actionsTaken, creationDate);
                    long newNoteId;

                    if (studentId != -1 && classroomId == -1) {
                        newNoteId = dbHelper.addNote(note, studentId, -1);
                    } else if (classroomId != -1) {
                        newNoteId = dbHelper.addNote(note, -1, classroomId);
                    } else {
                        showToast("Error: Cannot determine note context");
                        return;
                    }

                    if (newNoteId != -1) {
                        // Save temporary images for new note
                        saveTemporaryImages((int) newNoteId);
                        showToast("Anecdotal note saved successfully");
                    } else {
                        showToast("Failed to save note");
                    }
                }

                if (classroomId != -1) {
                    // If this was a classroom note, go back to classroom notes
                    ClassroomNotesFragment notesFragment = ClassroomNotesFragment.newInstance(classroomId, studentName);
                    ((MainActivity) requireActivity()).navigateToFragment(notesFragment);
                } else if (studentId != -1) {
                    // If this was a student note, go back to student notes
                    StudentNotesFragment notesFragment = StudentNotesFragment.newInstance( studentId, studentName);
                    ((MainActivity) requireActivity()).navigateToFragment(notesFragment);
                } else {
                    // Fallback to main menu
                    ((MainActivity) requireActivity()).navigateToMainMenu();
                }



            }
        });

        cancelBtn.setOnClickListener(v -> {
            // Same navigation logic as above but without saving
            if (studentId != -1) {
                StudentNotesFragment notesFragment = StudentNotesFragment.newInstance(studentId, studentName);
                ((MainActivity) requireActivity()).navigateToFragment(notesFragment);
            } else if (classroomId != -1) {
                ClassroomNotesFragment notesFragment = ClassroomNotesFragment.newInstance(classroomId, studentName);
                ((MainActivity) requireActivity()).navigateToFragment(notesFragment);
            } else {
                ((MainActivity) requireActivity()).navigateToMainMenu();
            }
        });
        setupImageHandling(view);
        return view;
    }

    private void saveTemporaryImages(int newNoteId) {
        if (!tempNoteImages.isEmpty()) {
            new Thread(() -> {
                for (NoteImage tempImage : tempNoteImages) {
                    dbHelper.addNoteImage(newNoteId, tempImage.getImageUri(), tempImage.getCaption());
                }
                tempNoteImages.clear();
            }).start();
        }
    }
    @SuppressLint("ClickableViewAccessibility")
    private void setupAutoScroll() {
        // List of all EditText fields
        EditText[] editTexts = {
                dateEditText,
                settingEventsEditText,
                antecedentTriggerEditText,
                behaviorEditText,
                consequencesEditText,
                actionsTakenEditText
        };

        for (EditText editText : editTexts) {
            if (editText != null) {
                editText.setOnFocusChangeListener((v, hasFocus) -> {
                    if (hasFocus) {
                        // Scroll to the focused EditText after a short delay to ensure keyboard is shown
                        new Handler().postDelayed(() -> {
                            if (getActivity() != null && scrollView != null) {
                                scrollView.smoothScrollTo(0, v.getTop());
                            }
                        }, 100);
                    }
                });

                // Also handle touch events for better UX
                editText.setOnTouchListener((v, event) -> {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        new Handler().postDelayed(() -> {
                            if (getActivity() != null && scrollView != null) {
                                scrollView.smoothScrollTo(0, v.getTop());
                            }
                        }, 100);
                    }
                    return false;
                });
            }
        }
    }

    private boolean validateInputs() {
        if (dateEditText.getText().toString().trim().isEmpty()) {
            showToast("Date is required");
            return false;
        }
        if (behaviorEditText.getText().toString().trim().isEmpty()) {
            showToast("Behavior description is required");
            return false;
        }
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }



    private void setupImageHandling(View view) {
        imagesRecyclerView = view.findViewById(R.id.images_recycler_view);
        addImageBtn = view.findViewById(R.id.add_image_btn);

        // Setup RecyclerView
        imagesRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),
                LinearLayoutManager.HORIZONTAL, false));

        imageAdapter = new ImageAdapter(getActivity(), noteImages, new ImageAdapter.OnImageClickListener() {
            @Override
            public void onImageClick(NoteImage image) {
                showImageDialog(image);
            }

            @Override
            public void onImageLongClick(NoteImage image) {
                showImageOptionsDialog(image);
            }
        });

        imagesRecyclerView.setAdapter(imageAdapter);

        // Load existing images if in edit mode
        if (isEditMode) {
            loadNoteImages();
        }

        addImageBtn.setOnClickListener(v -> showImageSourceDialog());
    }

    private void loadNoteImages() {
        if (noteId != -1) {
            new Thread(() -> {
                List<NoteImage> images = dbHelper.getNoteImages(noteId);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        noteImages.clear();
                        noteImages.addAll(images);
                        imageAdapter.updateImages(noteImages);
                    });
                }
            }).start();
        }
    }

    private void showImageSourceDialog() {
        String[] options = {"Take Photo", "Choose from Gallery", "Cancel"};

        new AlertDialog.Builder(getActivity())
                .setTitle("Add Image")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Take Photo
                            takePhoto();
                            break;
                        case 1: // Choose from Gallery
                            chooseFromGallery();
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void takePhoto() {
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.CAMERA}, CAPTURE_IMAGE_REQUEST);
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = createImageFile();
            if (photoFile != null) {
                capturedImageUri = FileProvider.getUriForFile(getActivity(),
                        getActivity().getPackageName() + ".provider",  // ✅ Correct authority
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, capturedImageUri);
                startActivityForResult(takePictureIntent, CAPTURE_IMAGE_REQUEST);
            }
        }
    }

    private void chooseFromGallery() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    private File createImageFile() {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File image = File.createTempFile(
                    imageFileName,  /* prefix */
                    ".jpg",         /* suffix */
                    storageDir      /* directory */
            );
            return image;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showImageDialog(NoteImage image) {
        if (image == null || image.getImageUri() == null || image.getImageUri().isEmpty()) {
            showToast("Cannot display image - image data is missing");
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_image_view, null);

        ImageView imageView = dialogView.findViewById(R.id.dialog_image_view);
        TextView captionText = dialogView.findViewById(R.id.dialog_caption_text);

        try {
            Glide.with(this)
                    .load(Uri.parse(image.getImageUri()))
                    .into(imageView);
        } catch (Exception e) {
            e.printStackTrace();
            showToast("Error loading image");
            return;
        }

        if (image.getCaption() != null && !image.getCaption().isEmpty()) {
            captionText.setText(image.getCaption());
            captionText.setVisibility(View.VISIBLE);
        } else {
            captionText.setVisibility(View.GONE);
        }

        builder.setView(dialogView)
                .setPositiveButton("Close", null)
                .show();
    }

    private void showImageOptionsDialog(NoteImage image) {
        String[] options = {"Edit Caption", "Delete Image", "Cancel"};

        new AlertDialog.Builder(getActivity())
                .setTitle("Image Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Edit Caption
                            editImageCaption(image);
                            break;
                        case 1: // Delete Image
                            deleteImage(image);
                            break;
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void editImageCaption(NoteImage image) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Caption");

        final EditText input = new EditText(getActivity());
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(image.getCaption());
        builder.setView(input);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String newCaption = input.getText().toString();
            updateImageCaption(image, newCaption);
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void updateImageCaption(NoteImage image, String newCaption) {
        if (isEditMode && noteId != -1) {
            // Existing note - update in database
            new Thread(() -> {
                boolean success = dbHelper.updateImageCaption(image.getImageId(), newCaption);
                if (success && getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        loadNoteImages();
                        showToast("Caption updated");
                    });
                }
            }).start();
        } else {
            // New note - update in temporary list
            for (NoteImage tempImage : tempNoteImages) {
                if (tempImage.getImageUri() != null && tempImage.getImageUri().equals(image.getImageUri())) {
                    tempImage.setCaption(newCaption);
                    break;
                }
            }
            // Also update in displayed list
            for (NoteImage displayedImage : noteImages) {
                if (displayedImage.getImageUri() != null && displayedImage.getImageUri().equals(image.getImageUri())) {
                    displayedImage.setCaption(newCaption);
                    break;
                }
            }
            imageAdapter.updateImages(noteImages);
            showToast("Caption updated");
        }
    }

    private void deleteImage(NoteImage image) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Image")
                .setMessage("Are you sure you want to delete this image?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    if (isEditMode && noteId != -1) {
                        // Existing note - delete from database
                        new Thread(() -> {
                            boolean success = dbHelper.deleteNoteImage(image.getImageId());
                            if (success && getActivity() != null) {
                                getActivity().runOnUiThread(() -> {
                                    loadNoteImages();
                                    showToast("Image deleted");
                                });
                            }
                        }).start();
                    } else {
                        // New note - remove from temporary lists
                        tempNoteImages.removeIf(tempImage ->
                                tempImage.getImageUri() != null && tempImage.getImageUri().equals(image.getImageUri()));
                        noteImages.removeIf(displayedImage ->
                                displayedImage.getImageUri() != null && displayedImage.getImageUri().equals(image.getImageUri()));
                        imageAdapter.updateImages(noteImages);
                        showToast("Image deleted");
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == getActivity().RESULT_OK) {
            Uri imageUri = null;

            if (requestCode == PICK_IMAGE_REQUEST && data != null) {
                imageUri = data.getData();
                if (imageUri != null) {
                    // Take persistent permission
                    try {
                        getActivity().getContentResolver().takePersistableUriPermission(
                                imageUri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    } catch (SecurityException e) {
                        e.printStackTrace();
                        showToast("Error accessing image");
                        return;
                    }
                }
            } else if (requestCode == CAPTURE_IMAGE_REQUEST) {
                imageUri = capturedImageUri;
            }

            if (imageUri != null) {
                if (isEditMode && noteId != -1) {
                    // Existing note - save directly to database
                    saveNoteImage(imageUri, "");
                } else {
                    // New note - store temporarily
                    NoteImage tempImage = new NoteImage();
                    tempImage.setImageUri(imageUri.toString());
                    tempImage.setCaption("");
                    tempImage.setNoteId(-1); // Temporary ID
                    tempImage.setOrder(tempNoteImages.size()); // Set order
                    tempNoteImages.add(tempImage);

                    // Update UI immediately
                    noteImages.add(tempImage);
                    imageAdapter.updateImages(noteImages);
                    showToast("Image added - will be saved when note is created");
                }
            } else {
                showToast("Failed to get image");
            }
        }
    }

    private void saveNoteImage(Uri imageUri, String caption) {
        new Thread(() -> {
            long imageId = dbHelper.addNoteImage(noteId, imageUri.toString(), caption);
            if (imageId != -1 && getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    loadNoteImages(); // Refresh the images
                    showToast("Image added to note");
                });
            }
        }).start();
    }
}