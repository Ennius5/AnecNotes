package com.main.anecnotes;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.main.anecnotes.utils.ClassRoom;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.DeleteButtonHandler;

import java.util.List;

public class ClassroomListFragment extends Fragment {

    private static final String TAG = "ClassroomListFragment";

    private DatabaseHelper dbHelper;
    private List<ClassRoom> classrooms;
    private ClassroomAdapter adapter;
    private boolean isDeleteInProgress = false;
    public MainActivity mainActivity;
    public ClassroomListFragment(MainActivity mainActivity) {
        // Required empty public constructor
        this.mainActivity = mainActivity;
    }
    public ClassroomListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_classroom_list, container, false);
        dbHelper = new DatabaseHelper(getActivity());

        ListView classroomListView = view.findViewById(R.id.classroom_list_view);
        Button backBtn = view.findViewById(R.id.back_btn);

        loadClassrooms();

        adapter = new ClassroomAdapter();
        classroomListView.setAdapter(adapter);

        classroomListView.setOnItemClickListener((parent, view1, position, id) -> {
            ClassRoom selectedClassroom = classrooms.get(position);
            ClassroomDetailFragment detailFragment = ClassroomDetailFragment.newInstance(selectedClassroom.getId());
            ((MainActivity) requireActivity()).navigateToFragment(detailFragment);
        });

        backBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToMainMenu();
        });

        return view;
    }

    private void loadClassrooms() {
        classrooms = dbHelper.getAllClassrooms();
        Log.d(TAG, "Loaded " + classrooms.size() + " classrooms");
        for (ClassRoom classroom : classrooms) {
            Log.d(TAG, "Classroom: " + classroom.getClassName() + " ID: " + classroom.getId());
        }
    }

    private class ClassroomAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return classrooms.size();
        }

        @Override
        public Object getItem(int position) {
            return classrooms.get(position);
        }

        @Override
        public long getItemId(int position) {
            return classrooms.get(position).getId();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.classroom_list_item, parent, false);
                holder = new ViewHolder();
                holder.classroomNameText = convertView.findViewById(R.id.classroom_name_text);
                holder.deleteButton = convertView.findViewById(R.id.delete_classroom_btn);
                holder.deleteProgress = convertView.findViewById(R.id.delete_progress);
                holder.itemContainer = convertView;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            ClassRoom classroom = classrooms.get(position);
            holder.classroomNameText.setText(classroom.getClassName());

            // Clear any existing handlers
            Object existingTag = convertView.getTag(R.id.delete_handler_tag);
            if (existingTag instanceof DeleteButtonHandler) {
                ((DeleteButtonHandler) existingTag).cleanup();
            }

            // Setup delete button with long press
            DeleteButtonHandler deleteHandler = new DeleteButtonHandler();

            // Set the delete listener
            deleteHandler.setOnDeleteListener(new DeleteButtonHandler.OnDeleteListener() {
                @Override
                public void onDelete() {
                    // Show confirmation dialog when delete is triggered
                    showDeleteConfirmationDialog(classroom);
                }

                @Override
                public void onDeleteCancelled() {
                    // FIX: Reset the delete in progress flag
                    isDeleteInProgress = false;
                    // Also refresh the view to ensure proper state
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                }

                @Override
                public void onDeleteStarted() {
                    isDeleteInProgress = true;
                }
            });

            deleteHandler.setupDeleteButton(holder.deleteButton, holder.deleteProgress);

            // Store the handler in the view's tag to prevent memory leaks
            convertView.setTag(R.id.delete_handler_tag, deleteHandler);

            // Set click listener for the entire item
            View finalConvertView = convertView;
            View.OnClickListener classroomClickListener = v -> {
                // FIX: Use the handler's current state instead of the global flag
                DeleteButtonHandler currentHandler = (DeleteButtonHandler) finalConvertView.getTag(R.id.delete_handler_tag);
                if (currentHandler == null || !currentHandler.isDeleting()) {
                    handleClassroomClick(classroom);
                }
            };

            holder.classroomNameText.setOnClickListener(classroomClickListener);
            holder.itemContainer.setOnClickListener(classroomClickListener);

            // Prevent the delete button from triggering item clicks
            holder.deleteButton.setOnClickListener(null);
            holder.deleteButton.setClickable(true);
            holder.deleteButton.setFocusable(true);

            return convertView;
        }

        class ViewHolder {
            TextView classroomNameText;
            Button deleteButton;
            ProgressBar deleteProgress;
            View itemContainer;
        }
    }

    private void handleClassroomClick(ClassRoom classroom) {
        Log.d(TAG, "Item clicked: " + classroom.getClassName() + " ID: " + classroom.getId());

        // Navigate to ClassroomDetailFragment
        ClassroomDetailFragment detailFragment = ClassroomDetailFragment.newInstance(classroom.getId());
        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, detailFragment)
//                .addToBackStack(null)
                .commit();
    }

    private void showDeleteConfirmationDialog(ClassRoom classroom) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Classroom")
                .setMessage("Are you sure you want to delete \"" + classroom.getClassName() + "\"?\n\nThis will also delete all students and notes associated with this classroom.")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteClassroom(classroom);
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    // Refresh the list to reset any delete progress
                    loadClassrooms();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                })
                .setOnCancelListener(dialog -> {
                    // Refresh the list if dialog is cancelled
                    loadClassrooms();
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                    }
                })
                .show();
    }

    private void deleteClassroom(ClassRoom classroom) {
        // Soft delete the classroom
        int classroomId = classroom.getId();
        dbHelper.softDeleteClassroomWithDependencies(classroomId);
        loadClassrooms();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }

        showToast("Classroom deleted successfully");
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Clean up any remaining delete handlers
        if (adapter != null) {
            // This is a simple approach - in a more complex app you might want to track all handlers
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the list when returning to this fragment
        loadClassrooms();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }


}