package com.main.anecnotes.utils;

public class NoteImage {
    private int imageId;
    private int noteId;
    private String imageUri;
    private String caption;
    private int order;

    public NoteImage() {}

    public NoteImage(int imageId, int noteId, String imageUri, String caption, int order) {
        this.imageId = imageId;
        this.noteId = noteId;
        this.imageUri = imageUri;
        this.caption = caption;
        this.order = order;
    }

    // Getters and setters
    public int getImageId() { return imageId; }
    public void setImageId(int imageId) { this.imageId = imageId; }

    public int getNoteId() { return noteId; }
    public void setNoteId(int noteId) { this.noteId = noteId; }

    public String getImageUri() { return imageUri; }
    public void setImageUri(String imageUri) { this.imageUri = imageUri; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }

    public int getOrder() { return order; }
    public void setOrder(int order) { this.order = order; }
}