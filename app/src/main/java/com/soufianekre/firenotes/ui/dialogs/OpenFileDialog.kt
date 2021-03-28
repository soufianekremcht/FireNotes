package com.soufianekre.firenotes.ui.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import com.soufianekre.firenotes.ui.base.BaseActivity

class OpenFileDialog(val activity : BaseActivity) {

    init {
        MaterialDialog(activity).show {
            title(text = "Open File File")
            positiveButton(text = "Open")
            positiveButton(text = "Cancel")
        }
    }
}