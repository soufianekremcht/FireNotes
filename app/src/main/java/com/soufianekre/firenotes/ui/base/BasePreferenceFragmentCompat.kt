package com.soufianekre.firenotes.ui.base

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import es.dmoral.toasty.Toasty


abstract class BasePreferenceFragmentCompat : PreferenceFragmentCompat(){

    private var mActivity : BaseActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        /*
        if (context is BaseActivity) {
            val activity = context as BaseActivity
            this.mActivity = activity
            activity.onFragmentAttached()
        }

         */


    }

    fun showInfo(msg :String){
        Toasty.info(requireActivity(),msg, Toasty.LENGTH_SHORT).show()

    }

    fun showSuccess(msg :String){
        Toasty.success(requireActivity(),msg, Toasty.LENGTH_SHORT).show()
    }

    fun showError(msg :String){
        Toasty.error(requireActivity(),msg, Toasty.LENGTH_SHORT).show()
    }




}
