package com.soufianekre.firenotes.ui.dialogs

import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.extensions.appConfig
import com.soufianekre.firenotes.extensions.beVisibleIf
import com.soufianekre.firenotes.helper.NotesHelper
import com.soufianekre.firenotes.ui.base.BaseActivity
import kotlinx.android.synthetic.main.dialog_open_note.view.*
import kotlinx.android.synthetic.main.item_open_note.view.*

class OpenNoteDialog(
    val activity: BaseActivity,
    val callback: (checkedId: Long, newNote: NoteObject?) -> Unit
) {
    private var dialog: MaterialDialog ?= null

    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_open_note, null)
        NotesHelper(activity).getNotes {
            initDialog(it, view)
        }

        view.dialog_open_note_new_radio.setOnClickListener {
            view.dialog_open_note_new_radio.isChecked = false
            NewNoteDialog(activity) {
                callback(0, it)
                dialog?.dismiss()
            }
        }
    }

    private fun initDialog(notes: ArrayList<NoteObject>, cView: View) {
        // add notes to the cView
        notes.forEach {
            activity.layoutInflater.inflate(R.layout.item_open_note, null).apply {
                val note = it

                open_note_item_radio_button.apply {
                    text = note.title
                    isChecked = note.id == activity.appConfig.currentNoteId
                    id = note.id!!.toInt()

                    setOnClickListener {
                        callback(note.id, null)
                        dialog?.dismiss()
                    }
                }
                open_note_item_icon.apply {
                    beVisibleIf(note.path.isNotEmpty())

                    setOnClickListener {
                    }
                }
                cView.dialog_open_note_linear.addView(
                    this,
                    RadioGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                )
            }
        }
        // replace the default view in material dialog to cView
        dialog = MaterialDialog(activity).show {
            title(text = "Pick a Note : ")
            customView(view = cView,scrollable = true)
        }
    }
}
