package com.soufianekre.firenotes.helper

import android.content.Context
import android.os.Handler
import android.os.Looper
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.extensions.appConfig
import com.soufianekre.firenotes.extensions.ensureBackgroundThread
import com.soufianekre.firenotes.extensions.notesDB
import java.io.File


class NotesHelper(val context: Context) {
    fun getNotes(callback: (notes: ArrayList<NoteObject>) -> Unit) {
        ensureBackgroundThread {
            // make sure the initial note has enough time to be precreated
            if (context.appConfig.appRunCount <= 1) {
                context.notesDB().notesDao().getNotes()
                Thread.sleep(200)
            }

            val notes = context.notesDB().notesDao().getNotes() as ArrayList<NoteObject>
            val notesToDelete = ArrayList<NoteObject>(notes.size)
            notes.forEach {
                if (it.path.isNotEmpty() && !File(it.path).exists()) {
                    context.notesDB().notesDao().deleteNote(it)
                    notesToDelete.add(it)
                }
            }

            notes.removeAll(notesToDelete)

            if (notes.isEmpty()) {
                val generalNote = context.resources.getString(R.string.general_note)
                val note = NoteObject(
                    title = generalNote,
                    path = "",
                    date = 0,
                    type = AppConstants.NoteType.TYPE_TEXT.value
                )
                context.notesDB().notesDao().insertOrUpdate(note)
                notes.add(note)
            }

            Handler(Looper.getMainLooper()).post {
                callback(notes)
            }
        }
    }

    fun getNoteWithId(id: Long, callback: (note: NoteObject?) -> Unit) {
        ensureBackgroundThread {
            val note = context.notesDB().notesDao().getNoteWithId(id)
            Handler(Looper.getMainLooper()).post {
                callback(note)
            }
        }
    }

    fun getNoteIdWithPath(path: String, callback: (id: Long?) -> Unit) {
        ensureBackgroundThread {
            val id = context.notesDB().notesDao().getNoteIdWithPath(path)
            Handler(Looper.getMainLooper()).post {
                callback(id)
            }
        }
    }

    fun insertOrUpdateNote(note: NoteObject, callback: ((newNoteId: Long) -> Unit)? = null) {
        ensureBackgroundThread {
            val noteId = context.notesDB().notesDao().insertOrUpdate(note)
            Handler(Looper.getMainLooper()).post {
                callback?.invoke(noteId)
            }
        }
    }
}
