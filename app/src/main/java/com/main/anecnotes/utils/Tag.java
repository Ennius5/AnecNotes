package com.main.anecnotes.utils;

public class Tag {
    private int id;
    private String name;
    private String color;
    private boolean isActive;

    public Tag() {}

    public Tag(String name, String color) {
        this.name = name;
        this.color = color;
        this.isActive = true;
    }

    public Tag(int id, String name, String color, boolean isActive) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.isActive = isActive;
    }

    // Getters and setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }
}