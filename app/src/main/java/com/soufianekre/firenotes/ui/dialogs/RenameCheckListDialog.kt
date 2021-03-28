package com.soufianekre.firenotes.ui.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.extensions.setupDialogStuff
import com.soufianekre.firenotes.extensions.showError
import com.soufianekre.firenotes.helper.KeyboardUtils
import kotlinx.android.synthetic.main.dialog_rename_checklist_item.view.*


class RenameCheckListDialog(val activity: Activity, val oldTitle: String, callback: (newTitle: String) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_rename_checklist_item, null).apply {
            checklist_item_title.setText(oldTitle)
        }

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this) {
                    KeyboardUtils.hideSoftInput(activity)
                    getButton(BUTTON_POSITIVE).setOnClickListener {
                        val newTitle = view.checklist_item_title.text.toString()
                        when {
                            newTitle.isEmpty() -> activity.showError(R.string.empty_name)
                            else -> {
                                callback(newTitle)
                                dismiss()
                            }
                        }
                    }
                }
            }
    }
}
