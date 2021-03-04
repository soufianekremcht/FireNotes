package com.soufianekre.firenotes.ui.notes

import android.app.Activity
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.view.*
import androidx.appcompat.view.ActionMode
import android.widget.TextView
import androidx.appcompat.app.ActionBar
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.soufianekre.firenotes.R
import com.soufianekre.firenotes.data.db.models.ChecklistItem
import com.soufianekre.firenotes.extensions.beVisibleIf
import com.soufianekre.firenotes.extensions.appConfig
import com.soufianekre.firenotes.helper.AppConstants.DONE_CHECKLIST_ITEM_ALPHA
import com.soufianekre.firenotes.ui.dialogs.RenameChecklistItemDialog
import kotlinx.android.synthetic.main.item_checklist.view.*
import java.util.*
import kotlin.collections.ArrayList


open class ChecklistAdapter(
    var activity: Activity,
    var items: ArrayList<ChecklistItem>,
    val listener: ChecklistItemsListener?,
    recyclerView: RecyclerView,
    val showIcons: Boolean,
    itemClick: (Any) -> Unit) :
    RecyclerView.Adapter<ChecklistAdapter.ChecklistViewHolder>(), ItemTouchHelperContract {

    protected var selectedKeys = LinkedHashSet<Int>()
    protected var actModeCallback: MyActionModeCallback
    protected var positionOffset = 0

    private var actMode: ActionMode? = null
    private var actBarTextView: TextView? = null
    private var lastLongPressedItem = -1

    //    (activity, recyclerView, null, itemClick)
    private lateinit var crossDrawable: Drawable
    private lateinit var checkDrawable: Drawable
    private var touchHelper: ItemTouchHelper? = null
    //private var startReorderDragListener: StartReorderDragListener

    init {
        // TODO : not yet
        //setupDragListener(true)
        initDrawables()

        actModeCallback = object : MyActionModeCallback() {
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean {
                actionItemPressed(item.itemId)
                return true
            }

            override fun onCreateActionMode(actionMode: ActionMode, menu: Menu?): Boolean {
                if (getActionMenuId() == 0) {
                    return true
                }
                isSelectable = true
                actMode = actionMode
                actBarTextView = activity.layoutInflater.inflate(R.layout.actionbar_title, null) as TextView
                actBarTextView!!.layoutParams =
                    ActionBar.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.MATCH_PARENT)

                actMode!!.customView = actBarTextView
                actBarTextView!!.setOnClickListener {
                    if (getSelectableItemCount() == selectedKeys.size) {
                        finishActMode()
                    } else {
                        selectAll()
                    }
                }
                activity.menuInflater.inflate(getActionMenuId(), menu)
                onActionModeCreated()
                return true
            }

            override fun onPrepareActionMode(actionMode: ActionMode, menu: Menu): Boolean {
                prepareActionMode(menu)
                return true
            }

            override fun onDestroyActionMode(actionMode: ActionMode) {
                isSelectable = false
                (selectedKeys.clone() as HashSet<Int>).forEach {
                    val position = getItemKeyPosition(it)
                    if (position != -1) {
                        toggleItemSelection(false, position, false)
                    }
                }
                updateTitle()
                selectedKeys.clear()
                actBarTextView?.text = ""
                actMode = null
                lastLongPressedItem = -1
                onActionModeDestroyed()
            }

        }

        //touchHelper = ItemTouchHelper(ItemMoveCallback(this))
        //touchHelper!!.attachToRecyclerView(recyclerView)d
    }


    /*** Action Mode ****/

    fun getActionMenuId() = R.menu.actionmode_checklist

    fun actionItemPressed(id: Int) {
        if (selectedKeys.isEmpty()) {
            return
        }

        when (id) {
            R.id.cab_move_to_top -> moveSelectedItemsToTop()
            R.id.cab_move_to_bottom -> moveSelectedItemsToBottom()
            R.id.cab_rename -> renameChecklistItem()
            R.id.cab_delete -> deleteSelection()
        }
    }

    fun onActionModeCreated() {
        notifyDataSetChanged()
    }

    fun onActionModeDestroyed() {
        notifyDataSetChanged()
    }

    fun prepareActionMode(menu: Menu) {
        val selectedItems = getSelectedItems()
        if (selectedItems.isEmpty()) {
            return
        }

        //menu.findItem(R.id.cab_rename).isVisible = isOneItemSelected()
    }


    private fun finishActMode() {
        actMode?.finish()

    }


    private fun updateTitle() {
        val selectableItemCount = getSelectableItemCount()
        val selectedCount = Math.min(selectedKeys.size, selectableItemCount)
        val oldTitle = actBarTextView?.text
        val newTitle = "$selectedCount / $selectableItemCount"
        if (oldTitle != newTitle) {
            actBarTextView?.text = newTitle
            actMode?.invalidate()
        }
    }


    /*** Selection Mode ***/
    protected fun getSelectedItemPositions(sortDescending: Boolean = true): ArrayList<Int> {
        val positions = ArrayList<Int>()
        val keys = selectedKeys.toList()
        keys.forEach {
            val position = getItemKeyPosition(it)
            if (position != -1) {
                positions.add(position)
            }
        }

        if (sortDescending) {
            positions.sortDescending()
        }
        return positions
    }

    protected fun selectAll() {
        val cnt = itemCount - positionOffset
        for (i in 0 until cnt) {
            toggleItemSelection(true, i, false)
        }
        lastLongPressedItem = -1
        updateTitle()
    }


    private fun toggleItemSelection(select: Boolean, pos: Int, updateTitle: Boolean = true) {
        if (select && !getIsItemSelectable(pos)) {
            return
        }

        val itemKey = getItemSelectionKey(pos) ?: return
        if ((select && selectedKeys.contains(itemKey)) || (!select && !selectedKeys.contains(itemKey))) {
            return
        }

        if (select) {
            selectedKeys.add(itemKey)
        } else {
            selectedKeys.remove(itemKey)
        }

        notifyItemChanged(pos + positionOffset)

        if (updateTitle) {
            updateTitle()
        }

        if (selectedKeys.isEmpty()) {
            finishActMode()
        }
    }


    fun getSelectableItemCount() = items.size

    fun getIsItemSelectable(position: Int) = true

    fun getItemSelectionKey(position: Int) = items.getOrNull(position)?.id

    fun getItemKeyPosition(key: Int) = items.indexOfFirst { it.id == key }

    private fun deleteSelection() {
        val removeItems = ArrayList<ChecklistItem>(selectedKeys.size)
        val positions = ArrayList<Int>()
        selectedKeys.forEach {
            val key = it
            val position = items.indexOfFirst { it.id == key }
            if (position != -1) {
                positions.add(position)

                val favorite = getItemWithKey(key)
                if (favorite != null) {
                    removeItems.add(favorite)
                }
            }
        }

        positions.sortDescending()
        removeSelectedItems(positions)

        items.removeAll(removeItems)
        listener?.saveChecklist()
        if (items.isEmpty()) {
            listener?.refreshItems()
        }
    }

    protected fun removeSelectedItems(positions: ArrayList<Int>) {
        positions.forEach {
            notifyItemRemoved(it)
        }
        finishActMode()
        //fastScroller?.measureRecyclerView()
    }


    private fun moveSelectedItemsToTop() {
        selectedKeys.reversed().forEach { checklistId ->
            val position = items.indexOfFirst { it.id == checklistId }
            val tempItem = items[position]
            items.removeAt(position)
            items.add(0, tempItem)
        }

        notifyDataSetChanged()
        listener?.saveChecklist()
    }

    private fun moveSelectedItemsToBottom() {
        selectedKeys.forEach { checklistId ->
            val position = items.indexOfFirst { it.id == checklistId }
            val tempItem = items[position]
            items.removeAt(position)
            items.add(items.size, tempItem)
        }

        notifyDataSetChanged()
        listener?.saveChecklist()
    }

    private fun getItemWithKey(key: Int): ChecklistItem? = items.firstOrNull { it.id == key }

    private fun getSelectedItems() =
        items.filter { selectedKeys.contains(it.id) } as ArrayList<ChecklistItem>


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChecklistViewHolder {

        var root =
            LayoutInflater.from(parent.context).inflate(R.layout.item_checklist, parent, false)
        return ChecklistViewHolder(root)
    }

    override fun onBindViewHolder(holder: ChecklistViewHolder, position: Int) {
        val item = items[position]
        setupView(holder.itemView, item, holder = holder)
    }

    override fun getItemCount() = items.size

    private fun setupView(
        view: View,
        checklistItem: ChecklistItem,
        holder: RecyclerView.ViewHolder
    ) {
        val isSelected = selectedKeys.contains(checklistItem.id)
        view.apply {
            checklist_title.apply {
                // TODO : not yet
                text = checklistItem.title
                //setTextColor(textColor)
                //setTextSize(TypedValue.COMPLEX_UNIT_PX, context.getPercentageFontSize())
                gravity = context.appConfig.getTextGravity()

                if (checklistItem.isDone) {
                    paintFlags = paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                    alpha = DONE_CHECKLIST_ITEM_ALPHA
                } else {
                    //paintFlags = paintFlags.removeBit(Paint.STRIKE_THRU_TEXT_FLAG)
                    alpha = 1f
                }
            }

            checklist_image.setImageDrawable(if (checklistItem.isDone) checkDrawable else crossDrawable)
            checklist_image.beVisibleIf(showIcons)
            checklist_holder.isSelected = isSelected

            checklist_drag_handle.beVisibleIf(selectedKeys.isNotEmpty())
        }
    }


    /*** Private Methods **/
    private fun initDrawables() {
        val res = activity.resources
        crossDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_cross)!!
        checkDrawable = ContextCompat.getDrawable(activity, R.drawable.ic_check)!!
    }

    private fun renameChecklistItem() {
        val item = getSelectedItems().first()
        RenameChecklistItemDialog(activity!!, item.title) {
            val position = getSelectedItemPositions().first()
            item.title = it
            listener?.saveChecklist()
            notifyItemChanged(position)
            finishActMode()
        }
    }


    override fun onRowMoved(fromPosition: Int, toPosition: Int) {
        if (fromPosition < toPosition) {
            for (i in fromPosition until toPosition) {
                Collections.swap(items, i, i + 1)
            }
        } else {
            for (i in fromPosition downTo toPosition + 1) {
                Collections.swap(items, i, i - 1)
            }
        }
        notifyItemMoved(fromPosition, toPosition)
    }

    override fun onRowSelected(myViewHolder: RecyclerView.ViewHolder?) {
    }

    override fun onRowClear(myViewHolder: RecyclerView.ViewHolder?) {
        listener?.saveChecklist()
    }


    inner class ChecklistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }


}
