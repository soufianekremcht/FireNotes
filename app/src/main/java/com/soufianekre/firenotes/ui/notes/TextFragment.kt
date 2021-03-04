package com.soufianekre.firenotes.ui.notes

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.Selection
import android.text.TextWatcher
import android.text.style.UnderlineSpan
import android.text.util.Linkify
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.extensions.*
import com.soufianekre.firenotes.helper.AppConstants.NOTE_ID
import com.soufianekre.firenotes.helper.KeyboardUtils
import com.soufianekre.firenotes.helper.MyMovementMethod
import com.soufianekre.firenotes.helper.NotesHelper
import com.soufianekre.firenotes.ui.main.MainActivity
import com.soufianekre.firenotes.ui.text_history.TextHistory
import com.soufianekre.firenotes.ui.text_history.TextHistoryItem
import kotlinx.android.synthetic.main.fragment_text.view.*
import kotlinx.android.synthetic.main.note_view_horiz_scrollable.view.*
import java.io.File


// text history handling taken from https://gist.github.com/zeleven/0cfa738c1e8b65b23ff7df1fc30c9f7e

class TextFragment : NoteFragment() {
    private val TEXT = "text"

    private var textHistory = TextHistory()
    private var isUndoOrRedo = false
    private var skipTextUpdating = false
    private var noteId = 0L
    private var note: NoteObject? = null

    lateinit var view: ViewGroup

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_text, container, false) as ViewGroup
        noteId = requireArguments().getLong(NOTE_ID, 0L)
        retainInstance = true

        val layoutToInflate = if (appConfig!!.enableLineWrap) R.layout.note_view_static
        else R.layout.note_view_horiz_scrollable

        inflater.inflate(layoutToInflate, view.notes_relative_layout, true)


        if (appConfig!!.clickableLinks) {
            view.text_note_view.apply {
                linksClickable = true
                autoLinkMask = Linkify.WEB_URLS or Linkify.EMAIL_ADDRESSES
                movementMethod = MyMovementMethod.getInstance()
            }
        }

        view.notes_horizontal_scrollview?.onGlobalLayout {
            view.text_note_view.minWidth = view.notes_horizontal_scrollview.width
        }

        return view
    }

    override fun onResume() {
        super.onResume()

        NotesHelper(requireActivity()).getNoteWithId(noteId) {
            if (it != null) {
                note = it
                setupFragment()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (appConfig!!.autosaveNotes) {
            saveText(false)
        }

        removeTextWatcher()
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)
        if (!menuVisible && noteId != 0L && appConfig?.autosaveNotes == true) {
            saveText(false)
        }

        if (menuVisible && noteId != 0L) {
            val currentText = getCurrentNoteViewText()
            if (currentText != null) {
                // TODO : not yet
                (activity as MainActivity)
                    .currentNoteTextChanged(currentText, isUndoAvailable(), isRedoAvailable())


            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (note != null) {
            outState.putString(TEXT, getCurrentNoteViewText())
        }
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        if (savedInstanceState != null && note != null && savedInstanceState.containsKey(TEXT)) {
            skipTextUpdating = true
            val newText = savedInstanceState.getString(TEXT) ?: ""
            view.text_note_view.setText(newText)
        }
    }

    private fun setupFragment() {
        val config = appConfig ?: return
        view.text_note_view.apply {
            typeface = if (config.monospacedFont) Typeface.MONOSPACE else Typeface.DEFAULT

            val fileContents = note!!.getNoteStoredValue()
            if (fileContents == null) {
                //(activity as MainActivity).deleteNote(false, note!!)
                return
            }

            /*
            val adjustedPrimaryColor = context.getAdjustedPrimaryColor()
            setColors(config.textColor, adjustedPrimaryColor, config.backgroundColor)
            setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())

             */
            //highlightColor = adjustedPrimaryColor.adjustAlpha(.4f)

            gravity = config.getTextGravity()
            if (text.toString() != fileContents) {
                if (!skipTextUpdating) {
                    removeTextWatcher()
                    setText(fileContents)
                    setTextWatcher()
                }
                skipTextUpdating = false
                setSelection(if (config.placeCursorToEnd) text.length else 0)
            }

            if (config.showKeyboard) {
                onGlobalLayout {
                    if (activity?.isDestroyed == false) {
                        requestFocus()
                        KeyboardUtils.showSoftInput(requireContext(),view.text_note_view)
                    }
                }
            }

        }

        if (config.showWordCount) {
            view.notes_counter.beVisible()
            //view.notes_counter.setTextColor(config.textColor)
            setWordCounter(view.text_note_view.text.toString())
        } else {
            view.notes_counter.beGone()
        }

        setTextWatcher()
    }

    fun setTextWatcher() {
        view.text_note_view.apply {
            removeTextChangedListener(textWatcher)
            addTextChangedListener(textWatcher)
        }
    }

    fun removeTextWatcher() = view.text_note_view.removeTextChangedListener(textWatcher)

    fun updateNoteValue(value: String) {
        note?.content = value
    }

    fun updateNotePath(path: String) {
        note?.path = path
    }

    fun getNotesView() = view.text_note_view

    fun saveText(force: Boolean) {
        if (note == null) {
            return
        }

        if (note!!.path.isNotEmpty() && !File(note!!.path).exists()) {
            return
        }

        if (context == null || activity == null) {
            return
        }

        val newText = getCurrentNoteViewText()
        val oldText = note!!.getNoteStoredValue()
        if (newText != null && (newText != oldText || force)) {
            note!!.content = newText
            saveNoteValue(note!!)

        }
    }

    fun hasUnsavedChanges() = getCurrentNoteViewText() != note!!.getNoteStoredValue()

    fun focusEditText() {
        view.text_note_view.requestFocus()
    }

    private fun saveNoteValue(note: NoteObject) {
        if (note.path.isEmpty()) {
            NotesHelper(requireActivity()).insertOrUpdateNote(note) {
                (activity as? MainActivity)?.noteSavedSuccessfully(note.title)
            }
        } else {
            val currentText = getCurrentNoteViewText()
            if (currentText != null) {
                val displaySuccess = activity?.appConfig?.displaySuccess ?: false
                // TODO : not yet
                (activity as? MainActivity)?.tryExportNoteValueToFile(note.path, currentText, displaySuccess)
            }
        }
    }

    fun getCurrentNoteViewText() = view.text_note_view?.text?.toString()

    private fun setWordCounter(text: String) {
        val words = text.replace("\n", " ").split(" ")
        view.notes_counter.text = words.count { it.isNotEmpty() }.toString()
    }

    fun undo() {
        val edit = textHistory.getPrevious() ?: return

        val text = view.text_note_view.editableText
        val start = edit.start
        val end = start + if (edit.after != null) edit.after.length else 0

        isUndoOrRedo = true
        try {
            text.replace(start, end, edit.before)
        } catch (e: Exception) {
            // show Error
            activity?.showError(e.localizedMessage)
            return
        }

        isUndoOrRedo = false

        for (span in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(span)
        }

        Selection.setSelection(text, if (edit.before == null) {
            start
        } else {
            start + edit.before.length
        })
    }

    fun redo() {
        val edit = textHistory.getNext() ?: return

        val text = view.text_note_view.editableText
        val start = edit.start
        val end = start + if (edit.before != null) edit.before.length else 0

        isUndoOrRedo = true
        text.replace(start, end, edit.after)
        isUndoOrRedo = false

        for (o in text.getSpans(0, text.length, UnderlineSpan::class.java)) {
            text.removeSpan(o)
        }

        Selection.setSelection(text, if (edit.after == null) {
            start
        } else {
            start + edit.after.length
        })
    }

    fun isUndoAvailable() = textHistory.position > 0

    fun isRedoAvailable() = textHistory.position < textHistory.history.size

    private var textWatcher: TextWatcher = object : TextWatcher {
        private var beforeChange: CharSequence? = null
        private var afterChange: CharSequence? = null

        override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {
            if (!isUndoOrRedo) {
                beforeChange = s.subSequence(start, start + count)
            }
        }

        override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
            if (!isUndoOrRedo) {
                afterChange = s.subSequence(start, start + count)
                textHistory.add(TextHistoryItem(start, beforeChange!!, afterChange!!))
            }
        }

        override fun afterTextChanged(editable: Editable) {
            val text = editable.toString()
            setWordCounter(text)
            (activity as MainActivity).currentNoteTextChanged(text, isUndoAvailable(), isRedoAvailable())
        }
    }
}
