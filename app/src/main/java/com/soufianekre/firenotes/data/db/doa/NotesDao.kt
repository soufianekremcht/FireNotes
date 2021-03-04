package com.soufianekre.firenotes.data.db.doa

import androidx.room.*
import com.soufianekre.firenotes.data.db.models.NoteObject

@Dao
interface NotesDao {

    @Query("SELECT * FROM notes ORDER BY title COLLATE NOCASE ASC")
    fun getNotes(): List<NoteObject>

    @Query("SELECT * FROM notes WHERE id = :id")
    fun getNoteWithId(id: Long): NoteObject?

    @Query("SELECT id FROM notes WHERE path = :path")
    fun getNoteIdWithPath(path: String): Long?

    @Query("SELECT id FROM notes WHERE title = :title COLLATE NOCASE")
    fun getNoteIdWithTitle(title: String): Long?

    @Query("SELECT id FROM notes WHERE title = :title")
    fun getNoteIdWithTitleCaseSensitive(title: String): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrUpdate(note: NoteObject): Long

    @Delete
    fun deleteNote(note: NoteObject)

}