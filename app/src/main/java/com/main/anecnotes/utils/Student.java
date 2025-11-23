package com.main.anecnotes.utils;

import java.util.List;

public class Student {
    private int id;
    private String LRN;
    private String lastName;
    private String firstName;
    private String middleName;
    private String sex;
    private String birthday;
    private String age;
    private String motherTongue;
    private String religion;
    private String address;
    private String fatherName;
    private String motherName;
    private String guardianName;
    private String guardianRelationship;
    private String contactInfo;
    private String learningModality;
    private String remarks;
    private AnecdotalNotes anecdotalNotes;
    private ClassRoom classroom;
    private int seatNumber;
    private List<Note> notes;


    // Full constructor
    public Student(String LRN, String lastName, String firstName, String middleName, String sex,
                   String birthday, String age, String motherTongue, String religion, String address,
                   String fatherName, String motherName, String guardianName, String guardianRelationship,
                   String contactInfo, String learningModality, String remarks, AnecdotalNotes anecdotalNotes,
                   ClassRoom classroom, int seatNumber) {
        this.LRN = LRN;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = middleName;
        this.sex = sex;
        this.birthday = birthday;
        this.age = age;
        this.motherTongue = motherTongue;
        this.religion = religion;
        this.address = address;
        this.fatherName = fatherName;
        this.motherName = motherName;
        this.guardianName = guardianName;
        this.guardianRelationship = guardianRelationship;
        this.contactInfo = contactInfo;
        this.learningModality = learningModality;
        this.remarks = remarks;
        this.anecdotalNotes = anecdotalNotes;
        this.classroom = classroom;
        this.seatNumber = seatNumber;
    }

    // Minimal constructor for basic student creation
    public Student(String LRN, String lastName, String firstName) {
        this.LRN = LRN;
        this.lastName = lastName;
        this.firstName = firstName;
        this.middleName = "";
        this.sex = "";
        this.birthday = "";
        this.age = "";
        this.motherTongue = "";
        this.religion = "";
        this.address = "";
        this.fatherName = "";
        this.motherName = "";
        this.guardianName = "";
        this.guardianRelationship = "";
        this.contactInfo = "";
        this.learningModality = "";
        this.remarks = "";
        this.anecdotalNotes = new AnecdotalNotes();
        this.classroom = null;
        this.seatNumber = 0;
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getLRN() { return LRN; }
    public void setLRN(String LRN) { this.LRN = LRN; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }

    public String getSex() { return sex; }
    public void setSex(String sex) { this.sex = sex; }

    public String getBirthday() { return birthday; }
    public void setBirthday(String birthday) { this.birthday = birthday; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getMotherTongue() { return motherTongue; }
    public void setMotherTongue(String motherTongue) { this.motherTongue = motherTongue; }

    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }

    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }

    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }

    public String getGuardianRelationship() { return guardianRelationship; }
    public void setGuardianRelationship(String guardianRelationship) { this.guardianRelationship = guardianRelationship; }

    // Method to get guardian as array (for backward compatibility)
    public String[] getGuardian() {
        return new String[]{guardianName, guardianRelationship};
    }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getLearningModality() { return learningModality; }
    public void setLearningModality(String learningModality) { this.learningModality = learningModality; }

    public String getRemarks() { return remarks; }
    public void setRemarks(String remarks) { this.remarks = remarks; }

    public AnecdotalNotes getAnecdotalNotes() { return anecdotalNotes; }
    public void setAnecdotalNotes(AnecdotalNotes anecdotalNotes) { this.anecdotalNotes = anecdotalNotes; }

    public ClassRoom getClassroom() { return classroom; }
    public void setClassroom(ClassRoom classroom) { this.classroom = classroom; }

    public int getSeatNumber() { return seatNumber; }
    public void setSeatNumber(int seatNumber) { this.seatNumber = seatNumber; }

    // Utility method to get full name
    public String getFullName() {
        return firstName + " " + (middleName != null && !middleName.isEmpty() ? middleName + " " : "") + lastName;
    }

    // Static factory method for creating students with all fields
    public static Student createStudent(String LRN, String lastName, String firstName, String middleName,
                                        String sex, String birthday, String age, String motherTongue,
                                        String religion, String address, String fatherName, String motherName,
                                        String guardianName, String guardianRelationship, String contactInfo,
                                        String learningModality, String remarks, AnecdotalNotes anecdotalNotes,
                                        ClassRoom classroom, int seatNumber) {
        return new Student(LRN, lastName, firstName, middleName, sex, birthday, age, motherTongue,
                religion, address, fatherName, motherName, guardianName, guardianRelationship,
                contactInfo, learningModality, remarks, anecdotalNotes, classroom, seatNumber);
    }

    // Static factory method for creating basic students
    public static Student createBasicStudent(String LRN, String lastName, String firstName) {
        return new Student(LRN, lastName, firstName);
    }
}