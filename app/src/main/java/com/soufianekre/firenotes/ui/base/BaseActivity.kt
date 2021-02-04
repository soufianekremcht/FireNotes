package com.soufianekre.firenotes.ui.base


import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import es.dmoral.toasty.Toasty


abstract class BaseActivity<T : ViewDataBinding,V:BaseViewModel> : AppCompatActivity() {


    private var mViewDataBinding: T? = null
    private var mViewModel: V? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        performDataBinding()
    }


    open fun getViewDataBinding(): T {
        return mViewDataBinding!!
    }

    abstract fun getLayoutId() :Int
    abstract fun getViewModel() : V

    private fun performDataBinding() {
        mViewDataBinding = DataBindingUtil.setContentView(this, getLayoutId())
        mViewModel = mViewModel ?: getViewModel()
        mViewDataBinding?.executePendingBindings()
    }



    fun showInfo(msg :String){
        Toasty.info(this,msg, Toasty.LENGTH_SHORT).show()

    }

    fun showSuccess(msg :String){
        Toasty.info(this,msg, Toasty.LENGTH_SHORT).show()
    }

    fun showError(msg :String){
        Toasty.error(this,msg,Toasty.LENGTH_SHORT).show()
    }

}