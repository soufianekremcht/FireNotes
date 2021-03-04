package com.soufianekre.firenotes.ui.dialogs

import android.app.Activity
import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.extensions.setupDialogStuff
import com.soufianekre.firenotes.extensions.showInfo
import com.soufianekre.firenotes.helper.KeyboardUtils
import kotlinx.android.synthetic.main.dialog_new_checklist_item.view.*

class NewChecklistItemDialog(val activity: Activity, callback: (titles: ArrayList<String>) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_checklist_item, null)

        AlertDialog.Builder(activity)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.add_new_checklist_items) {
                    KeyboardUtils.hideSoftInput(activity)
                    getButton(BUTTON_POSITIVE).setOnClickListener {
                        val title1 = view.checklist_item_title_1.text.toString()
                        val title2 = view.checklist_item_title_2.text.toString()
                        val title3 = view.checklist_item_title_3.text.toString()
                        when {
                            title1.isEmpty() && title2.isEmpty() && title3.isEmpty() -> activity.showInfo(activity.getString(R.string.empty_name))
                            else -> {
                                val titles = arrayListOf(title1, title2, title3).filter {
                                    it.isNotEmpty()
                                }.toMutableList() as ArrayList<String>
                                callback(titles)
                                dismiss()
                            }
                        }
                    }
                }
            }
    }
}
