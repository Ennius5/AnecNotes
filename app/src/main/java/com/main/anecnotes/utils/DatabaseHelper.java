package com.main.anecnotes.utils;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "AnecNotes.db";
    private static final int DATABASE_VERSION = 7;
    // Tag table
    private static final String TABLE_TAG = "tag";
    private static final String COLUMN_TAG_ID = "tag_id";
    private static final String COLUMN_TAG_NAME = "tag_name";
    private static final String COLUMN_TAG_COLOR = "tag_color";
    private static final String COLUMN_TAG_IS_ACTIVE = "is_active";

    // Classroom layout columns
    private static final String COLUMN_ROWS = "rows";
    private static final String COLUMN_COLUMNS = "columns";

    // Classroom table
    private static final String TABLE_CLASSROOM = "classroom";
    private static final String COLUMN_CLASSROOM_ID = "classroom_id";
    private static final String COLUMN_CLASS_NAME = "class_name";
    private static final String COLUMN_IS_DELETED = "is_deleted";

    // Student table
    private static final String TABLE_STUDENT = "student";
    private static final String COLUMN_STUDENT_ID = "student_id";
    private static final String COLUMN_LRN = "lrn";
    private static final String COLUMN_LAST_NAME = "last_name";
    private static final String COLUMN_FIRST_NAME = "first_name";
    private static final String COLUMN_MIDDLE_NAME = "middle_name";
    private static final String COLUMN_SEX = "sex";
    private static final String COLUMN_BIRTHDAY = "birthday";
    private static final String COLUMN_AGE = "age";
    private static final String COLUMN_MOTHER_TONGUE = "mother_tongue";
    private static final String COLUMN_RELIGION = "religion";
    private static final String COLUMN_ADDRESS = "address";
    private static final String COLUMN_FATHER_NAME = "father_name";
    private static final String COLUMN_MOTHER_NAME = "mother_name";
    private static final String COLUMN_GUARDIAN_NAME = "guardian_name";
    private static final String COLUMN_GUARDIAN_RELATIONSHIP = "guardian_relationship";
    private static final String COLUMN_CONTACT_INFO = "contact_info";
    private static final String COLUMN_LEARNING_MODALITY = "learning_modality";
    private static final String COLUMN_REMARKS = "remarks";
    private static final String COLUMN_SEAT_NUMBER = "seat_number";
    private static final String COLUMN_CLASSROOM_ID_FK = "classroom_id";

    // Note table
    private static final String TABLE_NOTE = "note";
    private static final String COLUMN_NOTE_ID = "note_id";
    private static final String COLUMN_NOTE_TAG = "tag";
    private static final String COLUMN_NOTE_TEXT = "note_text";
    private static final String COLUMN_NOTE_DATE = "note_date";
    private static final String COLUMN_NOTE_IS_DELETED = "is_deleted";
    private static final String COLUMN_STUDENT_ID_FK = "student_id";
    private static final String COLUMN_CLASSROOM_ID_FK_NOTE = "classroom_id";

    private static final String COLUMN_SETTING_EVENTS = "setting_events";
    private static final String COLUMN_ANTECEDENT_TRIGGER = "antecedent_trigger";
    private static final String COLUMN_BEHAVIOR = "behavior";
    private static final String COLUMN_CONSEQUENCES = "consequences";
    private static final String COLUMN_ACTIONS_TAKEN = "actions_taken";
    private static final String COLUMN_NOTE_CREATION_DATE = "creation_date";
    private static final String TABLE_NOTE_IMAGE = "note_image";
    private static final String COLUMN_IMAGE_ID = "image_id";
    private static final String COLUMN_IMAGE_URI = "image_uri";
    private static final String COLUMN_IMAGE_CAPTION = "image_caption";
    private static final String COLUMN_IMAGE_ORDER = "image_order";
    private static final String TAG = "Database";


    public Note getNoteById(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTE,
                new String[]{COLUMN_NOTE_ID, COLUMN_NOTE_DATE, COLUMN_SETTING_EVENTS,
                        COLUMN_ANTECEDENT_TRIGGER, COLUMN_BEHAVIOR, COLUMN_CONSEQUENCES,
                        COLUMN_ACTIONS_TAKEN, COLUMN_NOTE_CREATION_DATE},
                COLUMN_NOTE_ID + " = ? AND " + COLUMN_NOTE_IS_DELETED + " = 0",
                new String[]{String.valueOf(noteId)}, null, null, null);

        Note note = null;
        if (cursor != null && cursor.moveToFirst()) {
            note = new Note(
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_DATE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_EVENTS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANTECEDENT_TRIGGER)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BEHAVIOR)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONSEQUENCES)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIONS_TAKEN)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CREATION_DATE))
            );
            cursor.close();
        }
        db.close();
        return note;
    }
    public boolean updateNote(Note note) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_DATE, note.getDate());
        values.put(COLUMN_SETTING_EVENTS, note.getSettingEvents());
        values.put(COLUMN_ANTECEDENT_TRIGGER, note.getAntecedentTrigger());
        values.put(COLUMN_BEHAVIOR, note.getBehavior());
        values.put(COLUMN_CONSEQUENCES, note.getConsequences());
        values.put(COLUMN_ACTIONS_TAKEN, note.getActionsTaken());
        values.put(COLUMN_NOTE_CREATION_DATE, note.getNoteDate());

        int rowsAffected = db.update(TABLE_NOTE, values,
                COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(note.getNoteId())});
        db.close();
        return rowsAffected > 0;
    }

    public boolean softDeleteNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_IS_DELETED, 1);

        int rowsAffected = db.update(TABLE_NOTE, values,
                COLUMN_NOTE_ID + " = ?", new String[]{String.valueOf(noteId)});
        db.close();
        return rowsAffected > 0;
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createNoteImageTable = "CREATE TABLE " + TABLE_NOTE_IMAGE + "("
                + COLUMN_IMAGE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_IMAGE_URI + " TEXT,"
                + COLUMN_IMAGE_CAPTION + " TEXT,"
                + COLUMN_IMAGE_ORDER + " INTEGER DEFAULT 0,"
                + COLUMN_NOTE_ID + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_NOTE_ID + ") REFERENCES " + TABLE_NOTE + "(" + COLUMN_NOTE_ID + ")" + ")";


        String createTagTable = "CREATE TABLE " + TABLE_TAG + "("
                + COLUMN_TAG_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_TAG_NAME + " TEXT,"
                + COLUMN_TAG_COLOR + " TEXT,"
                + COLUMN_TAG_IS_ACTIVE + " INTEGER DEFAULT 1" + ")";

        String createClassroomTable = "CREATE TABLE " + TABLE_CLASSROOM + "("
                + COLUMN_CLASSROOM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_CLASS_NAME + " TEXT,"
                + "`" + COLUMN_ROWS + "` INTEGER DEFAULT 5,"  // Add backticks
                + "`" + COLUMN_COLUMNS + "` INTEGER DEFAULT 6,"  // Add backticks
                + COLUMN_IS_DELETED + " INTEGER DEFAULT 0" + ")";


        String createStudentTable = "CREATE TABLE " + TABLE_STUDENT + "("
                + COLUMN_STUDENT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_LRN + " TEXT,"
                + COLUMN_LAST_NAME + " TEXT,"
                + COLUMN_FIRST_NAME + " TEXT,"
                + COLUMN_MIDDLE_NAME + " TEXT,"
                + COLUMN_SEX + " TEXT,"
                + COLUMN_BIRTHDAY + " TEXT,"
                + COLUMN_AGE + " TEXT,"
                + COLUMN_MOTHER_TONGUE + " TEXT,"
                + COLUMN_RELIGION + " TEXT,"
                + COLUMN_ADDRESS + " TEXT,"
                + COLUMN_FATHER_NAME + " TEXT,"
                + COLUMN_MOTHER_NAME + " TEXT,"
                + COLUMN_GUARDIAN_NAME + " TEXT,"
                + COLUMN_GUARDIAN_RELATIONSHIP + " TEXT,"
                + COLUMN_CONTACT_INFO + " TEXT,"
                + COLUMN_LEARNING_MODALITY + " TEXT,"
                + COLUMN_REMARKS + " TEXT,"
                + COLUMN_SEAT_NUMBER + " INTEGER,"
                + COLUMN_CLASSROOM_ID_FK + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_CLASSROOM_ID_FK + ") REFERENCES " + TABLE_CLASSROOM + "(" + COLUMN_CLASSROOM_ID + ")" + ")";

        String createNoteTable = "CREATE TABLE " + TABLE_NOTE + "("
                + COLUMN_NOTE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NOTE_DATE + " TEXT,"  // The date when the incident occurred
                + COLUMN_SETTING_EVENTS + " TEXT,"
                + COLUMN_ANTECEDENT_TRIGGER + " TEXT,"
                + COLUMN_BEHAVIOR + " TEXT,"
                + COLUMN_CONSEQUENCES + " TEXT,"
                + COLUMN_ACTIONS_TAKEN + " TEXT,"
                + COLUMN_NOTE_CREATION_DATE + " TEXT,"  // When the note was created in system
                + COLUMN_NOTE_IS_DELETED + " INTEGER DEFAULT 0,"
                + COLUMN_STUDENT_ID_FK + " INTEGER,"
                + COLUMN_CLASSROOM_ID_FK_NOTE + " INTEGER,"
                + "FOREIGN KEY(" + COLUMN_STUDENT_ID_FK + ") REFERENCES " + TABLE_STUDENT + "(" + COLUMN_STUDENT_ID + "),"
                + "FOREIGN KEY(" + COLUMN_CLASSROOM_ID_FK_NOTE + ") REFERENCES " + TABLE_CLASSROOM + "(" + COLUMN_CLASSROOM_ID + ")" + ")";

        String createNoteTagTable = "CREATE TABLE note_tag ("
                + "note_id INTEGER,"
                + "tag_id INTEGER,"
                + "PRIMARY KEY (note_id, tag_id),"
                + "FOREIGN KEY (note_id) REFERENCES note(note_id),"
                + "FOREIGN KEY (tag_id) REFERENCES tag(tag_id))";

        db.execSQL(createNoteTagTable);
        db.execSQL(createClassroomTable);
        db.execSQL(createStudentTable);
        db.execSQL(createNoteTable);
        db.execSQL(createTagTable);
        db.execSQL(createNoteImageTable);

        // Insert default tags
        insertDefaultTags(db);
    }

    private void insertDefaultTags(SQLiteDatabase db) {
        String[] defaultTags = {"Behavior", "Academic", "Social", "Participation", "Homework"};
        String[] colors = {"#FF5733", "#33FF57", "#3357FF", "#F333FF", "#FF33A1"};

        for (int i = 0; i < defaultTags.length; i++) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_TAG_NAME, defaultTags[i]);
            values.put(COLUMN_TAG_COLOR, colors[i]);
            db.insert(TABLE_TAG, null, values);
        }
    }



    public boolean addTagsToNote(int noteId, List<Integer> tagIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            for (int tagId : tagIds) {
                ContentValues values = new ContentValues();
                values.put("note_id", noteId);
                values.put("tag_id", tagId);
                db.insert("note_tag", null, values);
            }
            return true;
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error adding tags to note", e);
            return false;
        } finally {
            db.close();
        }
    }

    public boolean updateNoteTags(int noteId, List<Integer> tagIds) {
        SQLiteDatabase db = this.getWritableDatabase();
        try {
            // Remove existing tags
            db.delete("note_tag", "note_id = ?", new String[]{String.valueOf(noteId)});

            // Add new tags
            return addTagsToNote(noteId, tagIds);
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error updating note tags", e);
            return false;
        } finally {
            db.close();
        }
    }

    public List<Tag> getTagsForNote(int noteId) {
        List<Tag> tags = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT t.* FROM tag t " +
                "INNER JOIN note_tag nt ON t.tag_id = nt.tag_id " +
                "WHERE nt.note_id = ? AND t.is_active = 1";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(noteId)});

        if (cursor.moveToFirst()) {
            do {
                Tag tag = new Tag(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_COLOR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG_IS_ACTIVE)) == 1
                );
                tags.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tags;
    }





    public List<Tag> getAllTags() {
        List<Tag> tagList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_TAG + " WHERE " + COLUMN_TAG_IS_ACTIVE + " = 1";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Tag tag = new Tag(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TAG_COLOR)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_TAG_IS_ACTIVE)) == 1
                );
                tagList.add(tag);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return tagList;
    }

    public long addTag(Tag tag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAG_NAME, tag.getName());
        values.put(COLUMN_TAG_COLOR, tag.getColor());
        long id = db.insert(TABLE_TAG, null, values);
        db.close();
        return id;
    }

    public boolean updateTag(Tag tag) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAG_NAME, tag.getName());
        values.put(COLUMN_TAG_COLOR, tag.getColor());
        values.put(COLUMN_TAG_IS_ACTIVE, tag.isActive() ? 1 : 0);

        int rowsAffected = db.update(TABLE_TAG, values,
                COLUMN_TAG_ID + " = ?", new String[]{String.valueOf(tag.getId())});
        db.close();
        return rowsAffected > 0;
    }

    public boolean softDeleteTag(int tagId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_TAG_IS_ACTIVE, 0);

        int rowsAffected = db.update(TABLE_TAG, values,
                COLUMN_TAG_ID + " = ?", new String[]{String.valueOf(tagId)});
        db.close();
        return rowsAffected > 0;
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 6) {
            // Backup old note data if needed
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSROOM);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
            db.execSQL("DROP TABLE IF EXISTS note_tag");
            onCreate(db);
        } else {
            // For other version upgrades
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NOTE);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_STUDENT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CLASSROOM);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAG);
            db.execSQL("DROP TABLE IF EXISTS note_tag");
            onCreate(db);
        }
    }
    // Classroom CRUD operations
    public long addClassroom(ClassRoom classroom) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, classroom.getClassName());
        long id = db.insert(TABLE_CLASSROOM, null, values);
        db.close();
        return id;
    }

    public boolean updateClassroomName(int classroomId, String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CLASS_NAME, newName);

        int rowsAffected = db.update(TABLE_CLASSROOM, values,
                COLUMN_CLASSROOM_ID + " = ?", new String[]{String.valueOf(classroomId)});
        db.close();
        return rowsAffected > 0;
    }

    public List<ClassRoom> getAllClassrooms() {
        List<ClassRoom> classroomList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_CLASSROOM + " WHERE " + COLUMN_IS_DELETED + " = 0";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                ClassRoom classroom = new ClassRoom(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CLASS_NAME)));
                classroom.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_CLASSROOM_ID)));
                classroom.setRows(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROWS)));
                classroom.setColumns(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLUMNS)));
                classroomList.add(classroom);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return classroomList;
    }

    public boolean updateClassroomLayout(int classroomId, int rows, int columns) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ROWS, rows);
        values.put(COLUMN_COLUMNS, columns);

        int rowsAffected = db.update(TABLE_CLASSROOM, values,
                COLUMN_CLASSROOM_ID + " = ?", new String[]{String.valueOf(classroomId)});
        db.close();
        return rowsAffected > 0;
    }

    public int[] getClassroomLayout(int classroomId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CLASSROOM,
                new String[]{COLUMN_ROWS, COLUMN_COLUMNS},
                COLUMN_CLASSROOM_ID + " = ?",
                new String[]{String.valueOf(classroomId)}, null, null, null);

        int[] layout = new int[]{5, 6}; // Default values
        if (cursor != null && cursor.moveToFirst()) {
            layout[0] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ROWS));
            layout[1] = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COLUMNS));
            cursor.close();
        }
        db.close();
        return layout;
    }

    public void softDeleteClassroom(int classroomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_DELETED, 1);
        db.update(TABLE_CLASSROOM, values, COLUMN_CLASSROOM_ID + " = ?", new String[]{String.valueOf(classroomId)});
        db.close();
    }

    // Student CRUD operations
    public long addStudent(Student student, int classroomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_LRN, student.getLRN());
        values.put(COLUMN_LAST_NAME, student.getLastName());
        values.put(COLUMN_FIRST_NAME, student.getFirstName());
        values.put(COLUMN_MIDDLE_NAME, student.getMiddleName());
        values.put(COLUMN_SEX, student.getSex());
        values.put(COLUMN_BIRTHDAY, student.getBirthday());
        values.put(COLUMN_AGE, student.getAge());
        values.put(COLUMN_MOTHER_TONGUE, student.getMotherTongue());
        values.put(COLUMN_RELIGION, student.getReligion());
        values.put(COLUMN_ADDRESS, student.getAddress());
        values.put(COLUMN_FATHER_NAME, student.getFatherName());
        values.put(COLUMN_MOTHER_NAME, student.getMotherName());
        values.put(COLUMN_GUARDIAN_NAME, student.getGuardian()[0]);
        values.put(COLUMN_GUARDIAN_RELATIONSHIP, student.getGuardian()[1]);
        values.put(COLUMN_CONTACT_INFO, student.getContactInfo());
        values.put(COLUMN_LEARNING_MODALITY, student.getLearningModality());
        values.put(COLUMN_REMARKS, student.getRemarks());
        values.put(COLUMN_SEAT_NUMBER, student.getSeatNumber());
        values.put(COLUMN_CLASSROOM_ID_FK, classroomId);

        long id = db.insert(TABLE_STUDENT, null, values);
        db.close();
        return id;
    }

    public List<Student> getStudentsByClassroom(int classroomId) {
        List<Student> studentList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_STUDENT + " WHERE " + COLUMN_CLASSROOM_ID_FK + " = " + classroomId;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Student student = new Student(
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LRN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEX)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDAY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOTHER_TONGUE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELIGION)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FATHER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOTHER_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GUARDIAN_NAME)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GUARDIAN_RELATIONSHIP)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_INFO)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEARNING_MODALITY)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARKS)),
                        null, // anecdotal notes
                        null, // classroom
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SEAT_NUMBER))
                );
                student.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)));
                studentList.add(student);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return studentList;
    }

    // Note CRUD operations
    public long addNote(Note note, Integer studentId, Integer classroomId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_DATE, note.getDate());
        values.put(COLUMN_SETTING_EVENTS, note.getSettingEvents());
        values.put(COLUMN_ANTECEDENT_TRIGGER, note.getAntecedentTrigger());
        values.put(COLUMN_BEHAVIOR, note.getBehavior());
        values.put(COLUMN_CONSEQUENCES, note.getConsequences());
        values.put(COLUMN_ACTIONS_TAKEN, note.getActionsTaken());
        values.put(COLUMN_NOTE_CREATION_DATE, note.getNoteDate());

        if (studentId != -1) {
            values.put(COLUMN_STUDENT_ID_FK, studentId);
        }
        if (classroomId != -1) {
            values.put(COLUMN_CLASSROOM_ID_FK_NOTE, classroomId);
        }

        long id = db.insert(TABLE_NOTE, null, values);
        db.close();
        return id;
    }

    public List<Note> getNotesByStudent(int studentId) {
        List<Note> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTE + " WHERE " + COLUMN_STUDENT_ID_FK + " = " + studentId + " AND " + COLUMN_NOTE_IS_DELETED + " = 0 ORDER BY " + COLUMN_NOTE_DATE + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_EVENTS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANTECEDENT_TRIGGER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BEHAVIOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONSEQUENCES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIONS_TAKEN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CREATION_DATE))
                );
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return noteList;
    }

    public List<Note> getNotesByClassroom(int classroomId) {
        List<Note> noteList = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTE + " WHERE " + COLUMN_CLASSROOM_ID_FK + " = " + classroomId + " AND " + COLUMN_NOTE_IS_DELETED + " = 0 ORDER BY " + COLUMN_NOTE_DATE + " DESC";
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                Note note = new Note(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_DATE)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_EVENTS)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANTECEDENT_TRIGGER)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BEHAVIOR)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONSEQUENCES)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIONS_TAKEN)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CREATION_DATE))
                );
                noteList.add(note);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return noteList;
    }


    public boolean updateStudent(Student student) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LRN, student.getLRN());
        values.put(COLUMN_LAST_NAME, student.getLastName());
        values.put(COLUMN_FIRST_NAME, student.getFirstName());
        values.put(COLUMN_MIDDLE_NAME, student.getMiddleName());
        values.put(COLUMN_SEX, student.getSex());
        values.put(COLUMN_BIRTHDAY, student.getBirthday());
        values.put(COLUMN_AGE, student.getAge());
        values.put(COLUMN_MOTHER_TONGUE, student.getMotherTongue());
        values.put(COLUMN_RELIGION, student.getReligion());
        values.put(COLUMN_ADDRESS, student.getAddress());
        values.put(COLUMN_FATHER_NAME, student.getFatherName());
        values.put(COLUMN_MOTHER_NAME, student.getMotherName());
        values.put(COLUMN_GUARDIAN_NAME, student.getGuardianName());
        values.put(COLUMN_GUARDIAN_RELATIONSHIP, student.getGuardianRelationship());
        values.put(COLUMN_CONTACT_INFO, student.getContactInfo());
        values.put(COLUMN_LEARNING_MODALITY, student.getLearningModality());
        values.put(COLUMN_REMARKS, student.getRemarks());
        values.put(COLUMN_SEAT_NUMBER, student.getSeatNumber());

        int rowsAffected = db.update(TABLE_STUDENT, values,
                COLUMN_STUDENT_ID + " = ?", new String[]{String.valueOf(student.getId())});
        db.close();
        return rowsAffected > 0;
    }

    public boolean deleteStudent(int studentId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_STUDENT,
                COLUMN_STUDENT_ID + " = ?", new String[]{String.valueOf(studentId)});
        db.close();
        return rowsAffected > 0;
    }

    public Student getStudentById(int studentId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_STUDENT, null,
                COLUMN_STUDENT_ID + " = ?", new String[]{String.valueOf(studentId)},
                null, null, null);

        Student student = null;
        if (cursor != null && cursor.moveToFirst()) {
            student = new Student(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LRN)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEX)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDAY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOTHER_TONGUE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELIGION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FATHER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOTHER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GUARDIAN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GUARDIAN_RELATIONSHIP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_INFO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEARNING_MODALITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARKS)),
                    null, // anecdotal notes
                    null, // classroom
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SEAT_NUMBER))
            );
            student.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)));
            cursor.close();
        }
        db.close();
        return student;
    }
    public Student getStudentBySeatNumber(int classroomId, int seatNumber) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_STUDENT +
                " WHERE " + COLUMN_CLASSROOM_ID_FK + " = ? AND " +
                COLUMN_SEAT_NUMBER + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(classroomId), String.valueOf(seatNumber)});

        Student student = null;
        if (cursor != null && cursor.moveToFirst()) {
            student = new Student(
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LRN)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LAST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FIRST_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MIDDLE_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SEX)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BIRTHDAY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_AGE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOTHER_TONGUE)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_RELIGION)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ADDRESS)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_FATHER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_MOTHER_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GUARDIAN_NAME)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_GUARDIAN_RELATIONSHIP)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT_INFO)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LEARNING_MODALITY)),
                    cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_REMARKS)),
                    null, // anecdotal notes
                    null, // classroom
                    cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_SEAT_NUMBER))
            );
            student.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_STUDENT_ID)));
            cursor.close();
        }
        db.close();
        return student;
    }


    public void softDeleteClassroomWithDependencies(int classroomId) {
        SQLiteDatabase db = this.getWritableDatabase();

        try {
            db.beginTransaction();

            // Soft delete the classroom
            ContentValues classroomValues = new ContentValues();
            classroomValues.put(COLUMN_IS_DELETED, 1);
            db.update(TABLE_CLASSROOM, classroomValues,
                    COLUMN_CLASSROOM_ID + " = ?", new String[]{String.valueOf(classroomId)});

            // Soft delete all notes associated with this classroom
            ContentValues noteValues = new ContentValues();
            noteValues.put(COLUMN_NOTE_IS_DELETED, 1);
            db.update(TABLE_NOTE, noteValues,
                    COLUMN_CLASSROOM_ID_FK_NOTE + " = ?", new String[]{String.valueOf(classroomId)});

            // Note: We're not deleting students because they might be referenced elsewhere
            // but you could add student soft deletion here if needed

            db.setTransactionSuccessful();
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error soft deleting classroom with dependencies", e);
        } finally {
            db.endTransaction();
            db.close();
        }
    }

    public void setDefaultGenderForExistingStudents() {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SEX, "Male");

        // Update all students where sex is null or empty
        db.update(TABLE_STUDENT, values,
                COLUMN_SEX + " IS NULL OR " + COLUMN_SEX + " = ''", null);
        db.close();
    }



    public List<NoteExportData> getNotesForExport(int classroomId) {
        List<NoteExportData> exportData = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        try {
            // Get classroom notes
            String classroomNotesQuery = "SELECT n.*, NULL as student_name " +
                    "FROM " + TABLE_NOTE + " n " +
                    "WHERE n." + COLUMN_CLASSROOM_ID_FK_NOTE + " = ? " +
                    "AND n." + COLUMN_NOTE_IS_DELETED + " = 0 " +
                    "ORDER BY n." + COLUMN_NOTE_DATE + " DESC";

            Cursor classCursor = db.rawQuery(classroomNotesQuery, new String[]{String.valueOf(classroomId)});
            while (classCursor.moveToNext()) {
                NoteExportData data = new NoteExportData();
                data.note = createNoteFromCursor(classCursor);
                data.studentName = null;
                data.isClassroomNote = true;
                exportData.add(data);
            }
            classCursor.close();

            // Get student notes with student names
            String studentNotesQuery = "SELECT n.*, s." + COLUMN_FIRST_NAME + " || ' ' || s." + COLUMN_LAST_NAME + " as student_name " +
                    "FROM " + TABLE_NOTE + " n " +
                    "INNER JOIN " + TABLE_STUDENT + " s ON n." + COLUMN_STUDENT_ID_FK + " = s." + COLUMN_STUDENT_ID + " " +
                    "WHERE s." + COLUMN_CLASSROOM_ID_FK + " = ? " +
                    "AND n." + COLUMN_NOTE_IS_DELETED + " = 0 " +
                    "ORDER BY n." + COLUMN_NOTE_DATE + " DESC";

            Cursor studentCursor = db.rawQuery(studentNotesQuery, new String[]{String.valueOf(classroomId)});
            while (studentCursor.moveToNext()) {
                NoteExportData data = new NoteExportData();
                data.note = createNoteFromCursor(studentCursor);
                data.studentName = studentCursor.getString(studentCursor.getColumnIndexOrThrow("student_name"));
                data.isClassroomNote = false;
                exportData.add(data);
            }
            studentCursor.close();

        } catch (Exception e) {
            Log.e(TAG, "Error getting notes for export", e);
        } finally {
            db.close();
        }

        return exportData;
    }

    // Helper method to create Note from cursor
    private Note createNoteFromCursor(Cursor cursor) {
        return new Note(
                cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_DATE)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_EVENTS)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ANTECEDENT_TRIGGER)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_BEHAVIOR)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONSEQUENCES)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ACTIONS_TAKEN)),
                cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NOTE_CREATION_DATE))
        );
    }

    public boolean deleteNote(int noteId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_NOTE, COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)}) > 0;
    }

    // Inner class for export data (add this to DatabaseHelper)
    public static class NoteExportData {
        public Note note;
        public String studentName;
        public boolean isClassroomNote;
    }

    public boolean isLrnExistsInClassroom(String lrn, int classroomId, int excludeStudentId) {
        // Handle null or empty LRN
        if (lrn == null || lrn.trim().isEmpty()) {
            return false;
        }

        SQLiteDatabase db = this.getReadableDatabase();
        boolean exists = false;

        try {
            String query;
            String[] selectionArgs;

            if (excludeStudentId == -1) {
                // For new students (check all students in classroom)
                query = "SELECT COUNT(*) FROM " + TABLE_STUDENT +
                        " WHERE " + COLUMN_LRN + " = ? AND " +
                        COLUMN_CLASSROOM_ID_FK + " = ?";
                selectionArgs = new String[]{lrn.trim(), String.valueOf(classroomId)};
            } else {
                // For existing students (exclude current student being edited)
                query = "SELECT COUNT(*) FROM " + TABLE_STUDENT +
                        " WHERE " + COLUMN_LRN + " = ? AND " +
                        COLUMN_CLASSROOM_ID_FK + " = ? AND " +
                        COLUMN_STUDENT_ID + " != ?";
                selectionArgs = new String[]{lrn.trim(), String.valueOf(classroomId), String.valueOf(excludeStudentId)};
            }

            Cursor cursor = db.rawQuery(query, selectionArgs);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    exists = cursor.getInt(0) > 0;
                }
                cursor.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking LRN existence", e);
        } finally {
            db.close();
        }

        return exists;
    }

    public long addNoteImage(int noteId, String imageUri, String caption) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NOTE_ID, noteId);
        values.put(COLUMN_IMAGE_URI, imageUri);
        values.put(COLUMN_IMAGE_CAPTION, caption);

        // Get the next order number
        int nextOrder = getNextImageOrder(noteId);
        values.put(COLUMN_IMAGE_ORDER, nextOrder);

        long id = db.insert(TABLE_NOTE_IMAGE, null, values);
//        db.close();
        return id;
    }

    private int getNextImageOrder(int noteId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_NOTE_IMAGE,
                new String[]{"MAX(" + COLUMN_IMAGE_ORDER + ")"},
                COLUMN_NOTE_ID + " = ?",
                new String[]{String.valueOf(noteId)}, null, null, null);

        int maxOrder = 0;
        if (cursor != null && cursor.moveToFirst()) {
            maxOrder = cursor.getInt(0);
            cursor.close();
        }
//        db.close();
        return maxOrder + 1;
    }

    public List<NoteImage> getNoteImages(int noteId) {
        List<NoteImage> images = new ArrayList<>();
        String selectQuery = "SELECT * FROM " + TABLE_NOTE_IMAGE +
                " WHERE " + COLUMN_NOTE_ID + " = " + noteId +
                " ORDER BY " + COLUMN_IMAGE_ORDER + " ASC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        if (cursor.moveToFirst()) {
            do {
                NoteImage image = new NoteImage(
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_ID)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_NOTE_ID)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)),
                        cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_CAPTION)),
                        cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_ORDER))
                );
                images.add(image);
            } while (cursor.moveToNext());
        }
        cursor.close();
//        db.close();
        return images;
    }

    public boolean deleteNoteImage(int imageId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_NOTE_IMAGE,
                COLUMN_IMAGE_ID + " = ?", new String[]{String.valueOf(imageId)});
//        db.close();
        return rowsAffected > 0;
    }

    public boolean updateImageCaption(int imageId, String caption) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_IMAGE_CAPTION, caption);

        int rowsAffected = db.update(TABLE_NOTE_IMAGE, values,
                COLUMN_IMAGE_ID + " = ?", new String[]{String.valueOf(imageId)});
//        db.close();
        return rowsAffected > 0;
    }
}