package com.soufianekre.firenotes.ui.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.ui.base.BaseActivity

// val note: Note, val callback: (deleteFile: Boolean) -> Unit
class DeleteNoteDialog (val activity:BaseActivity,val note :NoteObject, val callback :(deleteFile: Boolean) ->Unit){




    init {
        var deleteFile = true
        MaterialDialog(activity).show {
            title(text = "Delete Note :")
            message(text = "Do you want to Delete this note ?")
            positiveButton(text = "Delete"){
                callback(deleteFile && note.path.isNotEmpty())
                dismiss()
            }
            negativeButton(text = "Cancel")
        }
    }
}