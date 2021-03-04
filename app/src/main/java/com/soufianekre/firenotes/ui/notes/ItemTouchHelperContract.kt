package com.soufianekre.firenotes.ui.notes

import androidx.recyclerview.widget.RecyclerView


public interface ItemTouchHelperContract{
    fun onRowMoved(fromPosition: Int, toPosition: Int)

    fun onRowSelected(myViewHolder: RecyclerView.ViewHolder?)

    fun onRowClear(myViewHolder: RecyclerView.ViewHolder?)
}