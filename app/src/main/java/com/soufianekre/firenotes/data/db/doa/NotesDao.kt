package com.soufianekre.firenotes.data.db.doa

import androidx.room.*
import com.soufianekre.firenotes.data.models.NoteObject
import io.reactivex.Completable
import io.reactivex.Flowable

@Dao
interface NotesDao {


    @Insert
    fun insertNote(note: NoteObject): Completable

    @Delete
    fun deleteNote(note: NoteObject): Completable


    @Update
    fun updateNote(note: NoteObject): Completable


    @Query("SELECT * from notes")
    fun getAllNotes(): Flowable<List<NoteObject>>


    @Query("SELECT * from notes WHERE id =:id ")
    fun getNoteWithId(id: Long): NoteObject?

    fun insertOrUpdate(note: NoteObject): Long

    fun getNoteIdWithPath(path: String): @ParameterName(name = "id") Long?

}