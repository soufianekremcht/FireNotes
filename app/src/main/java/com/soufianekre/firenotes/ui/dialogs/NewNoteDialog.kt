package com.soufianekre.firenotes.ui.dialogs

import android.content.DialogInterface.BUTTON_POSITIVE
import androidx.appcompat.app.AlertDialog
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.extensions.*
import com.soufianekre.firenotes.helper.AppConstants
import com.soufianekre.firenotes.helper.KeyboardUtils
import com.soufianekre.firenotes.ui.base.BaseActivity
import kotlinx.android.synthetic.main.dialog_new_note.view.*


class NewNoteDialog(val activity: BaseActivity, title: String? = null, callback: (note: NoteObject) -> Unit) {
    init {
        val view = activity.layoutInflater.inflate(R.layout.dialog_new_note, null).apply {
            new_note_type.check(if (activity.appConfig.lastCreatedNoteType == AppConstants.NoteType.TYPE_TEXT.value) type_text_note.id else type_checklist.id)
        }

        view.note_title.setText(title)

        AlertDialog.Builder(activity)
            .setTitle(R.string.add_new_note)
            .setPositiveButton(R.string.ok, null)
            .setNegativeButton(R.string.cancel, null)
            .create().apply {
                activity.setupDialogStuff(view, this, R.string.new_note) {
                    KeyboardUtils.showSoftInput(activity,view.note_title)
                    getButton(BUTTON_POSITIVE).setOnClickListener {
                        val newNoteTitle = view.note_title.text.toString()
                        ensureBackgroundThread {
                            when {
                                newNoteTitle.isEmpty() -> activity.showInfo(activity.getString(R.string.no_title))
                                newNoteTitle.isNotEmpty()-> {
                                    val notes = context.notesDB().notesDao().getNotes()
                                    var isExisted = false
                                    notes.forEach{
                                        if (it.title == newNoteTitle){
                                            isExisted = true
                                            return@forEach
                                        }
                                    }
                                    if (isExisted){
                                        activity.showInfo(activity.getString(R.string.title_taken))
                                    } else{
                                        val type: Int
                                        if (view.new_note_type.checkedRadioButtonId == view.type_checklist.id)
                                            type = AppConstants.NoteType.TYPE_CHECKLIST.value
                                        else type = AppConstants.NoteType.TYPE_TEXT.value

                                        activity.appConfig.lastCreatedNoteType = type
                                        val newNote =
                                            NoteObject(title = newNoteTitle, content="",type =  type)
                                        callback(newNote)
                                        dismiss()
                                    }
                                }
                            }
                        }
                    }
                }
            }
    }
}
