package com.soufianekre.firenotes.ui.views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.HorizontalScrollView

class CustomHorizontalScrollView : HorizontalScrollView {
    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        parent.requestDisallowInterceptTouchEvent(false)
        return super.onTouchEvent(ev)
    }
}
