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

public class ClassroomNotesFragment extends Fragment {

    private static final String ARG_CLASSROOM_ID = "classroom_id";
    private static final String ARG_CLASSROOM_NAME = "classroom_name";

    public int classroomId;
    private String classroomName;
    private DatabaseHelper dbHelper;
    private LinearLayout notesContainer;

    public ClassroomNotesFragment() {
        // Required empty public constructor
    }

    public static ClassroomNotesFragment newInstance(int classroomId, String classroomName) {
        ClassroomNotesFragment fragment = new ClassroomNotesFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_CLASSROOM_ID, classroomId);
        args.putString(ARG_CLASSROOM_NAME, classroomName);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            classroomId = getArguments().getInt(ARG_CLASSROOM_ID);
            classroomName = getArguments().getString(ARG_CLASSROOM_NAME);
        }
        dbHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classroom_notes, container, false);

        TextView classroomNameTextView = view.findViewById(R.id.classroom_name_textview);
        notesContainer = view.findViewById(R.id.notes_container);
        Button addNoteBtn = view.findViewById(R.id.add_note_btn);
        Button backBtn = view.findViewById(R.id.back_btn);

        classroomNameTextView.setText("Classroom Notes: " + classroomName);

        loadNotes();

        addNoteBtn.setOnClickListener(v -> {
            NoteFragment noteFragment = NoteFragment.newInstanceForClassroom(
                    classroomId, classroomName, -1);
            noteFragment.classroomId = classroomId;
            ((MainActivity) requireActivity()).navigateToFragment(noteFragment);
        });

        backBtn.setOnClickListener(v -> {
            // Go back to classroom detail
            ClassroomDetailFragment detailFragment = ClassroomDetailFragment.newInstance(classroomId);
            ((MainActivity) requireActivity()).navigateToFragment(detailFragment);
        });

        return view;
    }

    private void loadNotes() {
        notesContainer.removeAllViews();
        List<Note> notes = dbHelper.getNotesByClassroom(classroomId);
        Log.d("DEBUGGGGGGGG",notes.toString());
        if (notes.isEmpty()) {
            TextView noNotesText = new TextView(getActivity());
            noNotesText.setText("No classroom notes available.");
            noNotesText.setPadding(16, 16, 16, 16);
            noNotesText.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            notesContainer.addView(noNotesText);
        } else {
            for (Note note : notes) {
                Log.d("DEBUGGGGGGGG CLASSROOM NOTES",note.getDate());
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
            // Navigate to NoteFragment in edit mode for classroom
            NoteFragment noteFragment = NoteFragment.newInstanceForClassroom(
                    classroomId, classroomName, note.getNoteId());
            noteFragment.classroomId =classroomId;
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

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}