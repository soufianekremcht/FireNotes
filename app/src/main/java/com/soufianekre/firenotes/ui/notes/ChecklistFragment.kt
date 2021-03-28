package com.soufianekre.firenotes.ui.notes

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.models.ChecklistItem
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.extensions.beVisibleIf
import com.soufianekre.firenotes.extensions.appConfig
import com.soufianekre.firenotes.extensions.ensureBackgroundThread
import com.soufianekre.firenotes.extensions.notesDB
import com.soufianekre.firenotes.helper.AppConstants.NOTE_ID
import com.soufianekre.firenotes.helper.KeyboardUtils
import com.soufianekre.firenotes.helper.NotesHelper
import com.soufianekre.firenotes.ui.base.BaseActivity
import com.soufianekre.firenotes.ui.dialogs.NewCheckListDialog
import kotlinx.android.synthetic.main.fragment_checklist.view.*


public class ChecklistFragment : NoteFragment(), ChecklistItemsListener {

    private var noteId = 0L
    private var noteObject: NoteObject?= null

    lateinit var view: ViewGroup

    var items = ArrayList<ChecklistItem>()

    val checklistItems get(): String = Gson().toJson(items)

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
            KeyboardUtils.hideSoftInput(requireActivity())
        }
    }

    private fun loadNoteById(noteId: Long) {
        NotesHelper(requireActivity()).getNoteWithId(noteId) { storedNote ->
            if (storedNote != null && activity?.isDestroyed == false) {
                noteObject = storedNote

                try {
                    val checklistItemType = object : TypeToken<List<ChecklistItem>>() {}.type
                    items = Gson().fromJson<ArrayList<ChecklistItem>>(storedNote.content, checklistItemType)
                        ?: ArrayList(1)
                } catch (e: Exception) {
                    migrateCheckListOnFailure(storedNote)
                }

                if (appConfig?.moveUndoneChecklistItems == true) {
                    items.sortBy { it.isDone }
                }

                //activity?.updateTextColors(view.checklist_holder)
                setupFragment()
            }
        }
    }

    private fun migrateCheckListOnFailure(noteObject: NoteObject) {

        items.clear()

        noteObject.content?.split("\n")!!
            .map { it.trim() }.filter { it.isNotBlank() }.forEachIndexed { index, value ->
            items.add(ChecklistItem(
                id = index,
                title = value,
                isDone = false
            ))
        }

        saveChecklist()
    }

    private fun setupFragment() {
        if (activity == null || requireActivity().isFinishing) {
            return
        }

        val plusIcon = ContextCompat.getDrawable(requireContext(),R.drawable.ic_plus)

        view.checklist_fab.apply {
            setImageDrawable(plusIcon)
            backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(requireContext(),R.color.colorPrimary))
            setOnClickListener {
                showNewItemDialog()
            }
        }

        view.fragment_placeholder_2.apply {
            //setTextColor(activity!!.getAdjustedPrimaryColor())
            //underlineText()
            setOnClickListener {
                showNewItemDialog()
            }
        }

        setupAdapter()
    }

    private fun showNewItemDialog() {
        // TODO : not yet
        NewCheckListDialog(activity as BaseActivity) { titles ->
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
            activity = requireActivity(),
            items = items,
            listener = this,
            recyclerView = view.checklist_list,
            showIcons = true
        ) { item ->
            val clickedNote = item as ChecklistItem
            clickedNote.isDone = !clickedNote.isDone

            saveNote(items.indexOfFirst { it.id == clickedNote.id })
            //context?.updateWidgets()
        }.apply {
            view.checklist_list.adapter = this
        }
    }

    private fun saveNote(refreshIndex: Int = -1) {
        ensureBackgroundThread {
            context?.let { ctx ->
                noteObject?.let { currentNote ->
                    if (refreshIndex != -1) {
                        view.checklist_list.post {
                            view.checklist_list.adapter?.notifyItemChanged(refreshIndex)
                        }
                    }

                    currentNote.content = checklistItems
                    ctx.notesDB().notesDao().insertOrUpdate(currentNote)
                    //ctx.updateWidgets()
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


}


