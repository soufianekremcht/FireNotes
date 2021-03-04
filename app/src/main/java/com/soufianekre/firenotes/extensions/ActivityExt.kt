package com.soufianekre.firenotes.extensions

import android.app.Activity
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.soufianekre.firenotes.R
import es.dmoral.toasty.Toasty
import kotlinx.android.synthetic.main.dialog_title.view.*


fun Activity.setupDialogStuff(
    view: View,
    dialog: AlertDialog,
    titleId: Int = 0,
    titleText: String = "",
    callback: (() -> Unit)? = null
) {
    if (isDestroyed || isFinishing) {
        return
    }

    var title: TextView? = null
    if (titleId != 0 || titleText.isNotEmpty()) {
        title = layoutInflater.inflate(R.layout.dialog_title, null) as TextView
        title.dialog_title_textview.apply {
            if (titleText.isNotEmpty()) {
                text = titleText
            } else {
                setText(titleId)
            }
            //setTextColor(baseConfig.textColor)
        }

    }
}

fun Activity.showInfo(msg: String) {
    Toasty.info(this, msg, Toasty.LENGTH_SHORT).show()

}

fun Activity.showInfo(res: Int) {
    Toasty.info(this, getString(res), Toasty.LENGTH_SHORT).show()

}

fun Activity.showSuccess(msg: String) {
    Toasty.info(this, msg, Toasty.LENGTH_SHORT).show()
}

fun Activity.showSuccess(res: Int) {
    Toasty.info(this, getString(res), Toasty.LENGTH_SHORT).show()
}


fun Activity.showError(msg: String) {
    Toasty.error(this, msg, Toasty.LENGTH_SHORT).show()
}


fun Activity.showError(res: Int) {
    Toasty.error(this, getString(res), Toasty.LENGTH_SHORT).show()
}