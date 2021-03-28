package com.soufianekre.firenotes.ui.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import com.soufianekre.firenotes.ui.base.BaseActivity

class ImportFolderDialog(val activity :BaseActivity) {

    init {
        MaterialDialog(activity).show {
            title(text = "Import File")
            positiveButton(text = "Import")
            negativeButton(text = "Cancel")
        }
    }
}