package com.soufianekre.firenotes.ui.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.ui.base.BaseActivity


class DeleteNoteDialog (val activity:BaseActivity){

    init {
        MaterialDialog(activity).show {
            title(text = "Delete Note :")
            message(text = "Do you want to Delete this note ?")
            positiveButton(text = "Delete")
            negativeButton(text = "Cancel")
        }
    }
}