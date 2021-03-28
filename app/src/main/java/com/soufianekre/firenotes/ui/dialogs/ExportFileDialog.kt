package com.soufianekre.firenotes.ui.dialogs

import com.afollestad.materialdialogs.MaterialDialog
import com.soufianekre.firenotes.ui.base.BaseActivity

class ExportFileDialog(val activity:BaseActivity) {

    init {
        MaterialDialog(activity).show {
            title(text = "Export File")
            positiveButton(text = "Export")
        }
    }
}