package com.main.anecnotes;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import com.main.anecnotes.utils.DatabaseHelper;
import com.main.anecnotes.utils.Tag;
import java.util.List;

public class TagManagementFragment extends Fragment {

    private DatabaseHelper dbHelper;
    private ListView tagsListView;
    private List<Tag> tagsList;
    private ArrayAdapter<Tag> adapter;

    public TagManagementFragment() {
        // Required empty public constructor
    }

    public static TagManagementFragment newInstance() {
        return new TagManagementFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_tag_management, container, false);

        tagsListView = view.findViewById(R.id.tags_list_view);
        Button addTagBtn = view.findViewById(R.id.add_tag_btn);
        Button backBtn = view.findViewById(R.id.back_btn);

        loadTags();

        addTagBtn.setOnClickListener(v -> showAddTagDialog());

        backBtn.setOnClickListener(v -> {
            ((MainActivity) requireActivity()).navigateToMainMenu();
        });

        // Set up long click for delete
        tagsListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            Tag tag = tagsList.get(position);
            showDeleteConfirmationDialog(tag);
            return true;
        });

        // Set up click for edit
        tagsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Tag tag = tagsList.get(position);
            showEditTagDialog(tag);
        });

        return view;
    }

    private void loadTags() {
        tagsList = dbHelper.getAllTags();

        adapter = new ArrayAdapter<Tag>(getActivity(), android.R.layout.simple_list_item_1) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Tag tag = tagsList.get(position);

                TextView textView = (TextView) view;
                textView.setText(tag.getName());
                try {
                    textView.setTextColor(Color.parseColor(tag.getColor()));
                } catch (Exception e) {
                    textView.setTextColor(Color.BLACK);
                }

                return view;
            }
        };

        adapter.addAll(tagsList);
        tagsListView.setAdapter(adapter);
    }

    private void showAddTagDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Add New Tag");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tag_edit, null);
        builder.setView(dialogView);

        EditText tagNameEditText = dialogView.findViewById(R.id.edit_tag_name);
        Spinner colorSpinner = dialogView.findViewById(R.id.color_spinner);

        // Setup color spinner
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.tag_colors,
                android.R.layout.simple_spinner_item
        );
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);

        builder.setPositiveButton("Add", (dialog, which) -> {
            String tagName = tagNameEditText.getText().toString();
            String color = getResources().getStringArray(R.array.tag_color_values)
                    [colorSpinner.getSelectedItemPosition()];

            if (!tagName.isEmpty()) {
                Tag tag = new Tag(tagName, color);
                dbHelper.addTag(tag);
                loadTags();
                showToast("Tag added successfully");
            } else {
                showToast("Tag name is required");
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showEditTagDialog(Tag tag) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Edit Tag");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_tag_edit, null);
        builder.setView(dialogView);

        EditText tagNameEditText = dialogView.findViewById(R.id.edit_tag_name);
        Spinner colorSpinner = dialogView.findViewById(R.id.color_spinner);

        // Setup color spinner
        ArrayAdapter<CharSequence> colorAdapter = ArrayAdapter.createFromResource(
                getActivity(),
                R.array.tag_colors,
                android.R.layout.simple_spinner_item
        );
        colorAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        colorSpinner.setAdapter(colorAdapter);

        // Pre-fill with current data
        tagNameEditText.setText(tag.getName());

        // Set current color in spinner
        String[] colorValues = getResources().getStringArray(R.array.tag_color_values);
        for (int i = 0; i < colorValues.length; i++) {
            if (colorValues[i].equals(tag.getColor())) {
                colorSpinner.setSelection(i);
                break;
            }
        }

        builder.setPositiveButton("Update", (dialog, which) -> {
            String tagName = tagNameEditText.getText().toString();
            String color = getResources().getStringArray(R.array.tag_color_values)
                    [colorSpinner.getSelectedItemPosition()];

            if (!tagName.isEmpty()) {
                tag.setName(tagName);
                tag.setColor(color);
                dbHelper.updateTag(tag);
                loadTags();
                showToast("Tag updated successfully");
            } else {
                showToast("Tag name is required");
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void showDeleteConfirmationDialog(Tag tag) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Delete Tag")
                .setMessage("Are you sure you want to delete \"" + tag.getName() + "\"?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    dbHelper.softDeleteTag(tag.getId());
                    loadTags();
                    showToast("Tag deleted successfully");
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showToast(String message) {
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}