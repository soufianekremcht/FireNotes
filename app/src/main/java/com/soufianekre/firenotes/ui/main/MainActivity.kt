package com.soufianekre.firenotes.ui.main

import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.soufianekre.firenotes.MyViewModelFactory
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.NotesDatabase
import com.soufianekre.firenotes.data.db.models.NoteObject
import com.soufianekre.firenotes.databinding.ActivityMainBinding
import com.soufianekre.firenotes.extensions.*
import com.soufianekre.firenotes.helper.AppConstants
import com.soufianekre.firenotes.helper.AppConstants.MIME_TEXT_PLAIN
import com.soufianekre.firenotes.helper.KeyboardUtils
import com.soufianekre.firenotes.helper.NotesHelper
import com.soufianekre.firenotes.ui.base.BaseActivity
import com.soufianekre.firenotes.ui.dialogs.*
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : BaseActivity() {


    private val EXPORT_FILE_SYNC = 1
    private val EXPORT_FILE_NO_SYNC = 2

    private val PICK_OPEN_FILE_INTENT = 1
    private val PICK_EXPORT_FILE_INTENT = 2

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

    lateinit var mViewDataBinding: ActivityMainBinding
    private lateinit var viewBinding: ActivityMainBinding
    private var factory: MyViewModelFactory? = null
    private lateinit var mMainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        mViewDataBinding.executePendingBindings()

        viewBinding = mViewDataBinding
        viewBinding.apply {
            vm = getViewModel()
        }

        setSupportActionBar(viewBinding.mainToolbar)


        initViewPager()

        val textColor = ContextCompat.getColor(this, R.color.black)
        viewBinding.pagerTitleStrip.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getPercentageFontSize())
            setGravity(Gravity.CENTER_VERTICAL)
            setNonPrimaryAlpha(0.4f)
            setTextColor(textColor)
        }
        viewBinding.pagerTitleStrip.layoutParams.height = (pager_title_strip.height +
                resources.getDimension(R.dimen.activity_margin) * 2 * (appConfig.fontSizePercentage / 100f)).toInt()

        checkIntents(intent)

        storeStateVariables()


        wasInit = true

        //checkAppOnSDCard()
        setupSearchButtons()
        if (appConfig.showNotePicker && savedInstanceState == null) {
            displayOpenNoteDialog()
        }

    }

    private fun getLayoutId(): Int {
        return R.layout.activity_main
    }

    private fun getViewModel(): MainViewModel {
        mMainViewModel =
            ViewModelProvider(this, MyViewModelFactory()).get(MainViewModel::class.java)
        return mMainViewModel
    }


    override fun onResume() {
        super.onResume()
        if (storedEnableLineWrap != appConfig.enableLineWrap) {
            initViewPager()
        }


        invalidateOptionsMenu()

        pager_title_strip.apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, getPercentageFontSize())
            setGravity(Gravity.CENTER_VERTICAL)
            setNonPrimaryAlpha(0.4f)
            setTextColor(Color.BLACK)
        }


    }

    override fun onPause() {
        super.onPause()
        storeStateVariables()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isChangingConfigurations) {
            NotesDatabase.destroyInstance()
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
            findItem(R.id.open_search).isVisible = false
            findItem(R.id.remove_done_items).isVisible = isCurrentItemChecklist()
            findItem(R.id.import_folder).isVisible = hasStoragePermission()

            saveNoteButton = findItem(R.id.save_note)
            saveNoteButton!!.isVisible =
                !appConfig.autosaveNotes && showSaveButton && mCurrentNote.type == AppConstants.NoteType.TYPE_TEXT.value
        }
        // tab layout pager
        pager_title_strip.visibility = View.VISIBLE
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (appConfig.autosaveNotes) {
            saveCurrentNote(false)
        }
        when (item.itemId) {
            R.id.open_note -> displayOpenNoteDialog()
            R.id.undo -> undo()
            R.id.redo -> redo()
            R.id.new_note -> displayNewNoteDialog("")
            R.id.rename_note -> displayRenameDialog()
            R.id.share -> shareText()
            R.id.open_file -> tryOpenFile()
            R.id.import_folder -> openFolder()
            R.id.export_as_file -> tryExportAsFile()
            R.id.export_all_notes -> tryExportAllNotes()
            R.id.delete_note -> displayDeleteNotePrompt()
            //R.id.settings -> startActivity(Intent(applicationContext, SettingsActivity::class.java))
            else -> return super.onOptionsItemSelected(item)
        }
        return true
    }


    private fun initViewPager(wantedNoteId: Long? = null) {

        NotesHelper(this).getNotes {
            mNotes = it
            invalidateOptionsMenu()
            mCurrentNote = mNotes[0]
            mAdapter = NotesPagerAdapter(
                supportFragmentManager,
                mNotes,
                this
            )
            viewBinding.mainViewPager.apply {
                adapter = mAdapter
                currentItem = getWantedNoteIndex(wantedNoteId)
                appConfig.currentNoteId = mCurrentNote.id!!



                addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
                    override fun onPageScrollStateChanged(state: Int) {}

                    override fun onPageScrolled(
                        position: Int,
                        positionOffset: Float,
                        positionOffsetPixels: Int
                    ) {

                    }

                    override fun onPageSelected(position: Int) {
                        mCurrentNote = mNotes[position]
                        appConfig.currentNoteId = mCurrentNote.id!!

                        invalidateOptionsMenu()
                    }
                })
            }

            if (!appConfig.showKeyboard || mCurrentNote.type == AppConstants.NoteType.TYPE_CHECKLIST.value) {
                KeyboardUtils.hideSoftInput(activity = this)
            }
        }
    }

    private fun setupSearchButtons() {


    }

    private fun storeStateVariables() {
        appConfig.apply {
            storedEnableLineWrap = enableLineWrap
        }
    }

    private fun getPercentageFontSize(): Float {
        return 17f
    }


    private fun checkIntents(intent: Intent) {
        // TODO :not yet
        intent.apply {
            if (action == Intent.ACTION_SEND && type == MIME_TEXT_PLAIN) {
                getStringExtra(Intent.EXTRA_TEXT)?.let {
                    handleTextIntent(it)
                    intent.removeExtra(Intent.EXTRA_TEXT)
                }
            }
            // TODO : not yet

            if (action == Intent.ACTION_VIEW) {
                /*
                val realPath = intent.getStringExtra(REAL_FILE_PATH)
                if (realPath != null && hasStoragePermission()) {
                    val file = File(realPath)
                    handleUri(Uri.fromFile(file))
                } else {
                    handleUri(data!!)
                }
                intent!!.removeCategory(Intent.CATEGORY_DEFAULT)
                intent.action = null

                 */
            }
        }

    }

    private fun handleTextIntent(text: String) {
    }

    private fun handleUri(uri: Uri) {
        NotesHelper(this).getNoteIdWithPath(uri.path!!) {
            if (it != null && it > 0L) {
                updateSelectedNote(it)
                return@getNoteIdWithPath
            }

            NotesHelper(this).getNotes {
                mNotes = it
                importUri(uri)
            }
        }
    }

    private fun importUri(uri: Uri) {

    }


    private fun currentNotesView() = mAdapter?.getCurrentNotesView(viewBinding.mainViewPager.currentItem)


    private fun updateSelectedNote(id: Long) {
        appConfig.currentNoteId = id
        if (mNotes.isEmpty()) {
            NotesHelper(this).getNotes {
                mNotes = it
                updateSelectedNote(id)
            }
        } else {
            val index = getNoteIndexWithId(id)
            viewBinding.mainViewPager.currentItem = index
            mCurrentNote = mNotes[index]
        }
    }

    private fun getNoteIndexWithId(noteId: Long): Int {
        for (i in 0 until mNotes.count()) {
            if (mNotes[i].id == noteId) {
                mCurrentNote = mNotes[i]
                return i
            }
        }
        return 0
    }


    private fun addNewNote(note: NoteObject) {
        NotesHelper(this).insertOrUpdateNote(note) {
            val newNoteId = it
            showSaveButton = false
            initViewPager(newNoteId)
            updateSelectedNote(newNoteId)
            viewBinding.mainViewPager.onGlobalLayout {
                mAdapter?.focusEditText(getNoteIndexWithId(newNoteId))
            }
        }
    }


    /*** MENU OPTIONS ***/

    private fun undo() {
        mAdapter?.undo(viewBinding.mainViewPager.currentItem)
    }

    private fun redo() {
        mAdapter?.redo(viewBinding.mainViewPager.currentItem)
    }


    private fun tryExportAllNotes() {
        // TODO : not yet

    }

    private fun tryExportAsFile() {
        // TODO : not yet

    }

    private fun openFolder() {
        //TODO : display Folder Picker dialog """ check Floating Draw" ""
        /*
        FilePickerDialog(this, pickFile = false, canAddShowHiddenButton = true) {
            openFolder(it) {
                ImportFolderDialog(this, it.path) {
                    NotesHelper(this).getNotes {
                        mNotes = it
                        showSaveButton = false
                        initViewPager()
                    }
                }
            }
        }

         */
    }

    private fun openFolder(path: String, onChecksPassed: (file: File) -> Unit) {
        // TODO : not yet
        val file = File(path)
        if (file.isDirectory) {
            onChecksPassed(file)
        }

    }

    private fun shareText() {
        // TODO : not yet
    }

    private fun tryOpenFile() {
        // TODO : not yet
        if (hasStoragePermission()) {
            openFile()
        } else {
            Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "text/*"
                startActivityForResult(this, PICK_OPEN_FILE_INTENT)
            }
        }
    }

    private fun openFile() {

    }

    private fun checkFile(path: String, checkTitle: Boolean, onChecksPassed: (file: File) -> Unit) {
        val file = File(path)
        if (path.isMediaFile()) {
            showInfo(getString(R.string.invalid_file_format))
        } else if (file.length() > 1000 * 1000) {
            showInfo(getString(R.string.file_too_large))
        } else if (checkTitle && mNotes.any { it.title.equals(path.getFilenameFromPath(), true) }) {
            showInfo(getString(R.string.title_taken))
        } else {
            onChecksPassed(file)
        }
    }

    private fun checkUri(uri: Uri, onChecksPassed: () -> Unit) {
        val inputStream = try {
            contentResolver.openInputStream(uri) ?: return
        } catch (e: Exception) {
            showError(e.localizedMessage)
            return
        }

        if (inputStream.available() > 1000 * 1000) {
            showInfo(getString(R.string.file_too_large))
        } else {
            onChecksPassed()
        }
    }


    /** Dialogs ***/

    private fun displayNewNoteDialog(noteText: String) {
        // TODO : not yet

        NewNoteDialog(this, noteText) {
            it.content = ""
            addNewNote(it)
        }

    }

    private fun displayRenameDialog() {

        RenameNoteDialog(this,mCurrentNote,getCurrentNoteText()){
            mCurrentNote = it
            initViewPager(mCurrentNote.id)
        }

    }

    private fun displayDeleteNotePrompt() {
        // TODO : not yet
        DeleteNoteDialog(this,mCurrentNote){
            showError("Note has been deleted.")
        }

    }

    private fun displayOpenNoteDialog() {
     OpenNoteDialog(this) { noteId, newNote ->
         if (newNote == null) {
             updateSelectedNote(noteId)
         } else {
             addNewNote(newNote)
         }
     }}






    /*** Others ***/

    private fun saveCurrentNote(b: Boolean) {
        // TODO

    }


    private fun getWantedNoteIndex(wantedNoteId: Long?): Int {
        // TODO : not yet
        return 0
    }


    private fun isCurrentItemChecklist(): Boolean {
        // TODO : not yet
        return false

    }

    private fun hasStoragePermission(): Boolean {
        // TODO : not yet
        return false

    }

    fun currentNoteTextChanged(text: String, undoAvailable: Boolean, redoAvailable: Boolean) {
        // TODO("Not yet implemented")
    }

    fun noteSavedSuccessfully(title: String?) {


    }

    fun tryExportNoteValueToFile(path: String, currentText: String, displaySuccess: Boolean) {
        // TODO("Not yet implemented")
    }
    private fun getPagerAdapter() = viewBinding.mainViewPager.adapter as NotesPagerAdapter
    private fun getCurrentNoteText() = getPagerAdapter().getCurrentNoteViewText(viewBinding.mainViewPager.currentItem)

}




