package com.main.anecnotes.utils;

import java.util.ArrayList;
import java.util.List;

public class AnecdotalNotes {
    private List<Note> noteList = new ArrayList<Note>();

    public AnecdotalNotes() {}

//    public boolean createNewNote(String tag, String text, String creationDate) {
//        noteList.add(
//                Note.createNote(
//                        noteList.size(),
//                        tag,
//                        text,
//                        creationDate
//                )
//        );
//        return true;
//    }

//    public void editNote(Note n, String newText) {
//        n.setNote(newText);
//    }

    public boolean permanent_delete_Notes() {
        try {
            List<Note> freshNewList = new ArrayList<Note>();
            for (Note n : noteList) {
                if (!n.isDeleted) {
                    freshNewList.add(n);
                }
            }
            noteList = freshNewList;
            return true;
        } catch (Exception e) {
            System.out.println("Error deleting notes: " + e);
            return false;
        }
    }

    public List<Note> getNoteList() {
        return noteList;
    }
}