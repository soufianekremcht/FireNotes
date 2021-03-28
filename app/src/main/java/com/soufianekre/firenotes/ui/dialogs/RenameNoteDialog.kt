package com.soufianekre.firenotes.ui.dialogs

import android.provider.ContactsContract
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.helper.NotesHelper
import com.soufianekre.firenotes.ui.base.BaseActivity


class RenameNoteDialog(val activity: BaseActivity, val note: NoteObject, val currentNoteText: String?, val callback: (note: NoteObject) -> Unit){

    init {
        MaterialDialog(activity).show {
            input(){ _, title ->
                if (title.isNotEmpty()){
                    note.title = title.toString()
                    NotesHelper(activity).insertOrUpdateNote(note) {
                        dismiss()
                        callback(note)
                    }
                }
            }
            title(text = "Rename Note :")
            positiveButton(text = "Rename")
            negativeButton(text = "Cancel")
        }
    }
}