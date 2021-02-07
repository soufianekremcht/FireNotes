package com.soufianekre.firenotes.ui.notes

import android.graphics.Color
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.models.ChecklistItem
import com.soufianekre.firenotes.helper.NotesHelper
import com.squareup.moshi.Moshi
import com.squareup.moshi.adapter


public class ChecklistFragment : NoteFragment(), ChecklistItemsListener {

    private var noteId = 0L
    private var note: ContactsContract.CommonDataKinds.Note? = null

    lateinit var view: ViewGroup

    var items = ArrayList<ChecklistItem>()

    val moshi : Moshi = Moshi.Builder().build()

    val checklistItems get(): String = moshi.adapter<ArrayList<ChecklistItem>>().toJson(items)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        view = inflater.inflate(R.layout.fragment_checklist, container, false) as ViewGroup
        noteId = requireArguments().getLong(NOTE_ID, 0L)
        return view
    }

    override fun onResume() {
        super.onResume()

        loadNoteById(noteId)
    }

    override fun setMenuVisibility(menuVisible: Boolean) {
        super.setMenuVisibility(menuVisible)

        if (menuVisible) {
            activity?.hideKeyboard()
        }
    }

    private fun loadNoteById(noteId: Long) {
        NotesHelper(activity!!).getNoteWithId(noteId) { storedNote ->
            if (storedNote != null && activity?.isDestroyed == false) {
                note = storedNote

                try {
                    val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
                    items = Gson().fromJson<ArrayList<ChecklistItem>>(storedNote.value, checklistItemType)
                        ?: ArrayList(1)
                } catch (e: Exception) {
                    migrateCheckListOnFailure(storedNote)
                }

                if (config?.moveUndoneChecklistItems == true) {
                    items.sortBy { it.isDone }
                }

                activity?.updateTextColors(view.checklist_holder)
                setupFragment()
            }
        }
    }

    private fun migrateCheckListOnFailure(note: ContactsContract.CommonDataKinds.Note) {
        items.clear()

        note.value.split("\n").map { it.trim() }.filter { it.isNotBlank() }.forEachIndexed { index, value ->
            items.add(ChecklistItem(
                id = index,
                title = value,
                isDone = false
            ))
        }

        saveChecklist()
    }

    private fun setupFragment() {
        if (activity == null || activity!!.isFinishing) {
            return
        }

        val plusIcon = resources.getColoredDrawableWithColor(R.drawable.ic_plus_vector, if (activity!!.isBlackAndWhiteTheme()) Color.BLACK else Color.WHITE)

        view.checklist_fab.apply {
            setImageDrawable(plusIcon)
            background?.applyColorFilter(activity!!.getAdjustedPrimaryColor())
            setOnClickListener {
                showNewItemDialog()
            }
        }

        view.fragment_placeholder_2.apply {
            setTextColor(activity!!.getAdjustedPrimaryColor())
            underlineText()
            setOnClickListener {
                showNewItemDialog()
            }
        }

        setupAdapter()
    }

    private fun showNewItemDialog() {
        NewChecklistItemDialog(activity as SimpleActivity) { titles ->
            var currentMaxId = items.maxBy { item -> item.id }?.id ?: 0

            titles.forEach { title ->
                title.split("\n").map { it.trim() }.filter { it.isNotBlank() }.forEach { row ->
                    items.add(ChecklistItem(currentMaxId + 1, row, false))
                    currentMaxId++
                }
            }

            saveNote()
            setupAdapter()
        }
    }

    private fun setupAdapter() {
        updateUIVisibility()

        ChecklistAdapter(
            activity = activity as SimpleActivity,
            items = items,
            listener = this,
            recyclerView = view.checklist_list,
            showIcons = true
        ) { item ->
            val clickedNote = item as ChecklistItem
            clickedNote.isDone = !clickedNote.isDone

            saveNote(items.indexOfFirst { it.id == clickedNote.id })
            context?.updateWidgets()
        }.apply {
            view.checklist_list.adapter = this
        }
    }

    private fun saveNote(refreshIndex: Int = -1) {
        ensureBackgroundThread {
            context?.let { ctx ->
                note?.let { currentNote ->
                    if (refreshIndex != -1) {
                        view.checklist_list.post {
                            view.checklist_list.adapter?.notifyItemChanged(refreshIndex)
                        }
                    }

                    currentNote.value = checklistItems
                    ctx.notesDB.insertOrUpdate(currentNote)
                    ctx.updateWidgets()
                }
            }
        }
    }

    fun removeDoneItems() {
        items = items.filter { !it.isDone }.toMutableList() as ArrayList<ChecklistItem>
        saveNote()
        setupAdapter()
    }

    private fun updateUIVisibility() {
        view.apply {
            fragment_placeholder.beVisibleIf(items.isEmpty())
            fragment_placeholder_2.beVisibleIf(items.isEmpty())
            checklist_list.beVisibleIf(items.isNotEmpty())
        }
    }

    override fun saveChecklist() {
        saveNote()
    }

    override fun refreshItems() {
        setupAdapter()
    }

    interface ChecklistItemsListener {

    }
}


