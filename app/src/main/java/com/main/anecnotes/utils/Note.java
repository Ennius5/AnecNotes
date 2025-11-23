package com.main.anecnotes.utils;

import java.util.ArrayList;
import java.util.List;

public class Note {
    private List<NoteImage> images = new ArrayList<>();
    private int note_id;
    private String date;
    private String settingEvents;
    private String antecedentTrigger;
    private String behavior;
    private String consequences;
    private String actionsTaken;
    private String noteDate; // For creation timestamp
    public boolean isDeleted = false;

    public Note(int note_id, String date, String settingEvents, String antecedentTrigger,
                String behavior, String consequences, String actionsTaken, String noteDate) {
        this.note_id = note_id;
        this.date = date;
        this.settingEvents = settingEvents;
        this.antecedentTrigger = antecedentTrigger;
        this.behavior = behavior;
        this.consequences = consequences;
        this.actionsTaken = actionsTaken;
        this.noteDate = noteDate;
    }

    public static Note createNote(int note_id, String date, String settingEvents, String antecedentTrigger,
                                  String behavior, String consequences, String actionsTaken, String noteDate) {
        return new Note(note_id, date, settingEvents, antecedentTrigger, behavior, consequences, actionsTaken, noteDate);
    }

    // Getters and Setters
    public int getNoteId() { return note_id; }
    public void setNoteId(int note_id) { this.note_id = note_id; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public String getSettingEvents() { return settingEvents; }
    public void setSettingEvents(String settingEvents) { this.settingEvents = settingEvents; }

    public String getAntecedentTrigger() { return antecedentTrigger; }
    public void setAntecedentTrigger(String antecedentTrigger) { this.antecedentTrigger = antecedentTrigger; }

    public String getBehavior() { return behavior; }
    public void setBehavior(String behavior) { this.behavior = behavior; }

    public String getConsequences() { return consequences; }
    public void setConsequences(String consequences) { this.consequences = consequences; }

    public String getActionsTaken() { return actionsTaken; }
    public void setActionsTaken(String actionsTaken) { this.actionsTaken = actionsTaken; }

    public String getNoteDate() { return noteDate; }
    public void setNoteDate(String noteDate) { this.noteDate = noteDate; }

    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public void softDelete() {
        this.isDeleted = true;
    }

    // Helper method to get display text for list views
    public String getDisplaySummary() {
        return "Date: " + date + " | Behavior: " +
                (behavior != null && behavior.length() > 50 ? behavior.substring(0, 47) + "..." : behavior);
    }

    public List<NoteImage> getImages() { return images; }
    public void setImages(List<NoteImage> images) { this.images = images; }
}