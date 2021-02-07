package com.soufianekre.firenotes.ui.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.lifecycle.ViewModelProvider
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.input.input
import com.soufianekre.firenotes.MyViewModelFactory
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.models.NoteObject
import com.soufianekre.firenotes.databinding.ActivityMainBinding
import com.soufianekre.firenotes.extensions.config
import com.soufianekre.firenotes.helper.AppConstants
import com.soufianekre.firenotes.helper.NotesHelper
import com.soufianekre.firenotes.ui.base.BaseActivity
import com.soufianekre.firenotes.ui.notes.NotesPagerAdapter
import com.soufianekre.firenotes.ui.settings.SettingsActivity
import kotlinx.android.synthetic.main.activity_main.*

public class MainActivity : BaseActivity<ActivityMainBinding, MainViewModel>() {


    lateinit var mCurrentNote: NoteObject;
    var mNotes: ArrayList<NoteObject> = ArrayList()

    private var mAdapter: NotesPagerAdapter? = null
    private var noteViewWithTextSelected: EditText? = null
    private var saveNoteButton: MenuItem? = null

    private var wasInit = false
    private var storedEnableLineWrap = true
    private var showSaveButton = false
    private var showUndoButton = false
    private var showRedoButton = false

    private var factory: MyViewModelFactory? = null
    private lateinit var mMainViewModel: MainViewModel
    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        viewBinding = getViewDataBinding()
        viewBinding.apply {
            vm = getViewModel()
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        menu.apply {
            findItem(R.id.undo).isVisible = showUndoButton
                    && mCurrentNote.type == AppConstants.NoteType.TYPE_TEXT.value
            findItem(R.id.redo).isVisible = showRedoButton
                    && mCurrentNote.type == AppConstants.NoteType.TYPE_TEXT.value
        }

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        val multipleNotesExist = mNotes.size > 1
        menu.apply {
            findItem(R.id.rename_note).isVisible = multipleNotesExist
            findItem(R.id.open_note).isVisible = multipleNotesExist
            findItem(R.id.delete_note).isVisible = multipleNotesExist
            findItem(R.id.export_all_notes).isVisible = multipleNotesExist && hasStoragePermission()
            findItem(R.id.open_search).isVisible = !isCurrentItemChecklist()
            findItem(R.id.remove_done_items).isVisible = isCurrentItemChecklist()
            findItem(R.id.import_folder).isVisible = hasStoragePermission()

            saveNoteButton = findItem(R.id.save_note)
            saveNoteButton!!.isVisible =
                !config.autosaveNotes && showSaveButton && mCurrentNote.type == AppConstants.NoteType.TYPE_TEXT.value
        }

        pager_title_strip.visibility = if (multipleNotesExist) View.VISIBLE else View.GONE
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (config.autosaveNotes) {
            saveCurrentNote(false)
        }

        when (item.itemId) {
            R.id.open_note -> displayOpenNoteDialog()
            R.id.save_note -> saveNote()
            R.id.undo -> undo()
            R.id.redo -> redo()
            R.id.new_note -> displayNewNoteDialog()
            R.id.rename_note -> displayRenameDialog()
            R.id.share -> shareText()
            R.id.open_file -> tryOpenFile()
            R.id.import_folder -> openFolder()
            R.id.export_as_file -> tryExportAsFile()
            R.id.export_all_notes -> tryExportAllNotes()
            R.id.delete_note -> displayDeleteNotePrompt()
            R.id.settings -> startActivity(Intent(applicationContext, SettingsActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }

    fun setupUI() {

    }




    private fun redo() {


    }

    private fun undo() {

    }



    private fun tryExportAllNotes() {


    }

    private fun tryExportAsFile() {


    }

    private fun openFolder() {


    }

    private fun tryOpenFile() {

    }

    private fun shareText() {


    }
    // Dialogs

    private fun displayNewNoteDialog() {
        MaterialDialog(this).show {
            input()
            positiveButton(R.string.submit)
        }

    }

    private fun displayRenameDialog() {
        MaterialDialog(this).show {
            input()
            positiveButton(R.string.submit)
        }

    }
    private fun displayDeleteNotePrompt() {
        MaterialDialog(this).show {
            input()
            positiveButton(R.string.submit)
        }

    }
    private fun displayOpenNoteDialog() {
        TODO("Not yet implemented")
    }

    private fun saveNote() {
        TODO("Not yet implemented")
    }
    private fun saveCurrentNote(b: Boolean) {
        MaterialDialog(this).show {
            input()
            positiveButton(R.string.submit)
        }

    }


    override fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    override fun getViewModel(): MainViewModel {
        mMainViewModel =
            ViewModelProvider(this, MyViewModelFactory()).get(MainViewModel::class.java)
        return mMainViewModel
    }

    private fun initViewPager(wantedNoteId: Long? = null) {
        NotesHelper(this).getNotes {
            mNotes = it
            invalidateOptionsMenu()
            mCurrentNote = mNotes[0]
            mAdapter = NotesPagerAdapter(this, mNotes)

            viewBinding.mainViewPager.apply {
                adapter = mAdapter
                currentItem = getWantedNoteIndex(wantedNoteId)
                config.currentNoteId = mCurrentNote.id!!


                onPageChangeListener {
                    mCurrentNote = mNotes[it]
                    config.currentNoteId = mCurrentNote.id!!
                    invalidateOptionsMenu()
                }
            }

            if (!config.showKeyboard || mCurrentNote.type == AppConstants.NoteType.TYPE_CHECKLIST.value) {
                //hideKeyboard()
            }
        }
    }

    private fun getWantedNoteIndex(wantedNoteId: Long?): Int {
        return 0
    }


    private fun isCurrentItemChecklist(): Boolean{


    }

    private fun hasStoragePermission(): Boolean {

        return false

    }


}






}