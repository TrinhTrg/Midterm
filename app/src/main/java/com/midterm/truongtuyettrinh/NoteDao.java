package com.midterm.truongtuyettrinh;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import androidx.room.Entity;
import androidx.room.*;
import java.util.List;

    @Dao
    public interface NoteDao {
        @Insert
        void insert(Note note);

        @Update
        void update(Note note);

        @Delete
        void delete(Note note);

        @Query("SELECT * FROM notes ORDER BY id DESC")
        List<Note> getAllNotes();
    }


