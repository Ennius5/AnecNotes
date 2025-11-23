package com.main.anecnotes.utils;

import java.util.ArrayList;
import java.util.List;

public class ClassRoom {
    private String className;
    private AnecdotalNotes anecdotalNotes;
    private List<Student> students;
    private int id;
    private boolean isDeleted;
    private int rows;
    private int columns;


    public ClassRoom(String className) {
        this.className = className;
        this.anecdotalNotes = new AnecdotalNotes();
        this.students = new ArrayList<>();
        this.isDeleted = false;
        this.rows = 5; // Default
        this.columns = 6; // Default
    }

    // Getters and setters
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }

    public int getColumns() { return columns; }
    public void setColumns(int columns) { this.columns = columns; }
    public String getClassName() { return className; }
    public void setClassName(String className) { this.className = className; }

    public AnecdotalNotes getAnecdotalNotes() { return anecdotalNotes; }
    public List<Student> getStudents() { return students; }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }

    public void addStudent(Student student) {
        students.add(student);
    }
}