package com.main.anecnotes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.DeleteButtonHandler;
import com.main.anecnotes.utils.Note;
import java.util.List;

public class StudentNotesFragment extends Fragment {

    private static final String ARG_STUDENT_ID = "student_id";
    private static final String ARG_STUDENT_NAME = "student_name";

    private int studentId;
    private String studentName;
    private DatabaseHelper dbHelper;
    private LinearLayout notesContainer;

    public int classroomID; //recorder

    public StudentNotesFragment() {
        // Required empty public constructor
    }

    public static StudentNotesFragment newInstance(int studentId, String studentName) {
        StudentNotesFragment fragment = new StudentNotesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_STUDENT_ID, studentId);
        args.putString(ARG_STUDENT_NAME, studentName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            studentId = getArguments().getInt(ARG_STUDENT_ID);
            studentName = getArguments().getString(ARG_STUDENT_NAME);
        }
        dbHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_student_notes, container, false);

        TextView studentNameTextView = view.findViewById(R.id.student_name_textview);
        notesContainer = view.findViewById(R.id.notes_container);
        Button addNoteBtn = view.findViewById(R.id.add_note_btn);
        Button backBtn = view.findViewById(R.id.back_btn);

        studentNameTextView.setText("Notes for: " + studentName);


        addNoteBtn.setOnClickListener(v -> {
            // Navigate to NoteFragment to add new note
            NoteFragment noteFragment = NoteFragment.newInstance(studentId, studentName, -1);
            noteFragment.classroomId = classroomID;
            Log.d("NAVIGATION SHIT","line 72 StudentNotesFragment notes fragment with classroom id of: "+ noteFragment.classroomId);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, noteFragment)
                    .commit();
        });

        backBtn.setOnClickListener(v -> {
            // Go back to classroom detail, this shit won't go back to the classroom though!
            ClassroomDetailFragment detailFragment = ClassroomDetailFragment.newInstance(classroomID);//////AAAAAAAAAAAAAAAAAAAAAA
            detailFragment.classroomId = classroomID;
            Log.d("NAVIGATION SHIT","line 82 StudentNotesFragment Classroom detail notes fragment with classroom id of: "+ detailFragment.classroomId);

            ((MainActivity) requireActivity()).navigateToFragment(detailFragment);
        });

        loadNotes();
        return view;
    }

    private void loadNotes() {
        notesContainer.removeAllViews();
        List<Note> notes = dbHelper.getNotesByStudent(studentId);

        if (notes.isEmpty()) {
            TextView noNotesText = new TextView(getActivity());
            noNotesText.setText("No notes available for this student.");
            noNotesText.setPadding(16, 16, 16, 16);
            notesContainer.addView(noNotesText);
        } else {
            for (Note note : notes) {
                Log.d("DEBUGGGGGGGG STUDENTNOTES",note.getDate());
                View noteItem = createNoteItemView(note);
                notesContainer.addView(noteItem);
            }
        }
    }

    private View createNoteItemView(Note note) {
        View noteView = getLayoutInflater().inflate(R.layout.note_item_detailed, notesContainer, false);

        TextView dateTextView = noteView.findViewById(R.id.note_date);
        TextView behaviorTextView = noteView.findViewById(R.id.note_behavior);
        TextView antecedentTextView = noteView.findViewById(R.id.note_antecedent);
        TextView actionsTextView = noteView.findViewById(R.id.note_actions);
        Button editBtn = noteView.findViewById(R.id.edit_note_btn);
        Button deleteBtn = noteView.findViewById(R.id.delete_note_btn);
        ProgressBar deleteProgress = noteView.findViewById(R.id.delete_progress);

        dateTextView.setText("Date: " + note.getDate());
        behaviorTextView.setText("Behavior: " +
                (note.getBehavior() != null && note.getBehavior().length() > 100 ?
                        note.getBehavior().substring(0, 97) + "..." : note.getBehavior()));

        antecedentTextView.setText("Trigger: " +
                (note.getAntecedentTrigger() != null && note.getAntecedentTrigger().length() > 80 ?
                        note.getAntecedentTrigger().substring(0, 77) + "..." : note.getAntecedentTrigger()));

        actionsTextView.setText("Actions: " +
                (note.getActionsTaken() != null && note.getActionsTaken().length() > 80 ?
                        note.getActionsTaken().substring(0, 77) + "..." : note.getActionsTaken()));

        editBtn.setOnClickListener(v -> {
            // Navigate to NoteFragment in edit mode
            NoteFragment noteFragment = NoteFragment.newInstance(studentId, studentName, note.getNoteId());
            noteFragment.classroomId=classroomID;
            Log.d("NAVIGATION SHIT"," line 137 StudentNotesFragment Classroom detail notes fragment with classroom id of: "+ noteFragment.classroomId);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, noteFragment)
                    .commit();
        });

        // Setup delete button with long press
        DeleteButtonHandler deleteHandler = new DeleteButtonHandler();

        deleteHandler.setOnDeleteListener(new DeleteButtonHandler.OnDeleteListener() {
            @Override
            public void onDelete() {
                // Perform the actual deletion
                boolean deleted = dbHelper.deleteNote(note.getNoteId());
                if (deleted) {
                    showToast("Note deleted successfully");
                    // Refresh the notes list
                    loadNotes();
                } else {
                    showToast("Failed to delete note");
                }
            }

            @Override
            public void onDeleteCancelled() {
                // Optional: Show cancellation message or do nothing
                showToast("Delete cancelled");
            }

            @Override
            public void onDeleteStarted() {
                // Optional: You can do something when delete starts
                Log.d("DeleteButton", "Delete started for note: " + note.getNoteId());
            }
        });

        deleteHandler.setupDeleteButton(deleteBtn, deleteProgress);

        // Store the handler in the view's tag to prevent memory leaks
        noteView.setTag(deleteHandler);

        return noteView;
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    // Add this method to clean up handlers
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up all delete handlers to prevent memory leaks
        for (int i = 0; i < notesContainer.getChildCount(); i++) {
            View noteView = notesContainer.getChildAt(i);
            Object tag = noteView.getTag();
            if (tag instanceof DeleteButtonHandler) {
                ((DeleteButtonHandler) tag).cleanup();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadNotes(); // Refresh when returning from note editing
    }
}