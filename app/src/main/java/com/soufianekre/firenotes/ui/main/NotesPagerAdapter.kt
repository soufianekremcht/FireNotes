package com.soufianekre.firenotes.ui.main

import android.app.Activity
import android.os.Bundle
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.extensions.showError
import com.soufianekre.firenotes.helper.AppConstants
import com.soufianekre.firenotes.helper.AppConstants.NOTE_ID
import com.soufianekre.firenotes.ui.notes.ChecklistFragment
import com.soufianekre.firenotes.ui.notes.NoteFragment
import com.soufianekre.firenotes.ui.notes.TextFragment

class NotesPagerAdapter(fm: FragmentManager, val notes: List<NoteObject>, val activity: Activity)
    : FragmentStatePagerAdapter(fm) {
    private var fragments: HashMap<Int, NoteFragment> = LinkedHashMap()

    override fun getCount() = notes.size

    override fun getItem(position: Int): NoteFragment {
        val bundle = Bundle()
        val note = notes[position]
        val id = note.id
        bundle.putLong(NOTE_ID, id)

        if (fragments.containsKey(position)) {
            return fragments[position]!!
        }

        val fragment = TextFragment()
        fragment.arguments = bundle
        fragments[position] = fragment
        return fragment
    }

    override fun getPageTitle(position: Int) = notes[position].title

    fun updateCurrentNoteData(position: Int, path: String, value: String) {
        (fragments[position] as? TextFragment)?.apply {
            updateNotePath(path)
            updateNoteValue(value)
        }
    }

    fun isChecklistFragment(position: Int): Boolean = (fragments[position] is ChecklistFragment)

    fun textFragment(position: Int): TextFragment? = (fragments[position] as? TextFragment)

    fun getCurrentNotesView(position: Int) = (fragments[position] as? TextFragment)?.getNotesView()

    fun getCurrentNoteViewText(position: Int) = (fragments[position] as? TextFragment)?.getCurrentNoteViewText()

    fun appendText(position: Int, text: String) = (fragments[position] as? TextFragment)?.getNotesView()?.append(text)

    fun saveCurrentNote(position: Int, force: Boolean) = (fragments[position] as? TextFragment)?.saveText(force)

    fun focusEditText(position: Int) = (fragments[position] as? TextFragment)?.focusEditText()

    fun anyHasUnsavedChanges() = fragments.values.any { (it as? TextFragment)?.hasUnsavedChanges() == true }

    fun saveAllFragmentTexts() = fragments.values.forEach { (it as? TextFragment)?.saveText(false) }

    fun getNoteChecklistRawItems(position: Int) = (fragments[position] as? ChecklistFragment)?.items

    fun getNoteChecklistItems(position: Int) = (fragments[position] as? ChecklistFragment)?.checklistItems

    fun undo(position: Int) = (fragments[position] as? TextFragment)?.undo()

    fun redo(position: Int) = (fragments[position] as? TextFragment)?.redo()

    override fun finishUpdate(container: ViewGroup) {
        try {
            super.finishUpdate(container)
        } catch (e: Exception) {
            activity.showError(e.localizedMessage)
        }
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        super.destroyItem(container, position, `object`)
        fragments.remove(position)
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val fragment = super.instantiateItem(container, position) as NoteFragment
        fragments[position] = fragment
        return fragment
    }

    fun removeDoneCheckListItems(position: Int) {
        (fragments[position] as? ChecklistFragment)?.removeDoneItems()
    }
}
